package models

import anorm._
import anorm.SqlParser._
import java.util.Date
import play.api.db.DB
import play.api.Play.current
import scala.collection.mutable.ListBuffer
import royal.ends._


/**
 * Class for an "initial" Change
 */
case class InitialChange(
  userId: Long, ownerId: Long, changeTypeId: Long, duration: Long, risk: Int,
  summary: String,  description: Option[String], dateCreated: Date,
  dateScheduled: Date
)

/**
 * Class for a Change
 */
case class Change(
  id: Pk[Long] = NotAssigned, userId: Long, ownerId: Long,
  changeTypeId: Long, duration: Long, risk: Int, summary: String,
  description: Option[String], notes: Option[String], dateBegun: Option[Date],
  dateClosed: Option[Date], dateCompleted: Option[Date], dateCreated: Date,
  dateScheduled: Date, success: Boolean
)

object ChangeModel {

  val allQuery = SQL("SELECT * FROM changes")
  val getByIdQuery = SQL("SELECT * FROM changes WHERE id={change_id}")
  val listQuery = SQL("SELECT * FROM changes LIMIT {offset},{count}")
  val listCountQuery = SQL("SELECT count(*) FROM changes")
  val insertQuery = SQL("INSERT INTO changes (user_id, owner_id, change_type_id, duration, risk, summary, description, date_scheduled, date_created) VALUES ({user_id}, {owner_id}, {change_type_id}, {duration}, {risk}, {summary}, {description}, {date_scheduled}, UTC_TIMESTAMP())")
  val updateQuery = SQL("INSERT INTO changes (ticket_id, user_id, project_id, reporter_id, assignee_id, attention_id, priority_id, severity_id, status_id, type_id, resolution_id, proposed_resolution_id, position, summary, description, date_created) VALUES ({ticket_id}, {user_id}, {project_id}, {reporter_id}, {assignee_id}, {attention_id}, {priority_id}, {severity_id}, {status_id}, {type_id}, {resolution_id}, {proposed_resolution_id}, {position}, {summary}, {description}, UTC_TIMESTAMP())")
  val deleteQuery = SQL("DELETE FROM changes WHERE id={id}")

  // parser for retrieving a ticket
  val change = {
    get[Pk[Long]]("id") ~
    get[Long]("user_id") ~
    get[Long]("ownerId") ~
    get[Long]("changeTypeId") ~
    get[Long]("duration") ~
    get[Int]("risk") ~
    get[String]("summary") ~
    get[Option[String]]("description") ~
    get[Option[String]]("notes") ~
    get[Option[Date]]("date_begun") ~
    get[Option[Date]]("date_closed") ~
    get[Option[Date]]("date_completed") ~
    get[Date]("date_created") ~
    get[Date]("date_scheduled") ~
    get[Boolean]("success") map {
      case id~userId~ownerId~changeTypeId~duration~risk~summary~description~notes~dateBegun~dateClosed~dateCompleted~dateCreated~dateScheduled~success => Change(
        id = id,
        userId = userId,
        ownerId = ownerId,
        changeTypeId = changeTypeId,
        duration = duration,
        risk = risk,
        summary = summary,
        description = description,
        notes = notes,
        dateBegun = dateBegun,
        dateClosed = dateClosed,
        dateCompleted = dateCompleted,
        dateCreated = dateCreated,
        dateScheduled = dateScheduled,
        success = success
      )
    }
  }

  /**
   * Create a change.
   */
  def create(userId: Long, change: InitialChange): Option[Change] = {

    DB.withConnection { implicit conn =>

      val maybeId = insertQuery.on(
        'user_id      -> userId,
        'owner_id     -> change.ownerId,
        'change_type_id -> change.changeTypeId,
        'duration     -> change.duration,
        'risk         -> change.risk,
        'summary      -> change.summary,
        'description  -> change.description,
        'date_scheduled -> change.dateScheduled
      ).executeInsert()
      maybeId.map({ id => this.getById(id) }).getOrElse(None)
    }
  }

  /**
   * Delete a change.
   */
  def delete(id: Long) {
    DB.withConnection { implicit conn =>
      deleteQuery.on('id -> id).execute
    }
  }

  /**
   * Get change by id.
   */
  def getById(id: Long) : Option[Change] = {

    DB.withConnection { implicit conn =>
      getByIdQuery.on('id -> id).as(change.singleOpt)
    }
  }

  def list(page: Int = 1, count: Int = 10) : Page[Change] = {

    val offset = count * (page - 1)

    DB.withConnection { implicit conn =>
      val changes = listQuery.on(
        'count  -> count,
        'offset -> offset
      ).as(change *)

      val totalRows = listCountQuery.as(scalar[Long].single)

      Page(changes, page, count, totalRows)
    }
  }
}
