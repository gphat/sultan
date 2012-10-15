package controllers

import controllers.Auth._
import play.api._
import play.api.mvc._
import models.SearchModel

object Admin extends Controller with Secured {

  def index = IsAuthenticated { implicit request =>

    Ok(views.html.admin.index(request))
  }

  def reindex = IsAuthenticated { implicit request =>

    SearchModel.reIndex
    Redirect(routes.Admin.index).flashing("success" -> "admin.reindex.success")
  }
}