package controllers.admin

import anorm._
import controllers._
import org.joda.time.{DateTime,DateTimeZone,LocalTime}
import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.i18n.Messages
import play.api.mvc._
import play.api.libs.json.Json
import models._
import sultan.MoreForms._

object Window extends Controller with Secured {

  def deriveDate(date: DateTime, time: LocalTime): DateTime = date.withFields(time)

  def getTime(date: DateTime): LocalTime = date.toLocalTime

  val addForm = Form(
    mapping(
      "id"  -> ignored(NotAssigned:Pk[Long]),
      "window_type_id"    -> longNumber,
      "owner_id"    -> longNumber,
      "name"-> nonEmptyText,
      "description" -> optional(text),
      "date_begun"  -> jodaDate,
      "time_begun"  -> jodaTime,
      "date_ended"  -> jodaDate,
      "time_ended"  -> jodaTime,
      "date_created" -> ignored(new DateTime())
    )
    ((id, owner_id, window_type_id, name, description, date_begun, time_begun, date_ended, time_ended, date_created) => models.Window(id, owner_id, window_type_id, name, description, deriveDate(date_begun, time_begun), deriveDate(date_ended, time_ended), date_created))
    ((window: models.Window) => Some((
      window.id, window.ownerId, window.windowTypeId, window.name,
      window.description, window.dateBegun, getTime(window.dateBegun),
      window.dateEnded, getTime(window.dateEnded), window.dateCreated
    )))
  )

  def add = IsAuthenticated { implicit request =>

    addForm.bindFromRequest.fold(
      errors => {
        BadRequest(views.html.admin.window.create(errors))
      },
      value => {
        WindowModel.create(request.user.id.get, value).map({ window =>
          Redirect(controllers.admin.routes.Window.item(window.id.get)).flashing("success" -> "admin.window.add.success")
        }).getOrElse(BadRequest(views.html.admin.window.create(addForm.fill(value))))
      }
    )
  }

  def create = IsAuthenticated { implicit request =>

    Ok(views.html.admin.window.create(addForm)(request))
  }

  def index(page: Int, count: Int) = IsAuthenticated { implicit request =>

    val windows = WindowModel.list(page = page, count = count)

    Ok(views.html.admin.window.index(windows)(request))
  }

  def edit(id: Long) = IsAuthenticated { implicit request =>

    WindowModel.getById(id).map({ window =>
      Ok(views.html.admin.window.edit(id, addForm.fill(window))(request))
    }).getOrElse(NotFound)
  }

  def item(id: Long) = IsAuthenticated { implicit request =>

    WindowModel.getById(id).map({ window =>
      Ok(views.html.admin.window.item(window)(request))
    }).getOrElse(NotFound)
  }

  def update(id: Long) = IsAuthenticated { implicit request =>

    addForm.bindFromRequest.fold(
      errors => {
        BadRequest(views.html.admin.window.edit(id, errors))
      },
      value => {
        WindowModel.update(request.user.id.get, id, value)
        Redirect(routes.Window.item(id)).flashing("success" -> "admin.window.edit.success")
      }
    )
  }
}
