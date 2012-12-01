package sultan

import anorm.Id
import models._
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import play.api.i18n.Messages
import play.api.libs.json.Json._
import play.api.libs.json._

object JsonFormats {

  val dateFormatter = DateTimeFormat.forPattern("yyyyMMdd'T'HHmmss'Z'")
  val dateFormatterUTC = DateTimeFormat.forPattern("yyyyMMdd'T'HHmmss'Z'").withZoneUTC()

  // XXX UNIT TESTS FOR THE LOVE OF GOD
  private def optionLongtoJsValue(maybeId: Option[Long]) = maybeId.map({ l => JsNumber(l) }).getOrElse(JsNull)

  private def optionI18nStringtoJsValue(maybeId: Option[String]) = maybeId.map({ s => JsString(Messages(s)) }).getOrElse(JsNull)
  private def optionStringtoJsValue(maybeId: Option[String]) = maybeId.map({ s => JsString(s) }).getOrElse(JsNull)

  // XXX UNIT TESTS FOR THE LOVE OF GOD

  /**
   * JSON conversion for Change
   */
  implicit object ChangeFormat extends Format[Change] {
    def reads(json: JsValue): Change = Change(
      id = Id((json \ "id").as[Long]),
      userId = (json \ "userId").as[Long],
      ownerId = (json \ "ownerId").as[Long],
      changeTypeId = (json \ "changeTypeId").as[Long],
      duration = (json \ "duration").as[Long],
      risk = (json \ "risk").as[Int],
      summary = (json \ "summary").as[String],
      description = (json \ "description").as[Option[String]],
      notes = (json \ "notes").as[Option[String]],
      dateBegun = (json \ "dateBegun").as[Option[String]].map({ d => Some(dateFormatterUTC.parseDateTime(d)) }).getOrElse(None),
      dateClosed = (json \ "dateClosed").as[Option[String]].map({ d => Some(dateFormatterUTC.parseDateTime(d)) }).getOrElse(None),
      dateCompleted = (json \ "dateCompleted").as[Option[String]].map({ d => Some(dateFormatterUTC.parseDateTime(d)) }).getOrElse(None),
      dateCreated = dateFormatterUTC.parseDateTime((json \ "dateCreated").as[String]),
      dateScheduled = dateFormatterUTC.parseDateTime((json \ "dateScheduled").as[String]),
      success = (json \ "success").as[Boolean]
    )

    def writes(obj: Change): JsValue = {

      val creator = UserModel.getById(obj.userId).get
      val owner = UserModel.getById(obj.ownerId).get
      val ctype = ChangeTypeModel.getById(obj.changeTypeId).get

      val doc: Map[String,JsValue] = Map(
        "id"            -> JsNumber(obj.id.get),
        "userId"        -> JsNumber(obj.userId),
        "userRealName"  -> JsString(creator.realName),
        "ownerId"       -> JsNumber(obj.ownerId),
        "ownerRealName" -> JsString(owner.realName),
        "changeTypeId"  -> JsNumber(obj.changeTypeId),
        "changeTypeName" -> JsString(ctype.name),
        "changeTypeNameI18N" -> JsString(Messages(ctype.name)),
        "changeTypeColor" -> JsString(ctype.color),
        "statusName"    -> JsString(obj.status),
        "statusNameI18N" -> JsString(Messages(obj.status)),
        "statusColor"   -> JsString(obj.color),
        "duration"      -> JsNumber(obj.duration),
        "risk"          -> JsNumber(obj.risk),
        "summary"       -> JsString(obj.summary),
        "description"   -> optionStringtoJsValue(obj.description),
        "notes"         -> optionStringtoJsValue(obj.notes),
        "dateBegun"     -> obj.dateBegun.map({ d => JsString(dateFormatter.print(d)) }).getOrElse(JsNull),
        "begun"         -> JsBoolean(obj.dateBegun.isDefined),
        "dateClosed"    -> obj.dateClosed.map({ d => JsString(dateFormatter.print(d)) }).getOrElse(JsNull),
        "closed"        -> JsBoolean(obj.dateClosed.isDefined),
        "dateCompleted" -> obj.dateCompleted.map({ d => JsString(dateFormatter.print(d)) }).getOrElse(JsNull),
        "completed"     -> JsBoolean(obj.dateCompleted.isDefined),
        "dateCreated"   -> JsString(dateFormatter.print(obj.dateCreated)),
        "dateScheduled" -> JsString(dateFormatter.print(obj.dateScheduled)),
        "success"       -> JsBoolean(obj.success)
      )
      toJson(doc)
    }
  }

  /**
   * JSON conversion for FullChange
   */
  implicit object FullChangeFormat extends Format[FullChange] {
    def reads(json: JsValue): FullChange = FullChange(
      id = Id((json \ "id").as[Long]),
      user = NamedThing(
        id    = (json \ "userId").as[Long],
        name  = (json \ "userRealName").as[String],
        i18nName = (json \ "userRealName").as[String]
      ),
      owner = NamedThing(
        id = (json \ "ownerId").as[Long],
        name = (json \ "ownerRealName").as[String],
        i18nName = (json \ "ownerRealName").as[String]
      ),
      changeType = ColoredThing(
        id = (json \ "changeTypeId").as[Long],
        name = (json \ "changeTypeName").as[String],
        i18nName = (json \ "changeTypeNameI18N").as[String],
        color = (json \ "changeTypeColor").as[String]
      ),
      duration = (json \ "duration").as[Long],
      risk = (json \ "risk").as[Int],
      summary = (json \ "summary").as[String],
      description = (json \ "description").as[Option[String]],
      notes = (json \ "notes").as[Option[String]],
      dateBegun = (json \ "dateBegun").as[Option[String]].map({ d => Some(dateFormatterUTC.parseDateTime(d)) }).getOrElse(None),
      dateClosed = (json \ "dateClosed").as[Option[String]].map({ d => Some(dateFormatterUTC.parseDateTime(d)) }).getOrElse(None),
      dateCompleted = (json \ "dateCompleted").as[Option[String]].map({ d => Some(dateFormatterUTC.parseDateTime(d)) }).getOrElse(None),
      dateCreated = dateFormatterUTC.parseDateTime((json \ "dateCreated").as[String]),
      dateScheduled = dateFormatterUTC.parseDateTime((json \ "dateScheduled").as[String]),
      success = (json \ "success").as[Boolean]
    )

    def writes(obj: FullChange): JsValue = {

      // Not needed
      val doc: Map[String,JsValue] = Map.empty
      toJson(doc)
    }
  }
}
