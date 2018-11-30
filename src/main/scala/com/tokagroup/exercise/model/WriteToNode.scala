package com.tokagroup.exercise.model

import play.api.libs.json.Json


case class WriteToNode(path: String, data: String)

object WriteToNode {
  implicit val format = Json.format[WriteToNode]
}