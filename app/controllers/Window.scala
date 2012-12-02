package controllers

import anorm._
import controllers._
import org.joda.time.{DateTime,DateTimeZone,LocalTime}
import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.i18n.Messages
import play.api.mvc._
import play.api.libs.json.Json
import models._
import sultan.MoreForms._

object Window extends Controller with Secured {

  def item(id: Long) = IsAuthenticated { implicit request =>

    WindowModel.getById(id).map({ window =>
      val wt = WindowTypeModel.getById(window.windowTypeId).get
      Ok(views.html.window.item(window, wt)(request))
    }).getOrElse(NotFound)
  }
}
