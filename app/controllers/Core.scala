package controllers

import java.util.Date
import models.SearchModel
import play.api._
import play.api.mvc._
import play.api.mvc.Security._
import play.db._
import royal.ends.Search._

object Core extends Controller with Secured {

  def index(page: Int = 1, count: Int = 10, query: String = "", sort: Option[String] = None, order: Option[String] = None) = IsAuthenticated { implicit request =>

    val filters = request.queryString filterKeys { key =>
      SearchModel.changeFilterMap.get(key).isDefined
    }

    val userId = request.session.get("user_id").get.toLong

    val q = SearchQuery(
      userId = userId, page = page, count = count, query = query,
      filters = filters, sortBy = sort, sortOrder = order
    )
    val result = SearchModel.searchChange(q)

    Ok(views.html.index(filters, result))
  }
}
