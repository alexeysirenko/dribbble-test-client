package controllers

import javax.inject._

import play.api.libs.json.Json
import play.api.mvc._
import services.DribbbleService

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class ApplicationController @Inject()(dribbbleService: DribbbleService, cc: ControllerComponents) extends AbstractController(cc) {

  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index())
  }

  def top10(login: String): Action[AnyContent] = Action.async { implicit request =>
    dribbbleService.getLikers(login) map { likers =>  Ok(Json.toJson(likers.take(10))) }
  }
}
