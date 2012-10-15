package sultan

import anorm.Id
import java.text.SimpleDateFormat
import java.util.Date
import models._
import org.clapper.markwrap._
import play.api.i18n.Messages
import play.api.libs.json.Json._
import play.api.libs.json._

object JsonFormats {

  val dateFormatter = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'")
  val markdown = MarkWrap.parserFor(MarkupType.Markdown)

  private def optionLongtoJsValue(maybeId: Option[Long]) = maybeId.map({ l => JsNumber(l) }).getOrElse(JsNull)

  private def optionI18nStringtoJsValue(maybeId: Option[String]) = maybeId.map({ s => JsString(Messages(s)) }).getOrElse(JsNull)
  private def optionStringtoJsValue(maybeId: Option[String]) = maybeId.map({ s => JsString(s) }).getOrElse(JsNull)

  // XXX UNIT TESTS FOR THE LOVE OF GOD

  /**
   * JSON conversion for TicketPriority
   */
  implicit object TicketPriorityFormat extends Format[TicketPriority] {
    def reads(json: JsValue): TicketPriority = TicketPriority(
      id = Id((json \ "id").as[Long]),
      name = (json \ "name").as[String],
      color = (json \ "color").as[String],
      position = (json \ "position").as[Int],
      dateCreated = new Date() // XXX
    )

    def writes(obj: TicketPriority): JsValue = {
      val doc: Map[String,JsValue] = Map(
        "id"            -> JsNumber(obj.id.get),
        "name"          -> JsString(obj.name),
        "color"         -> JsString(obj.color),
        "position"      -> JsNumber(obj.position),
        "date_created"  -> JsString(dateFormatter.format(obj.dateCreated))
      )
      toJson(doc)
    }
  }

  /**
   * JSON conversion for Comment
   */
  implicit object CommentFormat extends Format[Comment] {
    def reads(json: JsValue): Comment = Comment(
      id = Id((json \ "id").as[Long]),
      userId = (json \ "user_id").as[Long],
      username = (json \ "user_name").as[String],
      realName = (json \ "user_realname").as[String],
      ticketId = (json \ "ticket_id").as[String],
      content = (json \ "content").as[String],
      dateCreated = new Date() // XXX
    )

    def writes(comment: Comment): JsValue = {
      val cdoc: Map[String,JsValue] = Map(
        "id"            -> JsNumber(comment.id.get),
        "ticket_id"     -> JsString(comment.ticketId),
        "user_id"       -> JsNumber(comment.userId),
        "user_name"     -> JsString(comment.username),
        "user_realname" -> JsString(comment.realName),
        "content"       -> JsString(comment.content),
        "date_created"  -> JsString(dateFormatter.format(comment.dateCreated))
      )
      toJson(cdoc)
    }
  }

  implicit object EditTicketFormat extends Format[EditTicket] {
    def reads(json: JsValue): EditTicket = EditTicket(
      ticketId      = Id((json \ "ticket_id").as[String]),
      reporterId    = (json \ "reporter_id").as[Long],
      assigneeId    = (json \ "assignee_id").as[Option[Long]],
      priorityId    = (json \ "priority_id").as[Long],
      projectId     = (json \ "project_id").as[Long],
      resolutionId  = (json \ "resolution_id").as[Option[Long]],
      severityId    = (json \ "severity_id").as[Long],
      typeId        = (json \ "type_id").as[Long],
      summary       = (json \ "summary").as[String],
      description   = (json \ "description").as[Option[String]],
      attentionId    = (json \ "attention_id").as[Option[Long]],
      position      = (json \ "position").as[Option[Long]],
      proposedResolutionId = (json \ "proposed_resolution_id").as[Option[Long]]
    )

    def writes(ticket: EditTicket): JsValue = {

      val tdoc: Map[String,JsValue] = Map(
        "ticket_id"   -> JsString(ticket.ticketId.get),
        "reporter_id" -> JsNumber(ticket.reporterId),
        "assignee_id" -> optionLongtoJsValue(ticket.assigneeId),
        "priority_id" -> JsNumber(ticket.priorityId),
        "resolution_id" -> optionLongtoJsValue(ticket.resolutionId),
        "proposed_resolution_id" -> optionLongtoJsValue(ticket.proposedResolutionId),
        "severity_id" -> JsNumber(ticket.severityId),
        "type_id"     -> JsNumber(ticket.typeId),
        "position"    -> optionLongtoJsValue(ticket.position),
        "summary"     -> JsString(ticket.summary),
        "description" -> optionStringtoJsValue(ticket.description)
      )
      toJson(tdoc)
    }
  }

