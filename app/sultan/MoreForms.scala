package sultan

import org.joda.time.{DateTime,LocalTime}
import play.api.data.{FormError,Mapping}
import play.api.data.Forms._
import play.api.data.format.Formats._
import sultan.MoreFormats._

object MoreForms {

  /**
   * Constructs a simple mapping for a date field (mapped as `org.joda.time.DateTime type`).
   *
   * For example:
   * {{{
   *   Form("birthdate" -> jodaDate)
   * }}}
   */
  // val jodaDate: Mapping[org.joda.time.DateTime] = of[org.joda.time.DateTime]

  /**
   * Constructs a simple mapping for a date field (mapped as `org.joda.time.DateTime type`).
   *
   * For example:
   * {{{
   *   Form("birthdate" -> jodaDate("dd-MM-yyyy"))
   * }}}
   *
   * @param pattern the date pattern, as defined in `org.joda.time.format.DateTimeFormat`
   */
  // def jodaDate(pattern: String): Mapping[org.joda.time.DateTime] = of[org.joda.time.DateTime] as jodaDateTimeFormat(pattern)

  def jodaTime: Mapping[org.joda.time.LocalTime] = of[org.joda.time.LocalTime] as jodaTimeFormat
}