package models

import anorm._
import anorm.SqlParser._
import java.util.Date
import play.api.db.DB
import play.api.Play.current
import royal.ends._

/**
 * Class for a system.
 */
case class System(
  id: Pk[Long] = NotAssigned,
  name: String,
  dateCreated: Date
)

object SystemModel {

  val allQuery = SQL("SELECT * FROM systems")
  val getByIdQuery = SQL("SELECT * FROM systems WHERE id={id}")
  val listQuery = SQL("SELECT * FROM systems LIMIT {offset},{count}")
  val listCountQuery = SQL("SELECT count(*) FROM systems")
  val insertQuery = SQL("INSERT INTO systems (name, date_created) VALUES ({name}, UTC_TIMESTAMP())")
  val updateQuery = SQL("UPDATE systems SET name={name} WHERE id={id}")
  val deleteQuery = SQL("DELETE FROM systems WHERE id={id}")

  // Parser for retrieving a system.
  val system = {
    get[Pk[Long]]("id") ~
    get[String]("name") ~
    get[Date]("date_created") map {
      case id~name~dateCreated => System(
        id = id, name = name, dateCreated = dateCreated
      )
    }
  }

  /**
   * Create a system.
   */
  def create(system: System): System = {

    DB.withConnection { implicit conn =>
      val id = insertQuery.on(
        'name         -> system.name
      ).executeInsert()

      this.getById(id.get).get
    }
  }

  /**
   * Delete system.
   */
  def delete(id: Long) {
    DB.withConnection { implicit conn =>
      deleteQuery.on('id -> id).execute
    }
  }

  /**
   * Get all systems
   */
  def getAll(userId: Long): List[System] = {
    DB.withConnection { implicit conn =>
      allQuery.as(system *)
    }
  }

  /**
   * Get a system by id.
   */
  def getById(id: Long) : Option[System] = {

    DB.withConnection { implicit conn =>
      getByIdQuery.on('id -> id).as(system.singleOpt)
    }
  }

  def list(page: Int = 1, count: Int = 10) : Page[System] = {

      val offset = count * (page - 1)

      DB.withConnection { implicit conn =>
        val systems = listQuery.on(
          'count  -> count,
          'offset -> offset
        ).as(system *)

        val totalRows = listCountQuery.as(scalar[Long].single)

        Page(systems, page, count, totalRows)
      }
  }

  /**
   * Update a system.
   */
  def update(id: Long, system: System): Option[System] = {

    DB.withConnection { implicit conn =>
      updateQuery.on(
        'id         -> id,
        'name       -> system.name
      ).execute

      getById(id)
    }
  }
}
