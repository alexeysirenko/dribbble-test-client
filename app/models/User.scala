package models

import play.api.libs.json.{Json, OFormat}

case class User(id: Long,
                name: String,
                username: String,
                html_url: String,
                avatar_url: String,
                shots_count: Long)

object User {
  implicit val format: OFormat[User] = Json.format[User]
}