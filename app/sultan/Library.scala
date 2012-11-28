package sultan

import collection.JavaConversions._
import scala.collection.mutable.ListBuffer
import scala.math._

  /**
   * Convenient class that represents a single page out of a larger paginated
   * set of results. Contains smarts for preventing the request of pages
   * outside the bounds of the set.
   */
  case class Page[+A](items: Iterable[A], requestedPage: Int, count: Int, total: Long) {

    /**
     * The range of pages.
     */
     lazy val range = Range(start = 1, end = 1.to(total.toInt).by(count).size + 1)
    /**
     * The current page.
     */
    lazy val page = requestedPage match {
        case p if p < range.start => range.start
        case p if p >= range.end => range.end - 1
        case _ => requestedPage
    }
    /**
     * Value of the page before the current one.
     */
    lazy val prev = Option(page - 1).filter(_ >= range.start)
    /**
     * Value of the page after the current one.
     */
    lazy val next = Option(page + 1).filter(_ < range.end)

    /**
     * Calculates an offset based on the `count` and the curent page that
     * is suitable for use with an SQL or search backend.
     */
    lazy val offset = page match {
      case p if p == 0 => 0
      case _ => count * (page - 1)
    }

    def getWindow(size: Int): Range = {
      if(page <= count / 2) {
        Range(start = 1, end = min(size, range.end))
      } else if(page > count / 2) {
        // val s = page - count / 2 toInt
        Range(start = page - count / 2, end = min(page + count / 2, range.end))
      } else {
        range
      }
    }
  }

object Library {

  /**
   * Return a String (URL) with a query parameter (`name`) and `value`.
   * It uses the request to populate existing query params and also
   * to provide the base query path.
   * To override the base path you can pass in a `path`.
   */
  def filterLink(params: Map[String,Seq[String]], path: String, name: String, value: String): String = {

    val q = params + (name -> List(value))

    // Filter out any empty values
    val clean = q.filterNot { p =>
      val vals = p._2.filter { v => v != "" }
      vals.isEmpty
    }

    val qs = clean.foldLeft("")(
      (acc, value) => acc + value._2.foldLeft("")(
        (acc2, param) => {
          value._1 match {
            case "page" => ""
            case _ => acc2 + value._1 + "=" + param + "&"
          }
        }
      )
    )

    path + "?" + qs
  }

  def pagerLink(params: Map[String,Seq[String]], path: String, page: Int = 1, count: Int = 10) : String = {

    val q = params + ("page" -> List(page.toString)) + ("count" -> List(count.toString))


    val qs = q.foldLeft("")(
      (acc, value) => acc + value._2.foldLeft("")(
        (acc2, param) => acc2 + value._1 + "=" + param + "&"
      )
    )

    path + "?" + qs
  }

  def sortLink(params: Map[String,Seq[String]], path: String, name: String): String = {

    // Get rid of sort and order, we'll re-set those
    val cleanQ: Map[String,Seq[String]] = params.filterKeys { key => !key.equalsIgnoreCase("sort") && !key.equalsIgnoreCase("order") }

    val order = params.get("order") match {
      case Some(v) if v.head.isEmpty => "desc"
      case Some(v) if v.head.equalsIgnoreCase("desc") => "asc"
      case _ => "desc"
    }
    val q = cleanQ ++ Map("sort" -> Seq(name), "order" -> Seq(order))

    val qs = q.foldLeft("")(
      (acc, value) => acc + value._2.foldLeft("")(
        (acc2, param) => acc2 + value._1 + "=" + param + "&"
      )
    )

    path + "?" + qs
  }
}
