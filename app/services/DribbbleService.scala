package services

import javax.inject.{Inject, Singleton}

import akka.actor.ActorSystem
import akka.pattern.after
import models._
import play.api.Configuration
import play.api.libs.json.JsValue
import play.api.libs.ws._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

@Singleton
class DribbbleService @Inject()(wsClient: WSClient, config: Configuration, actorSystem: ActorSystem) {

  private lazy val dribbbleEndpoint: String = config.get[String]("api.dribbble.endpoint")
  private lazy val accessToken: String = config.get[String]("api.dribbble.clientAccessToken")

  def getLikers(login: String): Future[List[Liker]] = {
    val maybeLikes: Future[List[Like]] = for {
      followers <- getFollowers(login)
      shots <- Future.traverse(followers.filter(_.follower.shots_count > 0))(follower =>
        getShots(follower.follower.username)).map(_.flatten)
      likes <- Future.traverse(shots.filter(_.likes_count > 0))(shot => getLikes(shot)).map(_.flatten)
    } yield likes

    maybeLikes.map { likes =>
      val likers = (likes.groupBy(_.user.id) mapValues { userLikes =>
        Liker(userLikes.head.user, userLikes.length)
      }).values.toList
      likers.sortBy(liker => -liker.likesCount)
    }
  }

  def getFollowers(username: String): Future[List[Follower]] = {
    dribbbleRequest(s"users/$username/followers").map(json => json.as[List[Follower]])
  }

  def getShots(username: String): Future[List[Shot]] = {
    dribbbleRequest(s"users/$username/shots").map(json => json.as[List[Shot]])
  }

  def getLikes(shot: Shot): Future[List[Like]] = {
    dribbbleRequest(s"shots/${shot.id}/likes").map(json => json.as[List[Like]])
  }

  private def dribbbleRequest(url: String): Future[JsValue] = {
    def requestWithBackoff(nRetries: Int, base: Int = 2, delay: FiniteDuration = 500 millis): Future[JsValue] = {
      val MAX_RETRIES = 8
      wsClient.url(s"$dribbbleEndpoint/$url")
        .withHttpHeaders("Authorization" -> s"Bearer $accessToken").get() flatMap { response =>
          response.status match {
            case 200 => Future.successful(response.json)
            case 429 if nRetries < MAX_RETRIES =>
              val interval = delay * Math.pow(2, nRetries).toLong
              after(interval, actorSystem.scheduler)(requestWithBackoff(nRetries + 1))
            case 429 => Future.failed(new Exception(s"Number of retries exceeded: ${response.status} ${response.body}"))
            case _ => Future.failed(new Exception(s"Invalid response: ${response.status} ${response.body}"))
          }
        }
    }

    requestWithBackoff(nRetries = 0)
  }
}
