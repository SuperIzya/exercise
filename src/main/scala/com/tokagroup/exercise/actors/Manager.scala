package com.tokagroup.exercise.actors

import java.io.IOException

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Terminated}
import akka.util.ByteString
import com.tokagroup.exercise.Settings
import com.tokagroup.exercise.actors.Manager.Watch
import com.tokagroup.exercise.model.WriteToNode
import org.apache.zookeeper.AsyncCallback.StatCallback
import org.apache.zookeeper.Watcher.Event.KeeperState
import org.apache.zookeeper.ZooDefs.Ids
import org.apache.zookeeper.data.Stat
import org.apache.zookeeper.{CreateMode, WatchedEvent, ZooKeeper}

import scala.concurrent.duration._

/** *
  * Actor that manages the connection to ZK and creates and manages watchers for z-nodes.
  */
class Manager(host: String,
              port: Int,
              sessionTimeout: FiniteDuration)
  extends Actor with ActorLogging {

  val connectionString = s"$host:$port"

  /**
    * Flag indicating that connection to zk was actually established
    */
  var connected: Boolean = false
  /**
    *  ActorRef associated with server, when it waits for initial connection
    */
  var server: ActorRef = _
  /**
    * Connection to the zookeeper
    */
  var zkConnection: ZooKeeper = connect
  /**
    *  Map of all watchers (path -> WatcherActor)
    */
  var watchers: Map[String, ActorRef] = Map.empty
  /**
    * Reverse map of watcher. Needed for convenient delete
    */
  var reverseMap: Map[ActorRef, String] = Map.empty

  def connect: ZooKeeper = new ZooKeeper(connectionString, sessionTimeout.toMillis.toInt, watchConnection)

  def watchConnection(event: WatchedEvent): Unit = event.getState match {
    case KeeperState.Disconnected => self ! 'reconnect
    case KeeperState.Expired => self ! 'reconnect
    case KeeperState.SyncConnected =>
      log.info("Successfully connected to zookeeper")
      connected = true

      if(server != null) server ! 'ok

      if(zkConnection.exists("/", false) == null)
        throw new IOException("Zookeeper is inaccessible")
    case _ =>
  }

  /** *
    * Creates watcher for provided path or returns existing one (if it was created earlier)
    *
    * @param path       - path to watch
    * @param subscriber - subscriber, that requested to watch the path
    * @return ActorRef of the WatcherActor for requested path
    */
  def getWatcher(path: String, subscriber: ActorRef): ActorRef = watchers.get(path) match {
    case Some(actor) =>
      log.info(s"Adding watcher for path $path for existing actor")
      actor ! Watcher.Subscribe(subscriber)
      actor
    case None =>
      log.info(s"Creating watcher actor for path $path")
      val actor = context.actorOf(Watcher.props(path, zkConnection))
      context watch actor
      watchers += path -> actor
      reverseMap += actor -> path
      actor ! Watcher.Subscribe(subscriber)
      actor
  }

  override def receive: Receive = {
    case 'init =>
      if(!connected) server = sender()
      else sender() ! 'ok
    case 'reconnect =>
      log.info("Connection to zookeeper terminated. Reconnecting")
      zkConnection = connect
    case Watch(path, watcher) => getWatcher(path, watcher)
    case WriteToNode(path, stringData) =>
      val data = ByteString(stringData, "utf-8").toArray
      val callback: StatCallback = (_, _, _, stat: Stat) => {
        try {
          if (stat == null) {
            val acl = Ids.OPEN_ACL_UNSAFE
            val res = zkConnection.create(path, data, acl, CreateMode.PERSISTENT)
            log.info(s"Created node $path with log $stringData. Result: $res")
          }
          else {
            log.info(s"Writing to node $path data $stringData, version ${stat.getVersion}")
            zkConnection.setData(path, data, stat.getVersion)
          }
        } catch {
          case ex: Throwable => log.error(ex, "Error occurred while writing to zk")
        }
      }
      log.info("Going to write data to zk")
      zkConnection.exists(path, false, callback, null)
    case Terminated(actor) => reverseMap.get(actor) match {
      case Some(path) =>
        watchers = watchers - path
        reverseMap = reverseMap - actor
      case None =>
    }
  }

  override def postStop(): Unit = {
    super.postStop()
    zkConnection.close()
  }
}

object Manager {
  val props = Props(new Manager(
    Settings.zookeeperHost,
    Settings.zookeeperPort,
    Settings.zookeeperTimeout
  ))

  case class Watch(path: String, watcher: ActorRef)

}