package com.tokagroup.exercise.actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.tokagroup.exercise.Settings
import com.tokagroup.exercise.actors.ManagerActor.Watch
import org.apache.zookeeper.Watcher.Event.KeeperState
import org.apache.zookeeper.{WatchedEvent, ZooKeeper}

import scala.concurrent.duration.FiniteDuration

/***
  * Actor that manages the connection to ZK and creates and manages watchers for z-nodes.
  */
class ManagerActor(host: String,
                   port: Int,
                   sessionTimeout: FiniteDuration)
  extends Actor with ActorLogging {

  // Connection to the zookeeper
  var zkConnection: ZooKeeper = connect
  // Map of all watchers (path -> WatcherActor)
  var watchers: Map[String, ActorRef] = Map.empty

  def connect: ZooKeeper = new ZooKeeper(s"$host:$port", sessionTimeout.toMillis.toInt, watch)

  def watch(event: WatchedEvent): Unit = event.getState match {
    case KeeperState.Disconnected => self ! 'reconnect
    case KeeperState.Expired => self ! 'reconnect
  }

  /***
    * Creates watcher for provided path or returns existing one (if it was created earlier)
    * @param path - path to watch
    * @param subscriber - subscriber, that requested to watch the path
    * @return ActorRef of the WatcherActor for requested path
    */
  def getWatcher(path: String, subscriber: ActorRef): ActorRef = watchers.get(path) match {
    case Some(actor) =>
      actor ! WatcherActor.Subscribe(subscriber)
      actor
    case None =>
      val actor = context.actorOf(WatcherActor.props(subscriber, path, zkConnection))
      watchers += path -> actor
      actor
  }

  override def receive: Receive = {
    case 'reconnect => zkConnection = connect
    case Watch(path, watcher) => getWatcher(path, watcher)
  }

  override def postStop(): Unit = {
    super.postStop()
    zkConnection.close()
  }
}

object ManagerActor {
  val props = Props(new ManagerActor(
    Settings.zookeeperHost,
    Settings.zookeeperPort,
    Settings.zookeeperTimeout
  ))

  case class Watch(path: String, watcher: ActorRef)
}