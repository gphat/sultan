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
      userId = (json \ "user_id").as[Long],
      ownerId = (json \ "owner_id").as[Long],
      changeTypeId = (json \ "change_type_id").as[Long],
      duration = (json \ "duration").as[Long],
      risk = (json \ "risk").as[Int],
      summary = (json \ "summary").as[String],
      description = (json \ "description").as[Option[String]],
      notes = (json \ "notes").as[Option[String]],
      dateBegun = (json \ "date_begun").as[Option[String]].map({ d => Some(dateFormatterUTC.parseDateTime(d)) }).getOrElse(None),
      dateClosed = (json \ "date_closed").as[Option[String]].map({ d => Some(dateFormatterUTC.parseDateTime(d)) }).getOrElse(None),
      dateCompleted = (json \ "date_completed").as[Option[String]].map({ d => Some(dateFormatterUTC.parseDateTime(d)) }).getOrElse(None),
      dateCreated = dateFormatterUTC.parseDateTime((json \ "date_created").as[String]),
      dateScheduled = dateFormatterUTC.parseDateTime((json \ "date_scheduled").as[String]),
      success = (json \ "success").as[Boolean]
    )

    def writes(obj: Change): JsValue = {

      val creator = UserModel.getById(obj.userId).get
      val owner = UserModel.getById(obj.ownerId).get
      val ctype = ChangeTypeModel.getById(obj.changeTypeId).get

      val doc: Map[String,JsValue] = Map(
        "id"            -> JsNumber(obj.id.get),
        "user_id"       -> JsNumber(obj.userId),
        "user_realname" -> JsString(creator.realName),
        "owner_id"      -> JsNumber(obj.ownerId),
        "owner_realname"-> JsString(owner.realName),
        "change_type_id"-> JsNumber(obj.changeTypeId),
        "change_type_name" -> JsString(ctype.name),
        "change_type_name_i18n" -> JsString(Messages(ctype.name)),
        "change_type_color" -> JsString(ctype.color),
        "status_name"   -> JsString(obj.status),
        "status_name_i18n" -> JsString(Messages(obj.status)),
        "status_color"  -> JsString(obj.color),
        "duration"      -> JsNumber(obj.duration),
        "risk"          -> JsNumber(obj.risk),
        "summary"       -> JsString(obj.summary),
        "description"   -> optionStringtoJsValue(obj.description),
        "notes"         -> optionStringtoJsValue(obj.notes),
        "date_begun"    -> obj.dateBegun.map({ d => JsString(dateFormatter.print(d)) }).getOrElse(JsNull),
        "begun"         -> JsBoolean(obj.dateBegun.isDefined),
        "date_closed"   -> obj.dateClosed.map({ d => JsString(dateFormatter.print(d)) }).getOrElse(JsNull),
        "date_completed"-> obj.dateCompleted.map({ d => JsString(dateFormatter.print(d)) }).getOrElse(JsNull),
        "completed"     -> JsBoolean(obj.dateCompleted.isDefined),
        "date_created"  -> JsString(dateFormatter.print(obj.dateCreated)),
        "date_scheduled"-> JsString(dateFormatter.print(obj.dateScheduled)),
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
        id    = (json \ "user_id").as[Long],
        name  = (json \ "user_realname").as[String],
        i18nName = (json \ "user_realname").as[String]
      ),
      owner = NamedThing(
        id = (json \ "owner_id").as[Long],
        name = (json \ "owner_realname").as[String],
        i18nName = (json \ "owner_realname").as[String]
      ),
      changeType = ColoredThing(
        id = (json \ "change_type_id").as[Long],
        name = (json \ "change_type_name").as[String],
        i18nName = (json \ "change_type_name_i18n").as[String],
        color = (json \ "change_type_color").as[String]
      ),
      duration = (json \ "duration").as[Long],
      risk = (json \ "risk").as[Int],
      summary = (json \ "summary").as[String],
      description = (json \ "description").as[Option[String]],
      notes = (json \ "notes").as[Option[String]],
      dateBegun = (json \ "date_begun").as[Option[String]].map({ d => Some(dateFormatterUTC.parseDateTime(d)) }).getOrElse(None),
      dateClosed = (json \ "date_closed").as[Option[String]].map({ d => Some(dateFormatterUTC.parseDateTime(d)) }).getOrElse(None),
      dateCompleted = (json \ "date_completed").as[Option[String]].map({ d => Some(dateFormatterUTC.parseDateTime(d)) }).getOrElse(None),
      dateCreated = dateFormatterUTC.parseDateTime((json \ "date_created").as[String]),
      dateScheduled = dateFormatterUTC.parseDateTime((json \ "date_scheduled").as[String]),
      success = (json \ "success").as[Boolean]
    )

    def writes(obj: FullChange): JsValue = {

      // Not needed
      val doc: Map[String,JsValue] = Map.empty
      toJson(doc)
    }
  }
}
