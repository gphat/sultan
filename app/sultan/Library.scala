package sultan

import collection.JavaConversions._
import scala.collection.mutable.ListBuffer

/**
 * Helper for pagination.
 */
case class Page[+A](items: Iterable[A], requestedPage: Int, count: Int, total: Long) {
  lazy val lastPage = (total.toDouble / count).ceil.toInt
  lazy val page = requestedPage match {
      case p if p < firstPage => firstPage
      case p if p > lastPage => lastPage
      case _ => requestedPage
  }
  lazy val prev = Option(page - 1).filter(_ >= firstPage)
  lazy val next = Option(page + 1).filter(_ <= lastPage)
  val firstPage = 1
  lazy val offset = page match {
    case p if p == 0 => 0
    case _ => count * (page - 1)
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
