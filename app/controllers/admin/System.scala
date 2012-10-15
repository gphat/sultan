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

object System extends Controller with Secured {

  val addSystemForm = Form(
    mapping(
      "id"  -> ignored(NotAssigned:Pk[Long]),
      "name"-> nonEmptyText,
      "date_created" -> ignored(new Date())
    )(models.System.apply)(models.System.unapply)
  )

  val editSystemForm = Form(
    mapping(
      "id"  -> ignored(NotAssigned:Pk[Long]),
      "name"-> nonEmptyText,
      "date_created" -> ignored(new Date())
    )(models.System.apply)(models.System.unapply)
  )

  def add = IsAuthenticated { implicit request =>

    addSystemForm.bindFromRequest.fold(
      errors => {
        BadRequest(views.html.admin.system.create(errors))
      },
      value => {
        val system = SystemModel.create(value)
        Redirect(controllers.admin.routes.System.item(system.id.get)).flashing("success" -> "admin.system.add.success")
      }
    )
  }

  def create = IsAuthenticated { implicit request =>

    Ok(views.html.admin.system.create(addSystemForm)(request))
  }

  def index(page: Int, count: Int) = IsAuthenticated { implicit request =>

    val systems = SystemModel.list(page = page, count = count)

    Ok(views.html.admin.system.index(systems)(request))
  }

  def edit(id: Long) = IsAuthenticated { implicit request =>

    val system = SystemModel.getById(id)

    system match {
      case Some(value) => {
        Ok(views.html.admin.system.edit(id, editSystemForm.fill(value))(request))
      }
      case None => NotFound
    }
  }

  def item(id: Long) = IsAuthenticated { implicit request =>

    val system = SystemModel.getById(id)

    system match {
      case Some(value) => Ok(views.html.admin.system.item(value)(request))
      case None => NotFound
    }
  }

  def update(id: Long) = IsAuthenticated { implicit request =>

    editSystemForm.bindFromRequest.fold(
      errors => {
        BadRequest(views.html.admin.system.edit(id, errors))
      },
      value => {
        SystemModel.update(id, value)
        Redirect(routes.System.item(id)).flashing("success" -> "admin.system.edit.success")
      }
    )
  }
}
