package controllers

import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.Logger
import play.api.mvc._
import models._
import org.mindrot.jbcrypt.BCrypt

object Auth extends Controller {

  val loginForm = Form(
    mapping(
      "username" -> nonEmptyText,
      "password" -> nonEmptyText
    )(LoginUser.apply)(LoginUser.unapply)
    // XXX This whole login block could be replaced by a single method that checks everything and returns a boolean
    // XXX could eliminate one of these by combining, reducing one of the queries
    .verifying("auth.failure", params => !params.username.equalsIgnoreCase("anonymous"))
    .verifying("auth.failure", params => {
      val maybeUser = UserModel.getByUsername(params.username)
      maybeUser match {
        case Some(user) => {
          BCrypt.checkpw(params.password, user.password) == true
        }
        case None => false
      }
    })
  )

  def login(redirectUrl: String = "/") = Action { implicit request =>

    Ok(views.html.auth.login(loginForm, redirectUrl)(request))
  }

  def logout = Action { implicit request =>

    Redirect(routes.Auth.login()).withNewSession.flashing("error" -> "auth.logout.success")
  }

  def doLogin(redirectUrl: String = "/") = Action { implicit request =>

    loginForm.bindFromRequest.fold(
      errors => {
        BadRequest(views.html.auth.login(errors, redirectUrl)(request))
      }, {
        case loginUser => {

          val user = UserModel.getByUsername(loginUser.username).get // We know this exists, so just get it

          Redirect(redirectUrl).withSession("user_id" -> user.id.get.toString).flashing("success" -> "auth.success")
        }
      }
    )
  }
}

case class AuthenticatedRequest(
  val user: User, request: Request[AnyContent]
) extends WrappedRequest(request)

/**
 * Provide security features
 */
trait Secured {

  /**
   * Redirect to login if the user in not authenticated.
   */
  private def onUnauthenticated(request: RequestHeader) = Results.Redirect("/auth/login", Map("redirectUrl" -> Seq(request.uri))).flashing("error" -> "auth.mustlogin")

  /**
   * Redirect to index if the user in not authenticated.
   */
  private def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.Core.index).flashing("error" -> "auth.notauthorized")

  /**
   * Action for verifying authentication and authorization of users.
   */
  def IsAuthenticated(f: AuthenticatedRequest => Result) = {
    Action { request =>

      // First grab the user_id from the session, maybe
      val maybeUserId = request.session.get("user_id")

      maybeUserId match {
        case Some(userId) => {
          val maybeUser = UserModel.getById(userId.toLong)
          maybeUser match {
            case Some(user) => f(AuthenticatedRequest(user, request))
            case None => onUnauthenticated(request)
          }
        }
        case None => onUnauthenticated(request)
      }


    }
  }
}
