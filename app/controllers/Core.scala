package controllers

import java.util.Date
import models.{ChangeModel,UserModel}
import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.mvc._
import play.api.mvc.Security._
import play.db._
import org.slf4j.{Logger,LoggerFactory}

object Core extends Controller with Secured {

  val initialChangeForm = Form(
    mapping(
      "owner_id"    -> longNumber,
      "change_type_id" -> longNumber,
      "duration"    -> longNumber,
      "risk"        -> number,
      "summary"     -> nonEmptyText,
      "description" -> optional(text),
      "date_creatd" -> ignored(new Date()),
      "date_scheduled" -> date
    )(models.InitialChange.apply)(models.InitialChange.unapply)
  )

  val logger = LoggerFactory.getLogger("application")

  def add = IsAuthenticated { implicit request =>

    initialChangeForm.bindFromRequest.fold(
      errors => {
        val users = UserModel.getAll.map { u => (u.id.get.toString -> u.realName )}

        BadRequest(views.html.index(errors, users))
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

  // def item(id: Long) = IsAuthenticated { implicit request =>

  //   val maybeChange = ChangeModel.getById(id)

  //   maybeChange match {
  //     case Some(change) => Ok(views.html.change.item(change))
  //     case None => NotFound
  //   }
  // }

  def index = IsAuthenticated { implicit request =>

    val userId = request.session.get("user_id").get.toLong
    val users = UserModel.getAll.map { u => (u.id.get.toString -> u.realName )}

    Ok(views.html.index(initialChangeForm, users))
  }
}
