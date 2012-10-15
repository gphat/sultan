package models

import anorm._
import anorm.SqlParser._
import java.util.Date
import play.api.db.DB
import play.api.Play.current
import royal.ends._

/**
 * Class for a change type.
 */
case class ChangeType(
  id: Pk[Long] = NotAssigned,
  name: String,
  color: String
)

object ChangeTypeModel {

  val allQuery = SQL("SELECT * FROM change_types")
  val getByIdQuery = SQL("SELECT * FROM change_types WHERE id={id}")
  val listQuery = SQL("SELECT * FROM change_types LIMIT {offset},{count}")
  val listCountQuery = SQL("SELECT count(*) FROM change_types")
  val insertQuery = SQL("INSERT INTO change_types (name, date_created) VALUES ({name}, UTC_TIMESTAMP())")
  val updateQuery = SQL("UPDATE change_types SET name={name}, color={color} WHERE id={id}")
  val deleteQuery = SQL("DELETE FROM change_types WHERE id={id}")

  // Parser for retrieving a change type.
  val changeType = {
    get[Pk[Long]]("id") ~
    get[String]("name") ~
    get[String]("color") map {
      case id~name~color => ChangeType(
        id = id, name = name, color = color
      )
    }
  }

  /**
   * Create a change type.
   */
  def create(ct: ChangeType): ChangeType = {

    DB.withConnection { implicit conn =>
      val id = insertQuery.on(
        'name     -> ct.name,
        'color    -> ct.color
      ).executeInsert()

      this.getById(id.get).get
    }
  }

  /**
   * Delete change type.
   */
  def delete(id: Long) {
    DB.withConnection { implicit conn =>
      deleteQuery.on('id -> id).execute
    }
  }

  /**
   * Get all change type
   */
  def getAll: List[ChangeType] = {
    DB.withConnection { implicit conn =>
      allQuery.as(changeType *)
    }
  }

  /**
   * Get a change type by id.
   */
  def getById(id: Long) : Option[ChangeType] = {

    DB.withConnection { implicit conn =>
      getByIdQuery.on('id -> id).as(changeType.singleOpt)
    }
  }

  def list(page: Int = 1, count: Int = 10) : Page[ChangeType] = {

      val offset = count * (page - 1)

      DB.withConnection { implicit conn =>
        val cts = listQuery.on(
          'count  -> count,
          'offset -> offset
        ).as(changeType *)

        val totalRows = listCountQuery.as(scalar[Long].single)

        Page(cts, page, count, totalRows)
      }
  }

  /**
   * Update a change type.
   */
  def update(id: Long, ct: ChangeType): Option[ChangeType] = {

    DB.withConnection { implicit conn =>
      updateQuery.on(
        'id         -> id,
        'name       -> ct.name,
        'color      -> ct.color
      ).execute

      getById(id)
    }
  }
}
