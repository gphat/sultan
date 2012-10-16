package controllers.admin

import anorm._
import controllers._
import java.util.Date
import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.i18n.Messages
import play.api.mvc._
import play.api.libs.json.Json
import models._

object ChangeType extends Controller with Secured {

  val addTypeForm = Form(
    mapping(
      "id"    -> ignored(NotAssigned:Pk[Long]),
      "name"  -> nonEmptyText,
      "color" -> nonEmptyText,
      "date_created" -> ignored(new Date())
    )(models.ChangeType.apply)(models.ChangeType.unapply)
  )

  val editTypeForm = Form(
    mapping(
      "id"    -> ignored(NotAssigned:Pk[Long]),
      "name"  -> nonEmptyText,
      "color" -> nonEmptyText,
      "date_created" -> ignored(new Date())
    )(models.ChangeType.apply)(models.ChangeType.unapply)
  )

  def add = IsAuthenticated { implicit request =>

    addTypeForm.bindFromRequest.fold(
      errors => {
        BadRequest(views.html.admin.change_type.create(errors))
      },
      value => {
        val type = ChangeTypeModel.create(value)
        Redirect(controllers.admin.routes.ChangeType.item(type.id.get)).flashing("success" -> "admin.change_type.add.success")
      }
    )
  }

  def create = IsAuthenticated { implicit request =>

    Ok(views.html.admin.change_type.create(addTypeForm)(request))
  }

  def index(page: Int, count: Int) = IsAuthenticated { implicit request =>

    val types = ChangeTypeModel.list(page = page, count = count)

    Ok(views.html.admin.change_type.index(types)(request))
  }

  def edit(id: Long) = IsAuthenticated { implicit request =>

    val type = ChangeTypeModel.getById(id)

    type match {
      case Some(value) => {
        Ok(views.html.admin.change_type.edit(id, editTypeForm.fill(value))(request))
      }
      case None => NotFound
    }
  }

  def item(id: Long) = IsAuthenticated { implicit request =>

    val type = ChangeTypeModel.getById(id)

    type match {
      case Some(value) => Ok(views.html.admin.change_type.item(value)(request))
      case None => NotFound
    }
  }

  def update(id: Long) = IsAuthenticated { implicit request =>

    editTypeForm.bindFromRequest.fold(
      errors => {
        BadRequest(views.html.admin.change_type.edit(id, errors))
      },
      value => {
        ChangeTypeModel.update(id, value)
        Redirect(routes.ChangeType.item(id)).flashing("success" -> "admin.change_type.edit.success")
      }
    )
  }
}
