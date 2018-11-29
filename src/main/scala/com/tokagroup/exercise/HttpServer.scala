package com.tokagroup.exercise

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, TextMessage, UpgradeToWebSocket}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.tokagroup.exercise.actors.ManagerActor

import scala.language.postfixOps

class HttpServer private(interface: String, port: Int)
                        (implicit actorSystem: ActorSystem,
                         materializer: ActorMaterializer) {
  import akka.http.scaladsl.server.Directives._
  import ch.megard.akka.http.cors.scaladsl.CorsDirectives._

  val log = actorSystem.log
  val manager = actorSystem.actorOf(ManagerActor.props(
    Settings.zookeeperHost,
    Settings.zookeeperPort,
    Settings.zookeeperTimeout
  ))

  val routes = cors() {
    (pathPrefix("socket") & extractRequest & extractUnmatchedPath) {
      (request, path) => request.header[UpgradeToWebSocket] match {
        case Some(upgrade) =>
          log.debug("Starting socket")
          complete(upgrade.handleMessages(socketFlow(path.toString)))
        case None =>
          log.debug("Request for socket failed")
          reject
      }
    }
  }

  /***
    * Builds Flow to handle WebSocket.
    * From this flow socket will receive messages with events for the watched path
    * @param path - path to watch
    * @return - flow that emits messages with events for the watched path
    */
  def socketFlow(path: String): Flow[Message, Message, _] = {
    val source = Source.actorRef[String](1, OverflowStrategy.dropHead)
        .mapMaterializedValue(a => {
          manager ! ManagerActor.Watch(path, a)
          a
        })
        .map(TextMessage(_))

    Flow.fromSinkAndSourceCoupled(Sink.ignore, source)
  }

  /***
    * Start listening to incoming connections
    * @return Future[Http.ServerBinding]
    */
  def start = Http().bindAndHandle(routes, interface, port)

}

object HttpServer {

  /***
    * Creates http server
    * @param interface - interface to bind to
    * @param port - port to bind to
    * @param actorSystem - actor system of current run
    * @param materializer - actor materializer of current run (for akka-stream)
    * @return new instance of com.tokagroup.exercise.HttpServer
    */
  def apply(interface: String, port: Int)
           (implicit actorSystem: ActorSystem,
            materializer: ActorMaterializer): HttpServer =
    new HttpServer(interface, port)

}
