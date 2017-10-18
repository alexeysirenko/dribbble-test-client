package models

import play.api.libs.json.{Json, OFormat}

case class Shot(id: Long,
                likes_count: Long)

object Shot {
  implicit val format: OFormat[Shot] = Json.format[Shot]
}