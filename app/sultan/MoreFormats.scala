package sultan

import org.joda.time.{DateTime,LocalTime}
import play.api.data.FormError
import play.api.data.format.Formats._
import play.api.data.format.Formatter

object MoreFormats {

  /**
   * Formatter for the `org.joda.time.DateTime` type.
   *
   * @param pattern a date pattern as specified in `org.joda.time.format.DateTimeFormat`.
   */
  def jodaDateTimeFormat(pattern: String): Formatter[org.joda.time.DateTime] = new Formatter[org.joda.time.DateTime] {

    import org.joda.time.DateTime

    override val format = Some(("format.date", Seq(pattern)))

    def bind(key: String, data: Map[String, String]) = {

      stringFormat.bind(key, data).right.flatMap { s =>
        scala.util.control.Exception.allCatch[DateTime]
          .either(DateTime.parse(s, org.joda.time.format.DateTimeFormat.forPattern(pattern)))
          .left.map(e => Seq(FormError(key, "error.date", Nil)))
      }
    }

    def unbind(key: String, value: DateTime) = Map(key -> value.toString(pattern))
  }

  /**
   * Default formatter for `org.joda.time.DateTime` type with pattern `yyyy-MM-dd`.
   *
   * @param pattern a date pattern as specified in `org.joda.time.format.DateTimeFormat`.
   */
  implicit val jodaDateTimeFormat: Formatter[org.joda.time.DateTime] = jodaDateTimeFormat("yyyy-MM-dd")

  /**
   * Formatter for the `org.joda.time.DateTime` type.
   *
   * @param pattern a date pattern as specified in `org.joda.time.format.DateTimeFormat`.
   */
  implicit def jodaTimeFormat: Formatter[org.joda.time.LocalTime] = new Formatter[org.joda.time.LocalTime] {

    //override val format = Some(("format.date", Seq(pattern)))

    def bind(key: String, data: Map[String, String]) = {

      stringFormat.bind(key, data).right.flatMap { s =>
        scala.util.control.Exception.allCatch[LocalTime]
          .either(new LocalTime(12,0))
          .left.map(e => Seq(FormError(key, "error.time", Nil)))
      }
    }

    def unbind(key: String, value: LocalTime) = Map(key -> (value.getHourOfDay + ":" + value.getMinuteOfHour))
  }

}