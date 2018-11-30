package com.tokagroup.exercise.actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}

/***
  * Actor that manages web-socket
  */
class SocketManager(manager: ActorRef) extends Actor with ActorLogging {
  import SocketManager._

  var source: ActorRef = _

  override def receive: Receive = {
    case Source(actor) => source = actor
    case WatchPath(path) => manager ! Manager.Watch(path, source)
  }
}

object SocketManager {
  def props(manager: ActorRef) = Props(new SocketManager(manager))

  case class Source(actor: ActorRef)
  case class WatchPath(path: String)
}
