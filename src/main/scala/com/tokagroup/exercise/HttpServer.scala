package com.tokagroup.exercise

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, TextMessage, UpgradeToWebSocket}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.tokagroup.exercise.actors.{Manager, SocketManager}
import com.tokagroup.exercise.model.WriteToNode
import play.api.libs.json.Json


/** *
  * Http server based on akka-http
  *
  * @param interface    - interface to listen to
  * @param port         - port to listen
  * @param actorSystem  - actor system of current run
  * @param materializer - actor materializer of current run (for Flow)
  */
class HttpServer private(interface: String, port: Int)
                        (implicit actorSystem: ActorSystem,
                         materializer: ActorMaterializer) {

  import akka.http.scaladsl.server.Directives._
  import ch.megard.akka.http.cors.scaladsl.CorsDirectives._

  val log = actorSystem.log
  val manager = actorSystem.actorOf(Manager.props)

  val routes = cors() {
    get {
      pathSingleSlash {
        getFromResource("interface/index.html")
      } ~ pathPrefix("web") {
        getFromResourceDirectory("interface/web")
      }
    } ~ (path("api" / "socket") & extractRequest) {
      _.header[UpgradeToWebSocket] match {
        case Some(upgrade) =>
          log.debug("Starting socket")
          complete(upgrade.handleMessages(socketFlow))
        case None =>
          log.debug("Request for socket failed")
          reject
      }
    } ~ post {
      (path("api" / "write") & extractDataBytes) { bytes =>
        val data = bytes.runFold(StringBuilder.newBuilder)((acc, i) => acc.append(i.utf8String))
        val json = Json.parse(data.toString)
        val write = Json.fromJson[WriteToNode](json)
        manager ! write
        complete("Ok")
      }
    }
  }

  /** *
    * Builds Flow to handle WebSocket.
    * From this flow socket will receive messages with events for the watched path
    *
    * @param path - path to watch
    * @return - flow that emits messages with events for the watched path
    */
  def socketFlow: Flow[Message, Message, _] = {
    val socketManager = actorSystem.actorOf(SocketManager.props(manager))
    val source = Source.actorRef[String](1, OverflowStrategy.dropHead)
      .mapMaterializedValue(a => {
        socketManager ! SocketManager.Source(a)
        a
      })
      .map(TextMessage(_))

    val sink = Flow[Message]
      .map(_.asTextMessage.getStrictText)
      .map(socketManager ! SocketManager.WatchPath(_))
      .to(Sink.ignore)

    Flow.fromSinkAndSourceCoupled(sink, source)
  }

  /** *
    * Start listening to incoming connections
    *
    * @return Future[Http.ServerBinding]
    */
  def start = Http().bindAndHandle(routes, interface, port)
}

object HttpServer {
  /** *
    * Creates http server
    *
    * @param interface    - interface to bind to
    * @param port         - port to bind to
    * @param actorSystem  - actor system of current run
    * @param materializer - actor materializer of current run (for akka-stream)
    * @return new instance of com.tokagroup.exercise.HttpServer
    */
  def apply(interface: String, port: Int)
           (implicit actorSystem: ActorSystem,
            materializer: ActorMaterializer): HttpServer =
    new HttpServer(interface, port)
}
