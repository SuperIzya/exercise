package com.tokagroup.exercise

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

object Main extends App {
  implicit val actorSystem = ActorSystem("RaspberryFarm")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = actorSystem.dispatcher


  val f = HttpServer("localhost", 8080).start

  /*StdIn.readLine()
  StdIn.readLine()

  f.flatMap(_.unbind()).onComplete(_ => actorSystem.terminate())
  */
}
