package controllers

import java.util.Date
import models.{ChangeModel,ChangeTypeModel,UserModel}
import play.api.data._
import play.api.data.Forms._
import play.api.mvc._

object Change extends Controller with Secured {

  val initialChangeForm = Form(
    mapping(
      "owner_id"    -> longNumber,
      "change_type_id" -> longNumber,
      "duration"    -> longNumber,
      "risk"        -> number,
      "summary"     -> nonEmptyText,
      "description" -> optional(text),
      "date_created" -> ignored(new Date()),
      "date_scheduled" -> date
    )(models.InitialChange.apply)(models.InitialChange.unapply)
  )

  def add = IsAuthenticated { implicit request =>

    initialChangeForm.bindFromRequest.fold(
      errors => {
        val users = UserModel.getAll.map { u => (u.id.get.toString -> u.realName )}
        val types = ChangeTypeModel.getAll.map { ct => (ct.id.get.toString -> ct.name )}

        BadRequest(views.html.change.create(errors, users, types))
      },
      value => {
        val change = ChangeModel.create(userId = request.user.id.get, change = value)
        change match {
          case Some(t) => Redirect(routes.Core.index()).flashing("success" -> "ticket.add.success")
          case None => Redirect(routes.Core.index()).flashing("error" -> "ticket.add.failure")
        }
      }
    )
  }

  def create = IsAuthenticated { implicit request =>

    val userId = request.session.get("user_id").get.toLong
    val users = UserModel.getAll.map { u => (u.id.get.toString -> u.realName )}
    val types = ChangeTypeModel.getAll.map { ct => (ct.id.get.toString -> ct.name )}

    Ok(views.html.change.create(initialChangeForm, users, types))
  }

  def item(id: Long) = IsAuthenticated { implicit request =>

    val maybeChange = ChangeModel.getById(id)

    maybeChange match {
      case Some(change) => Ok(views.html.change.item(change)(request))
      case None => NotFound
    }
  }
}