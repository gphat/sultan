package models

import anorm._
import anorm.SqlParser._
import java.util.Date
import play.api.db.DB
import play.api.Play.current
import scala.collection.mutable.ListBuffer
import royal.ends._

/**
 * Class for editing a ticket.  Eliminates fields that aren't useful when editing.
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
  val insertQuery = SQL("INSERT INTO changes (ticket_id, user_id, reporter_id, assignee_id, project_id, priority_id, severity_id, status_id, type_id, position, summary, description, date_created) VALUES ({ticket_id}, {user_id}, {reporter_id}, {assignee_id}, {project_id}, {priority_id}, {severity_id}, {status_id}, {type_id}, {position}, {summary}, {description}, UTC_TIMESTAMP())")
  val updateQuery = SQL("INSERT INTO changes (ticket_id, user_id, project_id, reporter_id, assignee_id, attention_id, priority_id, severity_id, status_id, type_id, resolution_id, proposed_resolution_id, position, summary, description, date_created) VALUES ({ticket_id}, {user_id}, {project_id}, {reporter_id}, {assignee_id}, {attention_id}, {priority_id}, {severity_id}, {status_id}, {type_id}, {resolution_id}, {proposed_resolution_id}, {position}, {summary}, {description}, UTC_TIMESTAMP())")
  val deleteQuery = SQL("DELETE FROM changes WHERE ticket_id={ticket_id}")

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
    get[Option[Date]]("date_begin") ~
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
   * Create a ticket.
   */
  def create(userId: Long, change: InitialChange): Option[Change] = {

    val project = ProjectModel.getById(ticket.projectId)

    // Fetch the starting status we should use for the project's workflow.
    val startingStatus = project match {
      case Some(x) => WorkflowModel.getStartingStatus(x.workflowId)
      case None => None
    }
    // XXX Should log something up there, really

    val result = startingStatus match {
      case Some(status) => {
        DB.withConnection { implicit conn =>

          // If these are missing? XXX
          val proj = ProjectModel.getById(ticket.projectId).get
          val tid = ProjectModel.getNextSequence(ticket.projectId).get

          val ticketId = proj.key + "-" + tid.toString
          val id = insertQuery.on(
            'ticket_id    -> ticketId,
            'user_id      -> userId,
            'reporter_id  -> ticket.reporterId,
            'assignee_id  -> ticket.assigneeId,
            'project_id   -> ticket.projectId,
            'priority_id  -> ticket.priorityId,
            'severity_id  -> ticket.severityId,
            'status_id    -> status.id,
            'type_id      -> ticket.typeId,
            'description  -> ticket.description,
            'position     -> ticket.position,
            'summary      -> ticket.summary
          ).executeInsert()
          val nt = this.getFullById(ticketId)

          nt.map { t =>
            SearchModel.indexTicket(ticket = t)

            SearchModel.indexEvent(Event(
              projectId     = t.project.id,
              projectName   = t.project.name,
              userId        = t.user.id,
              userRealName  = t.user.name,
              eKey          = t.ticketId,
              eType         = "ticket_create",
              content       = t.summary,
              url           = "",
              dateCreated   = t.dateCreated
            ))
          }
          nt
        }
      }
      case None => None
    }

    result
  }

  /**
   * Delete a ticket.
   */
  def delete(ticketId: String) {
    DB.withConnection { implicit conn =>
      deleteQuery.on('ticket_id -> ticketId).execute
    }
  }

  /**
   * Get a comment by id.
   */
  def getCommentById(id: Long) : Option[Comment] = {

    DB.withConnection { implicit conn =>
      getCommentByIdQuery.on('id -> id).as(comment.singleOpt)
    }
  }

  /**
   * Get ticket by ticketId.
   */
  def getById(id: String) : Option[EditTicket] = {

    DB.withConnection { implicit conn =>
      getByIdQuery.on('ticket_id -> id).as(editTicket.singleOpt)
    }
  }

  /**
   * Get ticket by ticketId.  This version returns the `FullTicket`.
   */
  def getFullById(id: String) : Option[FullTicket] = {

    DB.withConnection { implicit conn =>
      getFullByIdQuery.on('ticket_id -> id).as(fullTicket.singleOpt)
    }
  }

  def getAllCurrent: List[Ticket] = {

    DB.withConnection { implicit conn =>
      allQuery.as(ticket *)
    }
  }

  def getAllCurrentFull: List[FullTicket] = {
    DB.withConnection { implicit conn =>
      getAllCurrentQuery.as(fullTicket *)
    }
  }

  def getAllComments: List[Comment] = {

    DB.withConnection { implicit conn =>
      allCommentsQuery.as(comment *)
    }
  }

  def getAllFullById(id: String): List[FullTicket] = {

    DB.withConnection { implicit conn =>
      getAllFullByIdQuery.on('ticket_id -> id).as(fullTicket *)
    }
  }

  def getAllFullCountById(id: String): Long = {

    DB.withConnection { implicit conn =>
      getAllFullByIdCountQuery.on('ticket_id -> id).as(scalar[Long].single)
    }
  }

  def getCountByProject(projectId: Pk[Long]): Long = {

    DB.withConnection { implicit conn =>
      getCountByProjectQuery.on('project_id -> projectId).as(scalar[Long].single)
    }
  }

  def getCountOpenByProject(projectId: Pk[Long]): Long = {

    DB.withConnection { implicit conn =>
      getCountOpenByProjectQuery.on('project_id -> projectId).as(scalar[Long].single)
    }
  }

  def getCountByProjectAndStatus(projectId: Pk[Long], statusId: Long): Long = {

    DB.withConnection { implicit conn =>
      getByProjectAndStatusQuery.on('project_id -> projectId, 'status_id -> statusId).as(scalar[Long].single)
    }
  }

  def getCountTodayByProject(projectId: Pk[Long]): Long = {

    DB.withConnection { implicit conn =>
      getCountTodayByProjectQuery.on('project_id -> projectId).as(scalar[Long].single)
    }
  }

  def getCountThisWeekByProject(projectId: Pk[Long]): Long = {

    DB.withConnection { implicit conn =>
      getCountThisWeekByProjectQuery.on('project_id -> projectId).as(scalar[Long].single)
    }
  }

  /**
   * Get links for a ticket.
   */
  def getLinks(id: String): List[FullLink] = {

    DB.withConnection { implicit conn =>
      val links = getLinksQuery.on('ticket_id -> id).as(link *)

      links.map { link =>

        // XXX This sucks.  I would love to fix this, but I can't turn
        // the link query into a JOIN to the full_tickets view because it
        // hangs MySQL. Ugh. - gphat
        val parent = getFullById(link.parentId).get
        val child = getFullById(link.childId).get

        FullLink(
          id            = link.id,
          typeId        = link.typeId,
          typeName      = link.typeName,
          parentId      = link.parentId,
          parentSummary = parent.summary,
          parentResolutionId = parent.resolution.id,
          childId       = link.childId,
          childSummary  = child.summary,
          childResolutionId = child.resolution.id,
          dateCreated   = link.dateCreated
        )
      }
    }
  }

  /**
   * Get a FullLink by id.
   */
  def getFullLinkById(id: Long): Option[FullLink] = {

    DB.withConnection { implicit conn =>
      val maybeL = getLinkByIdQuery.on('id -> id).as(link.singleOpt)
      maybeL match {
        case Some(l) => {
          val parent = getFullById(l.parentId).get
          val child = getFullById(l.childId).get

          Some(FullLink(
            id            = l.id,
            typeId        = l.typeId,
            typeName      = l.typeName,
            parentId      = l.parentId,
            parentSummary = parent.summary,
            parentResolutionId = parent.resolution.id,
            childId       = l.childId,
            childSummary  = child.summary,
            childResolutionId = child.resolution.id,
            dateCreated   = l.dateCreated
          ))
        }
        case None => None
      }
    }
  }

  /**
   * Get a Link by id.
   */
  def getLinkById(id: Long): Option[Link] = {

    DB.withConnection { implicit conn =>
      getLinkByIdQuery.on('id -> id).as(link.singleOpt)
    }
  }

  /**
   * Link a child ticket to a parent with a type.
   */
  def link(linkTypeId: Long, parentId: String, childId: String): Option[FullLink] = {

    DB.withConnection { implicit conn =>
      val li = insertLinkQuery.on(
        'link_type_id     -> linkTypeId,
        'parent_ticket_id -> parentId,
        'child_ticket_id  -> childId
      ).executeInsert()
      li.map({ lid =>
        EmperorEventBus.publish(
          LinkTicketEvent(
            parentId = parentId,
            childId = childId
          )
        )
        getFullLinkById(lid)
      }).getOrElse(None)
    }
  }

  /**
   * Remove a link between tickets.
   */
  def removeLink(id: Long) {
    DB.withConnection { implicit conn =>
      val link = getFullLinkById(id)
      link.map({ l =>
        deleteLinkQuery.on('id -> id).execute()
        EmperorEventBus.publish(
          UnlinkTicketEvent(
            parentId = l.parentId,
            childId = l.childId
          )
        )
      })
    }
  }

  def list(page: Int = 1, count: Int = 10) : Page[Ticket] = {

    val offset = count * (page - 1)

    DB.withConnection { implicit conn =>
      val tickets = listQuery.on(
        'count  -> count,
        'offset -> offset
      ).as(ticket *)

      val totalRows = listCountQuery.as(scalar[Long].single)

      Page(tickets, page, count, totalRows)
    }
  }

  /**
   * Change the contents of a ticket. The resolution, status and clear resolution
   * provide the caller with the ability to manipulate these fields directly, as they
   * are special fields that do not normally get modified.  The `clearResolution` field
   * allows the clearing of a resolution.  The optional comment will add a comment in
   * addition to other changes.
   * Note that if there is no change, nothing will happen here.
   */
  def update(
    userId: Long, id: String, ticket: EditTicket,
    resolutionId: Option[Long] = None, statusId: Option[Long] = None,
    clearResolution: Boolean = false,
    comment: Option[String] = None
  ): FullTicket = {

    val user = UserModel.getById(userId).get

    val oldTicket = DB.withConnection { implicit conn =>
      this.getFullById(id).get
    }

    // This is a bit hinky, so some explanation is required.
    // We could get passed a new resolutionId.  If so then we are changing
    // the resolution.  But if we DON'T get one (None) then we could either
    // be leaving the resolution alone OR setting it to None.  To disambiguate
    // we use the clearResolution boolean.  If that is true then we will
    // set newResId to None (regladless of what resolutionId we might've gotten).
    val newResId = clearResolution match {
      case true   => None
      case false  => resolutionId.getOrElse(oldTicket.resolution.id)
    }

    val changed = if(oldTicket.project.id != ticket.projectId) {
      true
    } else if(oldTicket.priority.id != ticket.priorityId) {
      true
    } else if(oldTicket.resolution.id != newResId) {
      true
    } else if(oldTicket.proposedResolution.id != ticket.proposedResolutionId) {
      true
    } else if(oldTicket.reporter.id != ticket.reporterId) {
      true
    } else if(oldTicket.assignee.id != ticket.assigneeId) {
      true
    } else if(oldTicket.attention.id != ticket.attentionId) {
      true
    } else if(oldTicket.severity.id != ticket.severityId) {
      true
    } else if(oldTicket.status.id != statusId.getOrElse(oldTicket.status.id)) {
      true
    } else if(oldTicket.ttype.id != ticket.typeId) {
      true
    } else if(oldTicket.description != ticket.description) {
      true
    } else if(oldTicket.summary != ticket.summary) {
      true
    } else {
      false
    }

    if(changed) {
      // Only record something if a change was made.
      val tid = DB.withTransaction { implicit conn =>

        // XXX Project
        updateQuery.on(
          'ticket_id              -> id,
          'user_id                -> userId,
          'project_id             -> ticket.projectId,
          'reporter_id            -> ticket.reporterId,
          'assignee_id            -> ticket.assigneeId,
          'attention_id           -> ticket.attentionId,
          'priority_id            -> ticket.priorityId,
          'severity_id            -> ticket.severityId,
          'status_id              -> statusId.getOrElse(oldTicket.status.id),
          'type_id                -> ticket.typeId,
          'resolution_id          -> newResId,
          'proposed_resolution_id -> ticket.proposedResolutionId,
          'position               -> ticket.position,
          'description            -> ticket.description,
          'summary                -> ticket.summary
        ).executeInsert()

        // Add a comment, if we had one.
        comment.map { content =>
          val comm = addComment(ticketId = id, userId = userId, content = content)
          SearchModel.indexComment(comm.get)
        }

        // Get on the bus!
        EmperorEventBus.publish(
          ChangeTicketEvent(
            ticketId = id,
            // This logic should probably be tested… XXX
            resolved = if(changed && (oldTicket.resolution.id != newResId)) true else false,
            unresolved = if(clearResolution && !oldTicket.resolution.id.isEmpty) true else false
          )
        )
      }

      val newTicket = DB.withConnection { implicit conn =>
        getFullById(id).get
      }

      if(changed) {
        SearchModel.indexHistory(newTick = newTicket, oldTick = oldTicket)
      }
      newTicket
    } else {
      oldTicket
    }
  }
}
