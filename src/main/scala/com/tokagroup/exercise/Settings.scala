package com.tokagroup.exercise

import scala.concurrent.duration.FiniteDuration

/***
  * Settings of current run. Taken from environment variables with fallback to default values.
  */
object Settings {
  val zookeeperHost: String = sys.env.getOrElse("ZK_HOST", "zookeeper")
  val zookeeperPort: Int = sys.env.getOrElse("ZK_PORT", "32181").toInt
  val zookeeperTimeout: FiniteDuration = {
    val to = sys.env.getOrElse("ZK_TIMEOUT", "2 seconds").split(" ")
    val long = to.head.toLong
    FiniteDuration(long, if(to.size == 1) "seconds" else to(1))
  }
  val bindPort: Int = sys.env.getOrElse("BIND_PORT", "9000").toInt
}
