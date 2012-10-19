package controllers

import anorm._
import java.text.SimpleDateFormat
import java.util.Date
import models.{ChangeModel,ChangeTypeModel,SearchModel,UserModel}
import play.api.data._
import play.api.data.Forms._
import play.api.i18n.Messages
import play.api.mvc._

object Change extends Controller with Secured {

  val dateFormatter = new SimpleDateFormat("yyyy-MM-dd")
  val finalFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm")
  val timeFormatter = new SimpleDateFormat("HH:mm")

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
      "date_begun" -> optional(ignored[Date](new Date())),
      "date_closed" -> optional(ignored[Date](new Date())),
      "date_completed" -> optional(ignored[Date](new Date())),
      "date_created" -> ignored[Date](new Date()),
      "date_scheduled" -> date,
      "time_scheduled" -> nonEmptyText,
      "success"      -> ignored[Boolean](false)
    )
    ((id, user_id, owner_id, change_type_id, duration, risk, summary, description, notes, date_begun, date_closed, date_completed, date_created, date_scheduled, time_scheduled, success) => models.Change(id, user_id, owner_id, change_type_id, duration, risk, summary, description, notes, date_begun, date_closed, date_completed, date_created, finalFormatter.parse(dateFormatter.format(date_scheduled) + " " + time_scheduled), success))
    ((change: models.Change) => Some((change.id, change.userId, change.ownerId, change.changeTypeId, change.duration, change.risk, change.summary, change.description, change.notes, change.dateBegun, change.dateClosed, change.dateCompleted, change.dateCreated, change.dateScheduled, timeFormatter.format(change.dateScheduled), change.success)))
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
      "date_begun" -> optional(date),
      "date_closed" -> optional(date),
      "date_completed" -> optional(date),
      "date_created" -> ignored[Date](new Date()),
      "date_scheduled" -> date,
      "success"      -> boolean
    )(models.Change.apply)(models.Change.unapply)
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

    val maybeChange = ChangeModel.getById(id)

    maybeChange match {
      case Some(change) => {
        val users = UserModel.getAll.map { u => (u.id.get.toString -> u.realName )}
        val types = ChangeTypeModel.getAll.map { ct => (ct.id.get.toString -> Messages(ct.name) )}

        Ok(views.html.change.edit(id, editForm.fill(change), users, types))
      }
      case None => NotFound
    }
  }

  def item(id: Long) = IsAuthenticated { implicit request =>

    val maybeChange = ChangeModel.getById(id)

    maybeChange match {
      case Some(change) => Ok(views.html.change.item(change)(request))
      case None => NotFound
    }
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