package com.tokagroup.exercise

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

object Main extends App {
  implicit val actorSystem = ActorSystem("RaspberryFarm")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = actorSystem.dispatcher


  val f = HttpServer("localhost", Settings.bindPort).start

  sys.addShutdownHook({
    f.flatMap(_.unbind()).onComplete(_ => actorSystem.terminate())
  })

}
