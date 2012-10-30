package sultan

import java.text.SimpleDateFormat
import java.util.Date

object DateFormatter {

  val longFormatter = new SimpleDateFormat("EEE, MMM d, yyyy")

  /**
   * Format a date (in seconds) into "long" string.
   */
  def displayLongDate(timestamp: Long): String = {
    longFormatter.format(new Date(timestamp * 1000))
  }
}