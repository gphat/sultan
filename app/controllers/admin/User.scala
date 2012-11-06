package controllers.admin

import anorm._
import controllers._
import models.UserModel
import org.joda.time.DateTime
import org.mindrot.jbcrypt.BCrypt
import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.libs.json.Json._
import play.api.mvc._
import play.db._

object User extends Controller with Secured {

  val newForm = Form(
    mapping(
      "username" -> nonEmptyText,
      "password" -> nonEmptyText,
      "realName" -> nonEmptyText,
      "email"    -> email,
      "date_created" -> ignored(new DateTime())
    )(models.InitialUser.apply)(models.InitialUser.unapply)
  )

  val editForm = Form(
    mapping(
      "username" -> nonEmptyText,
      "realName" -> nonEmptyText,
      "email"    -> email
    )(models.EditUser.apply)(models.EditUser.unapply)
  )

  val passwordForm = Form(
    mapping(
      "password" -> nonEmptyText,
      "password2"-> nonEmptyText
    )(models.NewPassword.apply)(models.NewPassword.unapply)
    verifying("admin.user.password.match", np => { np.password.equals(np.password2) })
  )

  def add = IsAuthenticated { implicit request =>

    newForm.bindFromRequest.fold(
      errors => BadRequest(views.html.admin.user.create(errors)),
      value => {
        val user = UserModel.create(value)
        Redirect(routes.User.item(user.id.get)).flashing("success" -> "admin.user.add.success")
      }
    )
  }

  def create = IsAuthenticated { implicit request =>

    Ok(views.html.admin.user.create(newForm)(request))
  }

  def index(page: Int, count: Int) = IsAuthenticated { implicit request =>

    val users = UserModel.list(page = page, count = count)

    Ok(views.html.admin.user.index(users)(request))
  }

  def edit(userId: Long) = IsAuthenticated { implicit request =>

    val user = UserModel.getById(userId)

    user match {
      case Some(value) => {
        val editUser = new models.EditUser(value.username, value.realName, value.email)
        Ok(views.html.admin.user.edit(userId, editForm.fill(editUser), passwordForm)(request))
      }
      case None => NotFound
    }
  }

  def item(userId: Long) = IsAuthenticated { implicit request =>

    val user = UserModel.getById(userId)

    user match {
      case Some(value) => Ok(views.html.admin.user.item(value)(request))
      case None => NotFound
    }

  }

  def update(userId: Long) = IsAuthenticated { implicit request =>

    editForm.bindFromRequest.fold(
      errors => BadRequest(views.html.admin.user.edit(userId, errors, passwordForm)),
      {
        case user: models.EditUser =>
        UserModel.update(userId, user)
        Redirect(routes.User.item(userId)).flashing("success" -> "admin.user.edit.success")
      }
    )
  }

  def updatePassword(userId: Long) = IsAuthenticated { implicit request =>

    val user = UserModel.getById(userId)

    user match {
      case Some(value) => {
        passwordForm.bindFromRequest.fold(
          errors => {
            val editUser = new models.EditUser(value.username, value.realName, value.email)
            BadRequest(views.html.admin.user.edit(userId, editForm.fill(editUser), errors))
          }, {
            case np: models.NewPassword =>
            UserModel.updatePassword(userId, np)
            Redirect(routes.User.item(userId)).flashing("success" -> "admin.user.password.success")
          }
        )
      }
      case None => NotFound
    }
  }
}
