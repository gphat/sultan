package models

import com.traackr.scalastic.elasticsearch.Indexer
import java.text.SimpleDateFormat
import java.util.{Date,TimeZone}
import play.api._
import play.api.Play.current
import play.api.libs.json.Json._
import play.api.libs.json._

import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.index.query._
import org.elasticsearch.index.query.FilterBuilders._
import org.elasticsearch.index.query.QueryBuilders._
import org.elasticsearch.search.facet.terms.strings._
import org.elasticsearch.search.facet.terms.longs.InternalLongTermsFacet
import org.elasticsearch.search.SearchHit
import org.elasticsearch.search.facet.FacetBuilders._
import org.elasticsearch.search.sort._

import org.elasticsearch.client._, transport._
import org.elasticsearch.common.settings.ImmutableSettings._
import org.elasticsearch.node._, NodeBuilder._

import sultan._
import sultan.Search._
import scala.collection.JavaConversions._
import sultan.JsonFormats._

object SearchModel {

  val dateFormatter = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'")

  val config = Play.configuration.getConfig("sultan")
  // Embedded ES
  val settings = Map(
    "path.data" -> config.get.getString("directory").getOrElse("")
  )
  val indexer = Indexer.at(
    nodeBuilder.local(true).data(true).settings(
      settingsBuilder.put(settings)
    ).node
  ).start

  // Change ES index
  val changeIndex = "changes"
  val changeType = "change"
  val changeFilterMap = Map(
    "type"        -> "change_type_name",
    "owner"       -> "owner_realname",
    "completed"   -> "completed",
    "begun"       -> "begun"
  )
  val changeSortMap = Map(
    "date_created"    -> "date_created",
    "date_scheduled"  -> "date_scheduled",
    "duration"        -> "duration",
    "id"              -> "id",
    "owner"           -> "owner_realname",
    "risk"            -> "risk",
    "type"            -> "change_type_name"
  )
  // XXX booleans for begun, etc
  val changeMapping = """
  {
    "change": {
      "properties": {
        "user_id": {
          "type": "long",
          "index": "not_analyzed"
        },
        "user_realname": {
          "type": "string",
          "index": "not_analyzed"
        },
        "owner_id": {
          "type": "long",
          "index": "not_analyzed"
        },
        "owner_realname": {
          "type": "string",
          "index": "not_analyzed"
        },
        "change_type_id": {
          "type": "long",
          "index": "not_analyzed"
        },
        "change_type_name": {
          "type": "string",
          "index": "not_analyzed"
        },
        "change_type_color": {
          "type": "string",
          "index": "not_analyzed"
        },
        "status_name": {
          "type": "string",
          "index": "not_analyzed"
        },
        "status_color": {
          "type": "string",
          "index": "not_analyzed"
        },
        "duration": {
          "type": "long",
          "index": "not_analyzed"
        },
        "risk": {
          "type": "integer",
          "index": "not_analyzed"
        },
        "summary": {
          "type": "string",
          "index": "analyzed"
        },
        "notes": {
          "type": "string",
          "index": "analyzed"
        },
        "description": {
          "type": "string",
          "index": "analyzed"
        },
        "completed": {
          "type": "boolean",
          "index": "not_analyzed"
        },
        "date_begun": {
          "type": "date",
          "format": "basic_date_time_no_millis"
        },
        "begun": {
          "type": "boolean",
          "index": "not_analyzed"
        },
        "date_closed": {
          "type": "date",
          "format": "basic_date_time_no_millis"
        },
        "date_completed": {
          "type": "date",
          "format": "basic_date_time_no_millis"
        },
        "date_created": {
          "type": "date",
          "format": "basic_date_time_no_millis"
        },
        "date_scheduled": {
          "type": "date",
          "format": "basic_date_time_no_millis"
        }
      }
    }
  }
  """

  val changeFacets = Map(
    "begun"   -> "begun",
    "type"    -> "change_type_name",
    "owner"   -> "owner_realname"
  )

  /**
   * Check that all the necessary indices exist.  If they don't, create them.
   */
  def checkIndices = {

    if(!indexer.exists(changeIndex)) {
      indexer.createIndex(changeIndex, settings = Map("number_of_shards" -> "1"))
      indexer.waitTillActive()
      indexer.putMapping(changeIndex, changeType, changeMapping)
    }
    indexer.refresh()
  }

  /**
   * Index a change.
   */
  def indexChange(change: Change) {

    indexer.index(changeIndex, changeType, change.id.get.toString, toJson(change).toString)

    indexer.refresh()
  }

  /**
   * Delete all the existing indexes and recreate them. Then iterate over
   * all the changes and index each one.
   */
  def reIndex {

    indexer.deleteIndex(changeIndex)
    checkIndices

    // Reindex all tickets and their history
    ChangeModel.getAll.foreach { change =>
      indexChange(change)
    }
  }

  /**
   * Search for a change.
   */
  def searchChange(query: SearchQuery): SearchResult[FullChange] = {

    val res = sultan.Search.runQuery(indexer, changeIndex, query, changeFilterMap, changeSortMap, changeFacets)
    val hits = res.hits.map { hit => Json.fromJson[FullChange](Json.parse(hit.sourceAsString())) }

    val pager = Page(hits, query.page, query.count, res.hits.totalHits)
    Search.parseSearchResponse(pager = pager, response = res)
  }

  /**
   * Shutdown ElasticSearch.
   */
  def shutdown {
    indexer.stop
  }
}
