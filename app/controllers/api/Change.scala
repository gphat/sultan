package controllers.api

import controllers._
import models.ChangeModel
import org.joda.time.DateTime
import play.api.libs.Jsonp
import play.api.libs.json.Json
import play.api.mvc._
import sultan.JsonFormats._

object Change extends Controller with Secured {

  // XXX Ensure UTC

  def begin(id: Long, callback: Option[String]) = IsAuthenticated { implicit request =>

    ChangeModel.getById(id).map({ change =>
      ChangeModel.update(request.user.id.get, id, change.copy(
        dateBegun = Some(new DateTime()) // XXX Ultimately this should be overridable
      )).map({ newchange =>
        val json = Json.toJson(newchange)
        val res: Result = callback.map({ c => Ok(Jsonp(c, json)) }).getOrElse(Ok(json))
        res
      }).getOrElse(BadRequest)
    }).getOrElse(NotFound)
  }

  def close(id: Long, callback: Option[String]) = IsAuthenticated { implicit request =>

    ChangeModel.getById(id).map({ change =>
      ChangeModel.update(request.user.id.get, id, change.copy(
        dateClosed = Some(new DateTime()) // XXX Ultimately this should be overridable
      )).map({ newchange =>
        val json = Json.toJson(newchange)
        val res: Result = callback.map({ c => Ok(Jsonp(c, json)) }).getOrElse(Ok(json))
        res
      }).getOrElse(BadRequest)
    }).getOrElse(NotFound)
  }

  def fail(id: Long, callback: Option[String]) = IsAuthenticated { implicit request =>

    ChangeModel.getById(id).map({ change =>
      ChangeModel.update(request.user.id.get, id, change.copy(
        dateCompleted = Some(new DateTime()), // XXX Ultimately this should be overridable
        success = false
      )).map({ newchange =>
        val json = Json.toJson(newchange)
        val res: Result = callback.map({ c => Ok(Jsonp(c, json)) }).getOrElse(Ok(json))
        res
      }).getOrElse(BadRequest)
    }).getOrElse(NotFound)
  }

  def item(id: Long, callback: Option[String]) = IsAuthenticated { implicit request =>

    ChangeModel.getById(id).map({ change =>
      val json = Json.toJson(change)
      val res: Result = callback.map({ c => Ok(Jsonp(c, json)) }).getOrElse(Ok(json))
      res
    }).getOrElse(NotFound)
  }

  def reset(id: Long, callback: Option[String]) = IsAuthenticated { implicit request =>

    ChangeModel.getById(id).map({ change =>
      ChangeModel.update(request.user.id.get, id, change.copy(
        dateBegun = None,
        dateClosed = None,
        dateCompleted = None
      )).map({ newchange =>
        val json = Json.toJson(newchange)
        val res: Result = callback.map({ c => Ok(Jsonp(c, json)) }).getOrElse(Ok(json))
        res
      }).getOrElse(BadRequest)
    }).getOrElse(NotFound)
  }

  def success(id: Long, callback: Option[String]) = IsAuthenticated { implicit request =>

    ChangeModel.getById(id).map({ change =>
      ChangeModel.update(request.user.id.get, id, change.copy(
        dateCompleted = Some(new DateTime()), // XXX Ultimately this should be overridable
        success = true
      )).map({ newchange =>
        val json = Json.toJson(newchange)
        val res: Result = callback.map({ c => Ok(Jsonp(c, json)) }).getOrElse(Ok(json))
        res
      }).getOrElse(BadRequest)
    }).getOrElse(NotFound)
  }
}