  /**
   * JSON conversion for Event
   */
  implicit object EventFormat extends Format[Event] {
    def reads(json: JsValue): Event = Event(
      projectId = (json \ "project_id").as[Long],
      projectName = (json \ "project_name").as[String],
      userId = (json \ "user_id").as[Long],
      userRealName = (json \ "user_realname").as[String],
      eKey = (json \ "ekey").as[String],
      eType = (json \ "etype").as[String],
      content = (json \ "content").as[String],
      url = (json \ "url").as[String],
      dateCreated = new Date() // XXX
    )

    def writes(event: Event): JsValue = {
      val edoc: Map[String,JsValue] = Map(
        "project_id"    -> JsNumber(event.projectId),
        "project_name"  -> JsString(event.projectName),
        "user_id"       -> JsNumber(event.userId),
        "user_realname" -> JsString(event.userRealName),
        "ekey"          -> JsString(event.eKey),
        "etype"         -> JsString(event.eType),
        "content"       -> JsString(event.content),
        "url"           -> JsString(event.url),
        "date_created"  -> JsString(dateFormatter.format(event.dateCreated))
      )
      toJson(edoc)
    }
  }

  /**
   * JSON conversion for FullLink
   */
  implicit object FullLinkFormat extends Format[FullLink] {

    def reads(json: JsValue): FullLink = FullLink(
      id          = Id((json \ "id").as[Long]),
      typeId      = (json \ "type_id").as[Long],
      typeName    = (json \ "name").as[String],
      parentId    = (json \ "parent_id").as[String],
      parentResolutionId = (json \ "parent_resolution_id").as[Option[Long]],
      parentSummary = (json \ "parent_summary").as[String],
      childId     = (json \ "parent_id").as[String],
      childResolutionId = (json \ "child_resolution_id").as[Option[Long]],
      childSummary = (json \ "child_summary").as[String],
      dateCreated = new Date() // XXX
    )

    def writes(l: FullLink): JsValue = {

      val childRes = l.childResolutionId match {
        case Some(reso) => JsNumber(reso)
        case None => JsNull
      }

      val parentRes = l.parentResolutionId match {
        case Some(reso) => JsNumber(reso)
        case None => JsNull
      }

      val ldoc: Map[String,JsValue] = Map(
        "id"              -> JsNumber(l.id.get),
        "type_id"         -> JsNumber(l.typeId),
        "name"            -> JsString(l.typeName),
        "name_i18n"       -> JsString(Messages(l.typeName)),
        "parent_id"       -> JsString(l.parentId),
        "parent_resolution_id" -> parentRes,
        "parent_summary"  -> JsString(l.parentSummary),
        "child_id"        -> JsString(l.childId),
        "child_resolution_id" -> childRes,
        "child_summary"  -> JsString(l.childSummary),
        "date_created"    -> JsString(dateFormatter.format(l.dateCreated))
      )
      toJson(ldoc)
    }
  }

