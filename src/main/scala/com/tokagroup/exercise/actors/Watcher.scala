package com.tokagroup.exercise.actors

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props, Terminated}
import akka.routing.{ActorRefRoutee, BroadcastRoutingLogic, Router}
import com.tokagroup.exercise.actors.Watcher.Subscribe
import org.apache.zookeeper.Watcher.Event.EventType
import org.apache.zookeeper.{WatchedEvent, ZooKeeper}

/** *
  * Actor that manages all watchers for current path
  *
  * @param subscriber   - first subscriber that requested the path
  * @param path         - path to watch
  * @param zkConnection - connection to ZK
  */
class Watcher(subscriber: ActorRef,
              path: String,
              zkConnection: ZooKeeper)
  extends Actor with ActorLogging {

  var subscribers = Router(BroadcastRoutingLogic(), Vector.empty[ActorRefRoutee]).addRoutee(subscriber)
  var active = true

  /***
    * Start actually watching the z-node
    */
  def startWatch: Unit = zkConnection.exists(
    path,
    (event: WatchedEvent) => if(active) {
      self ! event.getType
      startWatch
    })

  def sendEvent(event: String): Unit = {
    subscribers.route(s"$path: $event", self)
  }

  override def receive: Receive = {
    case Subscribe(actor) =>
      context watch actor
      subscribers = subscribers.addRoutee(actor)
    case Terminated(actor) =>
      subscribers = subscribers.removeRoutee(actor)
      // If no subscribers left, terminate the actor
      if(subscribers.routees.isEmpty) {
        active = false
        self ! PoisonPill
      }
    case EventType.NodeChildrenChanged => sendEvent("NodeChildrenChanged")
    case EventType.NodeCreated => sendEvent("NodeCreated")
    case EventType.NodeDataChanged => sendEvent("NodeDataChanged")
    case EventType.NodeDeleted => sendEvent("NodeDeleted")
    case _ =>
  }
}

object Watcher {
  def props(subscriber: ActorRef,
            path: String,
            zkConnection: ZooKeeper) = Props(new Watcher(subscriber, path, zkConnection))

  case class Subscribe(subscriber: ActorRef)
}
