package models

import play.api.libs.json.{Json, OFormat}

case class Like(id: Long,
                user: User)

object Like {
  implicit val format: OFormat[Like] = Json.format[Like]
}