  /**
   * JSON conversion for FullTicket
   */
  implicit object FullTicketFormat extends Format[FullTicket] {

    // XX This is allllll wrong.  To be able to inflate it should verify
    // all these IDs then poll the database for the values.
    def reads(json: JsValue): FullTicket = FullTicket(
      id        = Id((json \ "id").as[Long]),
      ticketId  = (json \ "ticket_id").as[String],
      user      = NamedThing(
        id    = (json \ "user_id").as[Long],
        name  = (json \ "user_name").as[String]
      ),
      reporter  = NamedThing(
        id    = (json \ "reporter_id").as[Long],
        name  = (json \ "reporter_name").as[String]
      ),
      assignee = OptionalNamedThing(
        id    = (json \ "assignee_id").as[Option[Long]],
        name  = (json \ "assignee_name").as[Option[String]]
      ),
      attention = OptionalNamedThing(
        id    = (json \ "attention_id").as[Option[Long]],
        name  = (json \ "attention_name").as[Option[String]]
      ),
      project  = NamedThing(
        id    = (json \ "project_id").as[Long],
        name  = (json \ "project_name").as[String]
      ),
      priority  = ColoredPositionedThing(
        id    = (json \ "priority_id").as[Long],
        name  = (json \ "priority_name").as[String],
        color = (json \ "priority_color").as[String],
        position = (json \ "priority_position").as[Int]
      ),
      resolution = OptionalNamedThing(
        id    = (json \ "resolution_id").as[Option[Long]],
        name  = (json \ "resolution_name").as[Option[String]]
      ),
      proposedResolution = OptionalNamedThing(
        id    = (json \ "proposed_resolution_id").as[Option[Long]],
        name  = (json \ "proposed_resolution_name").as[Option[String]]
      ),
      severity  = ColoredPositionedThing(
        id    = (json \ "severity_id").as[Long],
        name  = (json \ "severity_name").as[String],
        color = (json \ "severity_color").as[String],
        position = (json \ "severity_position").as[Int]
      ),
      workflowStatusId = (json \ "workflow_status_id").as[Long],
      status  = NamedThing(
        id    = (json \ "status_id").as[Long],
        name  = (json \ "status_name").as[String]
      ),
      ttype  = ColoredThing(
        id    = (json \ "type_id").as[Long],
        name  = (json \ "type_name").as[String],
        color = (json \ "type_color").as[String]
      ),
      position = (json \ "position").as[Option[Long]],
      summary = (json \ "summary").as[String],
      description = (json \ "description").as[Option[String]],
      dateCreated = new Date() // XXX
    )

    def writes(ticket: FullTicket): JsValue = {

      val tdoc: Map[String,JsValue] = Map(
        "id"              -> JsNumber(ticket.id.get),
        "ticket_id"       -> JsString(ticket.ticketId),
        "project_id"      -> JsNumber(ticket.project.id),
        "project_name"    -> JsString(ticket.project.name),
        "priority_id"     -> JsNumber(ticket.priority.id),
        "priority_name"   -> JsString(ticket.priority.name),
        "priority_name_i18n" -> JsString(Messages(ticket.priority.name)),
        "priority_color"  -> JsString(ticket.priority.color),
        "priority_position" -> JsNumber(ticket.priority.position),
        "resolution_id"   -> optionLongtoJsValue(ticket.resolution.id),
        // A ticket with no resolution gets a default name, hence the differing logic here
        "resolution_name" -> JsString(ticket.resolution.name.getOrElse("TICK_RESO_UNRESOLVED")),
        "resolution_name_i18n" -> JsString(Messages(ticket.resolution.name.getOrElse("TICK_RESO_UNRESOLVED"))),
        "proposed_resolution_id" -> optionLongtoJsValue(ticket.proposedResolution.id),
        "proposed_resolution_name" -> optionStringtoJsValue(ticket.proposedResolution.name),
        "proposed_resolution_name_i18n" -> optionI18nStringtoJsValue(ticket.proposedResolution.name),
        "reporter_id"     -> JsNumber(ticket.reporter.id),
        "reporter_name"   -> JsString(ticket.reporter.name),
        "assignee_id"     -> optionLongtoJsValue(ticket.assignee.id),
        "assignee_name"   -> optionStringtoJsValue(ticket.assignee.name),
        "attention_id"    -> optionLongtoJsValue(ticket.attention.id),
        "attention_name"  -> optionStringtoJsValue(ticket.attention.name),
        "severity_id"     -> JsNumber(ticket.severity.id),
        "severity_color"  -> JsString(ticket.severity.color),
        "severity_name"   -> JsString(ticket.severity.name),
        "severity_name_i18n" -> JsString(Messages(ticket.severity.name)),
        "severity_position" -> JsNumber(ticket.severity.position),
        "status_id"       -> JsNumber(ticket.status.id),
        "status_name"     -> JsString(ticket.status.name),
        "status_name_i18n"-> JsString(Messages(ticket.status.name)),
        "type_id"         -> JsNumber(ticket.ttype.id),
        "type_color"      -> JsString(ticket.ttype.color),
        "type_name"       -> JsString(ticket.ttype.name),
        "type_name_i18n"  -> JsString(Messages(ticket.ttype.name)),
        "user_id"         -> JsNumber(ticket.user.id),
        "user_name"       -> JsString(ticket.user.name),
        "summary"         -> JsString(ticket.summary),
        "short_summary"   -> JsString(ticket.summary match {
          case x if x.length > 15 => x.take(15) + "&hellip;"
          case x => x
        }),
        "workflow_status_id" -> JsNumber(ticket.workflowStatusId),
        "description"     -> JsString(markdown.parseToHTML(ticket.description.getOrElse(""))),
        "date_created"    -> JsString(dateFormatter.format(ticket.dateCreated))
      )
      toJson(tdoc)
    }
  }

  /**
   * JSON conversion for Group
   */
  implicit object GroupFormat extends Format[Group] {

    def reads(json: JsValue): Group = Group(
      id          = Id((json \ "id").as[Long]),
      name        = (json \ "name").as[String],
      dateCreated = new Date() // XXX
    )

    def writes(obj: Group): JsValue = {

      val doc: Map[String,JsValue] = Map(
        "id"              -> JsNumber(obj.id.get),
        "name"            -> JsString(obj.name),
        "date_created"    -> JsString(dateFormatter.format(obj.dateCreated))
      )
      toJson(doc)
    }
  }

