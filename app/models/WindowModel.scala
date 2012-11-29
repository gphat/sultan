package models

import anorm._
import anorm.SqlParser._
import org.joda.time.{DateTime,DateTimeZone}
import play.api.db.DB
import play.api.Play.current
import sultan._
import sultan.AnormExtension._

/**
 * Class for a Window
 */
case class Window(
  id: Pk[Long] = NotAssigned,
  ownerId: Long,
  windowTypeId: Long,
  name: String,
  description: Option[String],
  // The date the window was begun
  dateBegun: DateTime,
  // The date the window was ended
  dateEnded: DateTime,
  // The date this window was created
  dateCreated: DateTime
)

object WindowModel {

  val allQuery = SQL("SELECT * FROM windows")
  val getByIdQuery = SQL("SELECT * FROM windows WHERE id={id}")
  val listQuery = SQL("SELECT * FROM windows LIMIT {offset},{count}")
  val listCountQuery = SQL("SELECT count(*) FROM windows")
  val insertQuery = SQL("INSERT INTO windows (owner_id, window_type_id, name, description, date_begun, date_ended, date_created) VALUES ({owner_id}, {window_type_id}, {name}, {description}, {summary}, {date_begun}, {date_ended}, UTC_TIMESTAMP())")
  val updateQuery = SQL("UPDATE windows SET owner_id={owner_id}, window_type_id={window_type_id}, name={name}, description={description}, date_begun={date_begun}, date_ended={date_ended} WHERE id={id}")
  val deleteQuery = SQL("DELETE FROM windows WHERE id={id}")

  // parser for retrieving a window
  val window = {
    get[Pk[Long]]("id") ~
    get[Long]("owner_id") ~
    get[Long]("window_type_id") ~
    get[String]("name") ~
    get[Option[String]]("description") ~
    get[DateTime]("date_begun") ~
    get[DateTime]("date_ended") ~
    get[DateTime]("date_created") map {
      case id~ownerId~windowTypeId~name~description~dateBegun~dateEnded~dateCreated => Window(
        id = id,
        ownerId = ownerId,
        windowTypeId = windowTypeId,
        name = name,
        description = description,
        dateBegun = dateBegun,
        dateEnded = dateEnded,
        dateCreated = dateCreated
      )
    }
  }

  /**
   * Create a window.
   */
  def create(userId: Long, window: Window): Option[Window] = {

    UserModel.getById(userId).map({ user =>

      DB.withConnection { implicit conn =>

        insertQuery.on(
          'owner_id     -> window.ownerId,
          'window_type_id -> window.windowTypeId,
          'name         -> window.name,
          'description  -> window.description,
          'date_begun   -> window.dateBegun.withZoneRetainFields(DateTimeZone.forID(user.timeZone)),
          'date_ended   -> window.dateEnded.withZoneRetainFields(DateTimeZone.forID(user.timeZone))
        ).executeInsert().map({ id =>
          this.getById(id)
        }).getOrElse(None)
      }
    }).getOrElse(None)
  }

  /**
   * Delete a window.
   */
  def delete(id: Long) {
    DB.withConnection { implicit conn =>
      deleteQuery.on('id -> id).execute
    }
  }

  def getAll: List[Window] = {
    DB.withConnection { implicit conn =>
      allQuery.as(window *)
    }
  }

  /**
   * Get window by id.
   */
  def getById(id: Long) : Option[Window] = {

    DB.withConnection { implicit conn =>
      getByIdQuery.on('id -> id).as(window.singleOpt)
    }
  }

  def list(page: Int = 1, count: Int = 10) : Page[Window] = {

    val offset = count * (page - 1)

    DB.withConnection { implicit conn =>
      val windows = listQuery.on(
        'count  -> count,
        'offset -> offset
      ).as(window *)

      val totalRows = listCountQuery.as(scalar[Long].single)

      Page(windows, page, count, totalRows)
    }
  }

  def update(userId: Long, id: Long, window: Window): Option[Window] = {

    UserModel.getById(userId).map({ user =>

      DB.withConnection { implicit conn =>
        updateQuery.on(
          'id             -> id,
          'owner_id       -> window.ownerId,
          'window_type_id -> window.windowTypeId,
          'name           -> window.name,
          'description    -> window.description,
          'date_begun     -> window.dateBegun.withZoneRetainFields(DateTimeZone.forID(user.timeZone)),
          'date_ended     -> window.dateEnded.withZoneRetainFields(DateTimeZone.forID(user.timeZone))
        ).execute
        getById(id)
      }
    }).getOrElse(None)
  }
}
