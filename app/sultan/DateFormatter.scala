package sultan

import controllers.AuthenticatedRequest
import collection.JavaConversions._

import org.joda.time.format.{DateTimeFormat,DateTimeFormatter,ISODateTimeFormat}
import org.joda.time.{DateTime,DateTimeZone}

/**
 * Utilities for formatting dates.
 */
object DateFormatter {

  val longDateTimeFormatter = DateTimeFormat.forPattern("h:mm aa z EEE, MMM d, yyyy")
  val isoFormatter = ISODateTimeFormat.dateTime()

  val tzs = asScalaSet(DateTimeZone.getAvailableIDs).toSeq.sorted

  def timeZoneList: Seq[(String, String)] = {
    tzs.map({ id => (id, id) }).toSeq
  }

  def displayLongDateTime(dt: Option[DateTime]): String = displayLongDateTime(dt.get)

  /**
   * Formats a DateTime into a "long" string ("h:mm aa EEE, MMM d, yyyy") in
   * the user's Time Zone.
   */
  def displayLongDateTime(dt: DateTime): String = {
    longDateTimeFormatter.print(dt.withZone(DateTimeZone.forID("America/Los_Angeles")))
  }

  /**
   * Formats a DateTime into an ISO8601 string (in UTC)
   */
  def displayISO8601(dt: DateTime): String = isoFormatter.print(dt.withZone(DateTimeZone.UTC))
}