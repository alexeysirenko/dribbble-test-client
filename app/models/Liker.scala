package models

import play.api.libs.json.Json

case class Liker(user: User, likesCount: Int)

object Liker {
  implicit val format = Json.format[Liker]
}
