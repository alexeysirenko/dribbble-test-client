package services

import javax.inject.{Inject, Singleton}

import play.api.libs.ws._
import play.api.http.HttpEntity
import models._
import play.api.Configuration

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class DribbbleService @Inject()(wsClient: WSClient, config: Configuration) {

  private lazy val dribbbleEndpoint: String = config.get[String]("api.dribbble.endpoint")
  private lazy val accessToken: String = config.get[String]("api.dribbble.clientAccessToken")

  def getLikers(login: String): Future[List[Liker]] = {
    val maybeLikes: Future[List[Like]] = for {
      followers <- getFollowers(login)
      shots <- Future.traverse(followers.filter(_.follower.shots_count > 0))(follower => getShots(follower.follower.username)).map(_.flatten)
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
    wsClient.url(s"$dribbbleEndpoint/users/$username/followers")
      .withHttpHeaders("Authorization" -> s"Bearer $accessToken")
      .get() flatMap { response =>
        response.status match {
          case 200 => Future.successful(response.json.as[List[Follower]])
          case _ => Future.failed(new Exception(s"Failed to get followers of `$username`: Invalid response code ${response.status}"))
        }
      }
  }

  def getShots(username: String): Future[List[Shot]] = {
    wsClient.url(s"$dribbbleEndpoint/users/$username/shots")
      .withHttpHeaders("Authorization" -> s"Bearer $accessToken")
      .get() flatMap { response =>
        response.status match {
          case 200 => Future.successful(response.json.as[List[Shot]])
          case _ => Future.failed(new Exception(s"Failed to get shots of `$username`: Invalid response code ${response.status}"))
        }
      }
  }

  def getLikes(shot: Shot): Future[List[Like]] = {
    wsClient.url(s"$dribbbleEndpoint/shots/${shot.id}/likes")
      .withHttpHeaders("Authorization" -> s"Bearer $accessToken")
      .get() flatMap { response =>
        response.status match {
          case 200 => Future.successful(response.json.as[List[Like]])
          case _ => Future.failed(new Exception(s"Failed to get likes of `$shot`: Invalid response code ${response.status}"))
        }
      }
  }
}
