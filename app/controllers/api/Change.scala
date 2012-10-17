package controllers.api

import controllers._
import models.ChangeModel
import play.api.libs.Jsonp
import play.api.libs.json.Json
import play.api.mvc._
import sultan.JsonFormats._

object Change extends Controller with Secured {

  def item(id: Long, callback: Option[String]) = IsAuthenticated { implicit request =>

    def maybeChange = ChangeModel.getById(id)

    maybeChange match {
      case Some(change) => {
        val json = Json.toJson(change)
        callback match {
          case Some(c) => Ok(Jsonp(c, json))
          case None => Ok(json)
        }
      }
      case None => NotFound
    }
  }
}