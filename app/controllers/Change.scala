package controllers

import anorm._
import java.text.SimpleDateFormat
import models.{ChangeModel,ChangeTypeModel,SearchModel,UserModel}
import org.joda.time.{DateTime,DateTimeZone,LocalTime}
import org.joda.time.format.DateTimeFormat
import play.api.data._
import play.api.data.Forms._
import play.api.i18n.Messages
import play.api.mvc._
import sultan.MoreForms._

object Change extends Controller with Secured {

  def deriveDate(date: Option[DateTime], time: Option[LocalTime]): Option[DateTime] = {

    if(date.isDefined) {
      if(time.isDefined) {
        Some(date.get.withFields(time.get))
      } else {
        date
      }
    } else {
      None
    }
  }

  def getTime(date: Option[DateTime]): Option[LocalTime] = date.map({ d => Some(d.toLocalTime) }).getOrElse(None)

  val addForm = Form(
    mapping(
      "id"          -> ignored(NotAssigned:Pk[Long]),
      "user_id"     -> ignored[Long](0),
      "owner_id"    -> longNumber,
      "change_type_id" -> longNumber,
      "duration"    -> longNumber,
      "risk"        -> number,
      "summary"     -> nonEmptyText,
      "description" -> optional(text),
      "notes"       -> optional(ignored[String]("")),
      "date_begun"  -> optional(ignored[DateTime](new DateTime())),
      "time_scheduled" -> optional(jodaTime),
      "date_closed" -> optional(ignored[DateTime](new DateTime())),
      "date_completed" -> optional(ignored[DateTime](new DateTime())),
      "date_created" -> ignored[DateTime](new DateTime()),
      "date_scheduled" -> jodaDate,
      "time_scheduled" -> jodaTime,
      "success"      -> ignored[Boolean](false)
    )
    ((id, user_id, owner_id, change_type_id, duration, risk, summary, description, notes, date_begun, time_begun, date_closed, date_completed, date_created, date_scheduled, time_scheduled, success) => models.Change(id, user_id, owner_id, change_type_id, duration, risk, summary, description, notes, deriveDate(date_begun, time_begun), date_closed, date_completed, date_created, deriveDate(Some(date_scheduled), Some(time_scheduled)).get, success))
    ((change: models.Change) => Some((
      change.id, change.userId, change.ownerId, change.changeTypeId,
      change.duration, change.risk, change.summary, change.description,
      change.notes, change.dateBegun, getTime(change.dateBegun),
      change.dateClosed, change.dateCompleted, change.dateCreated,
      change.dateScheduled, getTime(Some(change.dateScheduled)).get,
      change.success)
    ))
  )

  val editForm = Form(
    mapping(
      "id"          -> ignored(NotAssigned:Pk[Long]),
      "user_id"     -> ignored[Long](0),
      "owner_id"    -> longNumber,
      "change_type_id" -> longNumber,
      "duration"    -> longNumber,
      "risk"        -> number,
      "summary"     -> nonEmptyText,
      "description" -> optional(text),
      "notes"       -> optional(ignored[String]("")),
      "date_begun"  -> optional(jodaDate),
      "time_begun"  -> optional(jodaTime),
      "date_closed" -> optional(jodaDate),
      "time_closed" -> optional(jodaTime),
      "date_completed" -> optional(jodaDate),
      "date_created" -> ignored[DateTime](new DateTime()),
      "date_scheduled" -> jodaDate,
      "time_scheduled" -> jodaTime,
      "success"      -> boolean
    )
    ((id, user_id, owner_id, change_type_id, duration, risk, summary, description, notes, date_begun, time_begun, date_closed, time_closed, date_completed, date_created, date_scheduled, time_scheduled, success) => {
      models.Change(
        id = id,
        userId = user_id,
        ownerId = owner_id,
        changeTypeId = change_type_id,
        duration = duration,
        risk = risk,
        summary = summary,
        description = description,
        notes = notes,
        dateBegun = deriveDate(date_begun, time_begun),
        dateClosed = deriveDate(date_closed, time_closed),
        dateCompleted = date_completed,
        dateCreated = date_created,
        dateScheduled = deriveDate(Some(date_scheduled), Some(time_scheduled)).get,
        success = success
      )
    })
    ((change: models.Change) => Some((
      change.id, change.userId, change.ownerId, change.changeTypeId,
      change.duration, change.risk, change.summary, change.description,
      change.notes, change.dateBegun, getTime(change.dateBegun),
      change.dateClosed, getTime(change.dateClosed), change.dateCompleted,
      change.dateCreated, change.dateScheduled,
      getTime(Some(change.dateScheduled)).get, change.success)
    ))
  )

  def add = IsAuthenticated { implicit request =>

    addForm.bindFromRequest.fold(
      errors => {
        val users = UserModel.getAll.map { u => (u.id.get.toString -> u.realName )}
        val types = ChangeTypeModel.getAll.map { ct => (ct.id.get.toString -> Messages(ct.name) )}

        BadRequest(views.html.change.create(errors, users, types))
      },
      value => {
        val change = ChangeModel.create(userId = request.user.id.get, change = value)
        change match {
          case Some(t) => Redirect(routes.Core.index()).flashing("success" -> "change.add.success")
          case None => Redirect(routes.Core.index()).flashing("error" -> "change.add.failure")
        }
      }
    )
  }

  def create = IsAuthenticated { implicit request =>

    val userId = request.user.id
    val users = UserModel.getAll.map { u => (u.id.get.toString -> u.realName )}
    val types = ChangeTypeModel.getAll.map { ct => (ct.id.get.toString -> Messages(ct.name) )}

    Ok(views.html.change.create(addForm, users, types))
  }

  def edit(id: Long) = IsAuthenticated { implicit request =>

    ChangeModel.getById(id).map({ change =>
      val users = UserModel.getAll.map { u => (u.id.get.toString -> u.realName )}
      val types = ChangeTypeModel.getAll.map { ct => (ct.id.get.toString -> Messages(ct.name) )}

      val localizedChange = change.copy(
        dateBegun = change.dateBegun.map({ d => Some(d.withZone(DateTimeZone.forID(request.user.timeZone))) }).getOrElse(None),
        dateCompleted = change.dateCompleted.map({ d => Some(d.withZone(DateTimeZone.forID(request.user.timeZone))) }).getOrElse(None),
        dateScheduled = change.dateScheduled.withZone(DateTimeZone.forID(request.user.timeZone))
      )

      Ok(views.html.change.edit(id, editForm.fill(localizedChange), users, types))
    }).getOrElse(NotFound)
  }

  def item(id: Long) = IsAuthenticated { implicit request =>

    ChangeModel.getById(id).map({ change =>
      Ok(views.html.change.item(change)(request))
    }).getOrElse(NotFound)
  }

  def update(id: Long) = IsAuthenticated { implicit request =>

    editForm.bindFromRequest.fold(
      errors => {
        val users = UserModel.getAll.map { u => (u.id.get.toString -> u.realName )}
        val types = ChangeTypeModel.getAll.map { ct => (ct.id.get.toString -> Messages(ct.name) )}

        BadRequest(views.html.change.edit(id, errors, users, types))
      },
      value => {
        val change = ChangeModel.update(userId = request.user.id.get, id = id, change = value)
        change match {
          case Some(c) => {
            SearchModel.indexChange(c)
            Redirect(routes.Change.item(id)).flashing("success" -> "change.edit.success")
          }
          case None => Redirect(routes.Change.item(id)).flashing("error" -> "change.edit.failure")
        }
      }
    )
  }
}