  /**
   * JSON conversion for Link
   */
  implicit object LinkFormat extends Format[Link] {

    def reads(json: JsValue): Link = Link(
      id          = Id((json \ "id").as[Long]),
      typeId      = (json \ "type_id").as[Long],
      typeName    = (json \ "name").as[String],
      parentId    = (json \ "parent_id").as[String],
      childId     = (json \ "child_id").as[String],
      dateCreated = new Date() // XXX
    )

    def writes(l: Link): JsValue = {

      val ldoc: Map[String,JsValue] = Map(
        "id"              -> JsNumber(l.id.get),
        "type_id"         -> JsNumber(l.typeId),
        "name"            -> JsString(l.typeName),
        "name_i18n"       -> JsString(Messages(l.typeName)),
        "parent_id"       -> JsString(l.parentId),
        "child_id"        -> JsString(l.childId),
        "date_created"    -> JsString(dateFormatter.format(l.dateCreated))
      )
      toJson(ldoc)
    }
  }

  /**
   * JSON conversion for Project
   */
  implicit object ProjectFormat extends Format[Project] {

    def reads(json: JsValue): Project = Project(
      id          = Id((json \ "id").as[Long]),
      workflowId  = (json \ "workflow_id").as[Long],
      name        = (json \ "name").as[String],
      key         = (json \ "key").as[String],
      ownerId     = (json \ "owner_id").as[Option[Long]],
      permissionSchemeId = (json \ "permission_scheme_id").as[Long],
      defaultPriorityId = (json \ "default_priority_id").as[Option[Long]],
      defaultSeverityId = (json \ "default_severity_id").as[Option[Long]],
      defaultTypeId = (json \ "default_type_id").as[Option[Long]],
      defaultAssignee = (json \ "default_assignee").as[Option[Int]],
      dateCreated = new Date() // XXX
    )

    def writes(obj: Project): JsValue = {

      val owner = obj.ownerId match {
        case Some(id) => JsNumber(id)
        case None     => JsNull
      }

      val prio = obj.defaultPriorityId match {
        case Some(id) => JsNumber(id)
        case None     => JsNull
      }

      val sev = obj.defaultSeverityId match {
        case Some(id) => JsNumber(id)
        case None     => JsNull
      }

      val ttype = obj.defaultTypeId match {
        case Some(id) => JsNumber(id)
        case None     => JsNull
      }

      val defAssign = obj.defaultAssignee match {
        case Some(id) => JsNumber(id)
        case None     => JsNull
      }

      val doc: Map[String,JsValue] = Map(
        "id"              -> JsNumber(obj.id.get),
        "workflow_id"     -> JsNumber(obj.workflowId),
        "name"            -> JsString(obj.name),
        "key"             -> JsString(obj.key),
        "sequence_current"-> JsNumber(obj.sequenceCurrent),
        "owner_id"        -> owner,
        "permission_scheme_id" -> JsNumber(obj.permissionSchemeId),
        "default_priority_id" -> prio,
        "default_severity_id" -> sev,
        "default_type_id" -> ttype,
        "default_assignee" -> defAssign,
        "date_created"    -> JsString(dateFormatter.format(obj.dateCreated))
      )
      toJson(doc)
    }
  }

  /**
   * JSON conversion for User
   */
  implicit object UserFormat extends Format[User] {

    def reads(json: JsValue): User = User(
      id          = Id((json \ "id").as[Long]),
      username    = (json \ "username").as[String],
      password    = (json \ "password").as[String],
      realName    = (json \ "real_name").as[String],
      email       = (json \ "email").as[String],
      dateCreated = new Date() // XXX
    )

    def writes(obj: User): JsValue = {

      val doc: Map[String,JsValue] = Map(
        "id"              -> JsNumber(obj.id.get),
        "username"        -> JsString(obj.username),
        "password"        -> JsString(obj.password),
        "real_name"       -> JsString(obj.realName),
        "email"           -> JsString(obj.email),
        "date_created"    -> JsString(dateFormatter.format(obj.dateCreated))
      )
      toJson(doc)
    }
  }


  /**
   * JSON conversion for WorkflowStatus
   */
  implicit object WorkflowStatusFormat extends Format[WorkflowStatus] {

    def reads(json: JsValue): WorkflowStatus = WorkflowStatus(
      id          = Id((json \ "id").as[Long]),
      workflowId  = (json \ "workflow_id").as[Long],
      statusId    = (json \ "status_id").as[Long],
      name        = (json \ "name").as[String],
      position    = (json \ "position").as[Int]
    )

    def writes(ws: WorkflowStatus): JsValue = {

      val wsdoc: Map[String,JsValue] = Map(
        "id"              -> JsNumber(ws.id.get),
        "workflow_id"     -> JsNumber(ws.workflowId),
        "status_id"       -> JsNumber(ws.statusId),
        "name"            -> JsString(ws.name),
        "name_i18n"       -> JsString(Messages(ws.name)),
        "position"        -> JsNumber(ws.position)
      )
      toJson(wsdoc)
    }
  }
}