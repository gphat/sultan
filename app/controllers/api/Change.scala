package controllers.api

import controllers._
import java.util.Date
import models.ChangeModel
import play.api.libs.Jsonp
import play.api.libs.json.Json
import play.api.mvc._
import sultan.JsonFormats._

object Change extends Controller with Secured {

  // XXX Ensure UTC

  def begin(id: Long, callback: Option[String]) = IsAuthenticated { implicit request =>
    def maybeChange = ChangeModel.getById(id)

    maybeChange match {
      case Some(change) => {
        ChangeModel.update(request.user.id.get, id, change.copy(
          dateBegun = Some(new Date()))
        ) // XXX Ultimately this should be overridable
        val json = Json.toJson(Map("ok" -> "ok"))
        callback match {
          case Some(c) => Ok(Jsonp(c, json))
          case None => Ok(json)
        }
      }
      case None => NotFound
    }
  }

  def close(id: Long, callback: Option[String]) = IsAuthenticated { implicit request =>
    def maybeChange = ChangeModel.getById(id)

    maybeChange match {
      case Some(change) => {
        ChangeModel.update(request.user.id.get, id, change.copy(
          dateClosed = Some(new Date()))
        ) // XXX Ultimately this should be overridable
        val json = Json.toJson(Map("ok" -> "ok"))
        callback match {
          case Some(c) => Ok(Jsonp(c, json))
          case None => Ok(json)
        }
      }
      case None => NotFound
    }
  }

  def fail(id: Long, callback: Option[String]) = IsAuthenticated { implicit request =>
    def maybeChange = ChangeModel.getById(id)

    maybeChange match {
      case Some(change) => {
        ChangeModel.update(request.user.id.get, id, change.copy(
          dateCompleted = Some(new Date()),
          success = false
        )) // XXX Ultimately this should be overridable
        val json = Json.toJson(Map("ok" -> "ok"))
        callback match {
          case Some(c) => Ok(Jsonp(c, json))
          case None => Ok(json)
        }
      }
      case None => NotFound
    }
  }

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

  def reset(id: Long, callback: Option[String]) = IsAuthenticated { implicit request =>
    def maybeChange = ChangeModel.getById(id)

    maybeChange match {
      case Some(change) => {
        ChangeModel.update(request.user.id.get, id, change.copy(
          dateBegun = None,
          dateClosed = None,
          dateCompleted = None
        )) // XXX Ultimately this should be overridable
        val json = Json.toJson(Map("ok" -> "ok"))
        callback match {
          case Some(c) => Ok(Jsonp(c, json))
          case None => Ok(json)
        }
      }
      case None => NotFound
    }
  }

  def success(id: Long, callback: Option[String]) = IsAuthenticated { implicit request =>
    def maybeChange = ChangeModel.getById(id)

    maybeChange match {
      case Some(change) => {
        ChangeModel.update(request.user.id.get, id, change.copy(
          dateCompleted = Some(new Date()),
          success = true
        )) // XXX Ultimately this should be overridable
        val json = Json.toJson(Map("ok" -> "ok"))
        callback match {
          case Some(c) => Ok(Jsonp(c, json))
          case None => Ok(json)
        }
      }
      case None => NotFound
    }
  }
}