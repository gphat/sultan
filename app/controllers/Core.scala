package controllers

import play.api._
import play.api.mvc._
import play.api.mvc.Security._
import play.db._
import org.slf4j.{Logger,LoggerFactory}

object Core extends Controller with Secured {

  val logger = LoggerFactory.getLogger("application")

  def index = IsAuthenticated { implicit request =>

    val userId = request.session.get("user_id").get.toLong

    Ok(views.html.index("Asdasd"))
  }
}
