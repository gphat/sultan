package sultan

import anorm.Id
import java.text.SimpleDateFormat
import java.util.Date
import models._
import play.api.i18n.Messages
import play.api.libs.json.Json._
import play.api.libs.json._

object JsonFormats {

  val dateFormatter = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'")

  // XXX UNIT TESTS FOR THE LOVE OF GOD
  private def optionDatetoJsValue(maybeDate: Option[Date]) = maybeDate.map({ d => JsString(dateFormatter.format(d)) }).getOrElse(JsNull)
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
      dateBegun = None, // XXX
      dateClosed = None, // XXX
      dateCompleted = None, // XXX
      dateCreated = new Date(), // XXX
      dateScheduled = new Date(), // XXX
      success = (json \ "success").as[Boolean]
    )

    def writes(obj: Change): JsValue = {
      val doc: Map[String,JsValue] = Map(
        "id"            -> JsNumber(obj.id.get),
        "user_id"       -> JsNumber(obj.userId),
        "owner_id"      -> JsNumber(obj.ownerId),
        "change_type_id"-> JsNumber(obj.changeTypeId),
        "duration"      -> JsNumber(obj.duration),
        "risk"          -> JsNumber(obj.risk),
        "summary"       -> JsString(obj.summary),
        "description"   -> optionStringtoJsValue(obj.description),
        "notes"         -> optionStringtoJsValue(obj.notes),
        "date_begun"    -> optionDatetoJsValue(obj.dateBegun),
        "date_closed"   -> optionDatetoJsValue(obj.dateClosed),
        "date_completed"-> optionDatetoJsValue(obj.dateCompleted),
        "date_created"  -> JsString(dateFormatter.format(obj.dateCreated)),
        "date_scheduled"-> JsString(dateFormatter.format(obj.dateCreated)),
        "success"       -> JsBoolean(obj.success)
      )
      toJson(doc)
    }
  }
}
