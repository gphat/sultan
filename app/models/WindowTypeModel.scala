package models

import anorm._
import anorm.SqlParser._
import org.joda.time.{DateTime,DateTimeZone}
import play.api.db.DB
import play.api.Play.current
import sultan._
import sultan.AnormExtension._

/**
 * Class for a WindowType
 */
case class WindowType(
  id: Pk[Long] = NotAssigned,
  name: String,
  threshold: Int,
  color: String
)

object WindowTypeModel {

  val allQuery = SQL("SELECT * FROM window_types")
  val getByIdQuery = SQL("SELECT * FROM window_types WHERE id={id}")
  val listQuery = SQL("SELECT * FROM window_types LIMIT {offset},{count}")
  val listCountQuery = SQL("SELECT count(*) FROM window_types")
  val insertQuery = SQL("INSERT INTO window_types (name, threshold) VALUES ({name}, {threshold})")
  val updateQuery = SQL("UPDATE window_types SET name={name}, threshold={threshold} WHERE id={id}")
  val deleteQuery = SQL("DELETE FROM window_types WHERE id={id}")

  // parser for retrieving a window
  val windowType = {
    get[Pk[Long]]("id") ~
    get[String]("name") ~
    get[Int]("threshold") ~
    get[String]("color") map {
      case id~name~threshold~color => WindowType(
        id = id,
        name = name,
        threshold = threshold,
        color = color
      )
    }
  }

  /**
   * Create a window type.
   */
  def create(wt: WindowType): Option[WindowType] = {

    DB.withConnection { implicit conn =>

      insertQuery.on(
        'name         -> wt.name,
        'treshold     -> wt.threshold,
        'color        -> wt.color
      ).executeInsert().map({ id =>
        this.getById(id)
      }).getOrElse(None)
    }
  }

  /**
   * Delete a window type.
   */
  def delete(id: Long) {
    DB.withConnection { implicit conn =>
      deleteQuery.on('id -> id).execute
    }
  }

  def getAll: List[WindowType] = {
    DB.withConnection { implicit conn =>
      allQuery.as(windowType *)
    }
  }

  /**
   * Get window type by id.
   */
  def getById(id: Long) : Option[WindowType] = {

    DB.withConnection { implicit conn =>
      getByIdQuery.on('id -> id).as(windowType.singleOpt)
    }
  }

  def list(page: Int = 1, count: Int = 10) : Page[WindowType] = {

    val offset = count * (page - 1)

    DB.withConnection { implicit conn =>
      val windowTypes = listQuery.on(
        'count  -> count,
        'offset -> offset
      ).as(windowType *)

      val totalRows = listCountQuery.as(scalar[Long].single)

      Page(windowTypes, page, count, totalRows)
    }
  }

  def update(id: Long, wt: WindowType): Option[WindowType] = {

    DB.withConnection { implicit conn =>
      updateQuery.on(
        'id             -> id,
        'name           -> wt.name,
        'threshold      -> wt.threshold,
        'color          -> wt.color
      ).execute
      getById(id)
    }
  }
}
