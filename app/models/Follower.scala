package models

import play.api.libs.json.{Json, OFormat}

case class Follower(id: Long, follower: User)

object Follower {
  implicit val format: OFormat[Follower] = Json.format[Follower]
}
