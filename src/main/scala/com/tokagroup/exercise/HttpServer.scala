package com.tokagroup.exercise

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes.InternalServerError
import akka.http.scaladsl.model.ws.{Message, TextMessage, UpgradeToWebSocket}
import akka.pattern.ask
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.util.Timeout
import com.tokagroup.exercise.actors.{Manager, SocketManager}
import com.tokagroup.exercise.model.WriteToNode
import play.api.libs.json.{JsError, JsSuccess, Json}
import scala.util.{Failure, Success}


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

  import actorSystem.dispatcher
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
        onComplete(bytes.runFold(StringBuilder.newBuilder)((acc, i) => acc.append(i.utf8String))) {
          case Success(data) =>
            val json = Json.parse(data.toString)
            Json.fromJson[WriteToNode](json) match {
              case JsSuccess(write, _) =>
                log.info(s"Writing to zk $write")
                manager ! write
                complete("Ok")
              case JsError(errors) =>
                complete((InternalServerError, s"Error while parsing request data $errors"))
            }
          case Failure(ex) => complete((InternalServerError, s"An error occurred: ${ex.getMessage}"))
        }
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
  def start = {
    import scala.concurrent.duration._
    implicit val timeout = Timeout(5 minutes)
    (manager ? 'init).flatMap(_ => Http().bindAndHandle(routes, interface, port))
  }
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
