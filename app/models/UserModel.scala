package models

import anorm._
import anorm.SqlParser._
import java.util.Date
import org.mindrot.jbcrypt.BCrypt
import play.api.db.DB
import play.api.i18n.Messages
import play.api.Play.current
import sultan._

case class User(id: Pk[Long] = NotAssigned, username: String, password: String, realName: String, email: String, dateCreated: Date) {
  def isAnonymous = username.equals("anonymous")
}

case class EditUser(username: String, realName: String, email: String)

case class InitialUser(username: String, password: String, realName: String, email: String, dateCreated: Date)

case class LoginUser(username: String, password: String)

case class NewPassword(password: String, password2: String)

object UserModel {

  val allQuery = SQL("SELECT * FROM users")
  val getByIdQuery = SQL("SELECT * FROM users WHERE id={id}")
  val getByGroupIdQuery = SQL("SELECT * FROM users")
  val getByUsernameQuery = SQL("SELECT * FROM users WHERE username={username}")
  val listQuery = SQL("SELECT * FROM users ORDER BY username LIMIT {offset},{count}")
  val listCountQuery = SQL("SELECT count(*) FROM users")
  val insertQuery = SQL("INSERT INTO users (username, password, realname, email, date_created) VALUES ({username}, {password}, {realname}, {email}, UTC_TIMESTAMP())")
  val startsWithQuery = SQL("SELECT * FROM users WHERE username COLLATE utf8_unicode_ci LIKE {username}")
  val updateQuery = SQL("UPDATE users SET username={username}, realname={realname}, email={email} WHERE id={id}")
  val updatePassQuery = SQL("UPDATE users SET password={password} WHERE id={id}")
  val deleteQuery = SQL("DELETE FROM users WHERE id={id}")

  val user = {
    get[Pk[Long]]("id") ~
    get[String]("username") ~
    get[String]("password") ~
    get[String]("realName") ~
    get[String]("email") ~
    get[Date]("date_created") map {
      case id~username~password~realName~email~dateCreated => User(id, username, password, realName, email, dateCreated)
    }
  }

  /**
   * Add a user.  Uses `InitialUser`.
   */
  def create(user: InitialUser): User = {

    DB.withConnection { implicit conn =>
      val id = insertQuery.on(
        'username   -> user.username,
        'password   -> BCrypt.hashpw(user.password, BCrypt.gensalt(12)),
        'realname   -> user.realName,
        'email      -> user.email
      ).executeInsert()

      this.getById(id.get).get
    }
  }

  /**
   * Delete a user.
   */
  def delete(id: Long) {
    DB.withConnection { implicit conn =>
      deleteQuery.on(
        'id -> id
      ).execute
    }
  }

  /**
   * Retrieve a user by id.
   */
  def getById(id: Long) : Option[User] = {

    DB.withConnection { implicit conn =>
      getByIdQuery.on('id -> id).as(UserModel.user.singleOpt)
    }
  }

  def getAll: List[User] = {

    DB.withConnection { implicit conn =>
      allQuery.as(user *)
    }
  }

  def getByUsername(username: String) : Option[User] = {

    DB.withConnection { implicit conn =>
      getByUsernameQuery.on('username -> username).as(UserModel.user.singleOpt)
    }
  }

  /**
   * Find all users starting with a specific string. Used for
   * autocomplete.
   */
  def getStartsWith(query: String) : Seq[User] = {

    val likeQuery = query + "%"

    DB.withConnection { implicit conn =>
      startsWithQuery.on(
        'username -> likeQuery
      ).as(user *)
    }
  }

  def list(page: Int = 1, count: Int = 10) : Page[User] = {

      val offset = count * (page - 1)

      DB.withConnection { implicit conn =>
        val users = listQuery.on(
          'count  -> count,
          'offset -> offset
        ).as(user *)

        val totalRows = listCountQuery.as(scalar[Long].single)

        Page(users, page, count, totalRows)
      }
  }

  def update(id: Long, user: EditUser): Option[User] = {

    DB.withConnection { implicit conn =>
      updateQuery.on(
        'id         -> id,
        'username   -> user.username,
        'realname   -> user.realName,
        'email      -> user.email
      ).execute
      getById(id)
    }
  }

  def updatePassword(id: Long, np: NewPassword) = {

    val hashedPass = BCrypt.hashpw(np.password, BCrypt.gensalt(12))

    DB.withConnection { implicit conn =>
      val foo = updatePassQuery.on(
        'id         -> id,
        'password   -> hashedPass
      ).executeUpdate
    }
  }
}