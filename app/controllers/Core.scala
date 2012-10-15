package controllers

import java.util.Date
import play.api._
import play.api.mvc._
import play.api.mvc.Security._
import play.db._

object Core extends Controller with Secured {

  def index = IsAuthenticated { implicit request =>

    Ok(views.html.index(request))
  }
}
