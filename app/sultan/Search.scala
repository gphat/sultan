package sultan

import collection.JavaConversions._
import com.traackr.scalastic.elasticsearch.Indexer
import royal.ends._
import royal.ends.Search._
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.index.query._
import org.elasticsearch.index.query.FilterBuilders._
import org.elasticsearch.index.query.QueryBuilders._
import org.elasticsearch.search.facet.terms.strings._
import org.elasticsearch.search.facet.terms.longs.InternalLongTermsFacet
import org.elasticsearch.search.SearchHit
import org.elasticsearch.search.facet.FacetBuilders._
import org.elasticsearch.search.sort._
import play.api.Logger

object Search {

  def runQuery(indexer: Indexer, index: String, query: SearchQuery, filterMap: Map[String,String], sortMap: Map[String,String], facets: Map[String,String] = Map.empty): SearchResponse = {

    // Make a bool filter to collect all our filters together
    val finalFilter: BoolFilterBuilder = boolFilter

    val termFilters : Iterable[Seq[FilterBuilder]] = query.filters.filter { kv =>
      kv._1 != "project_id" && filterMap.get(kv._1).isDefined
    } map {
      case (key, values) => values.map { v =>
        termFilter(filterMap.get(key).getOrElse(key), v).asInstanceOf[FilterBuilder]
      }
    }

    // Might not have user filters
    if(!termFilters.isEmpty) {
      val userFilter = andFilter(termFilters.flatten.toSeq:_*)
      finalFilter.must(userFilter)
    }
    val simpleQuery = queryString(if(query.query.isEmpty) "*" else query.query)
    val actualQuery = if(termFilters.isEmpty) simpleQuery else filteredQuery(simpleQuery, finalFilter)

    Logger.debug("Running ES query:")
    Logger.debug(actualQuery.toString)

    val sortOrder = query.sortOrder match {
      case Some(s) => if(s.equalsIgnoreCase("desc")) SortOrder.DESC else SortOrder.ASC
      case None => SortOrder.DESC
    }

    indexer.search(
      query = actualQuery,
      indices = Seq(index),
      facets = facets.map { case (name, field) =>
        termsFacet(name).field(field)
      },
      size = Some(query.count),
      from = query.page match {
        case 0 => Some(0)
        case 1 => Some(0)
        case _ => Some((query.page * query.count) - 1)
      },
      sorting = Seq(
        // This is a bit messy… but it gets the job done.
        sortMap.get(query.sortBy.getOrElse("date_created")).getOrElse("date_created") -> sortOrder
      )
    )
  }
}