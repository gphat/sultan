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

object WindowType extends Controller with Secured {

  val addTypeForm = Form(
    mapping(
      "id"    -> ignored(NotAssigned:Pk[Long]),
      "name"  -> nonEmptyText,
      "threshold" -> number(min = 0),
      "color" -> nonEmptyText
    )(models.WindowType.apply)(models.WindowType.unapply)
  )

  def add = IsAuthenticated { implicit request =>

    addTypeForm.bindFromRequest.fold(
      errors => {
        BadRequest(views.html.admin.window_type.create(errors))
      },
      value => {
        WindowTypeModel.create(value).map({ wt =>
          Redirect(controllers.admin.routes.WindowType.item(wt.id.get)).flashing("success" -> "admin.window_type.add.success")
        }).getOrElse(
          BadRequest(views.html.admin.window_type.create(addTypeForm.fill(value)))
        )
      }
    )
  }

  def create = IsAuthenticated { implicit request =>

    Ok(views.html.admin.window_type.create(addTypeForm)(request))
  }

  def index(page: Int, count: Int) = IsAuthenticated { implicit request =>

    val types = WindowTypeModel.list(page = page, count = count)

    Ok(views.html.admin.window_type.index(types)(request))
  }

  def edit(id: Long) = IsAuthenticated { implicit request =>

    WindowTypeModel.getById(id).map({ wt =>
      Ok(views.html.admin.window_type.edit(id, addTypeForm.fill(wt))(request))
    }).getOrElse(NotFound)
  }

  def item(id: Long) = IsAuthenticated { implicit request =>

    WindowTypeModel.getById(id).map({ wt =>
      Ok(views.html.admin.window_type.item(wt)(request))
    }).getOrElse(NotFound)
  }

  def update(id: Long) = IsAuthenticated { implicit request =>

    addTypeForm.bindFromRequest.fold(
      errors => {
        BadRequest(views.html.admin.window_type.edit(id, errors))
      },
      value => {
        WindowTypeModel.update(id, value)
        Redirect(controllers.admin.routes.WindowType.item(id)).flashing("success" -> "admin.window_type.edit.success")
      }
    )
  }
}
