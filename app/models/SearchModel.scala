package models

import com.traackr.scalastic.elasticsearch.Indexer
import org.joda.time.DateTime
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
    "type"        -> "changeTypeName",
    "owner"       -> "ownerRealName",
    "completed"   -> "completed",
    "status"      -> "statusName"
  )
  val changeSortMap = Map(
    "date_created"    -> "dateCreated",
    "date_scheduled"  -> "dateScheduled",
    "duration"        -> "duration",
    "id"              -> "id",
    "owner"           -> "ownerRealName",
    "risk"            -> "risk",
    "type"            -> "changeTypeName"
  )
  // XXX booleans for begun, etc
  val changeMapping = """
  {
    "change": {
      "properties": {
        "userId": {
          "type": "long",
          "index": "not_analyzed"
        },
        "userRealName": {
          "type": "string",
          "index": "not_analyzed"
        },
        "ownerId": {
          "type": "long",
          "index": "not_analyzed"
        },
        "ownerRealName": {
          "type": "string",
          "index": "not_analyzed"
        },
        "changeTypeId": {
          "type": "long",
          "index": "not_analyzed"
        },
        "changeTypeName": {
          "type": "string",
          "index": "not_analyzed"
        },
        "changeTypeColor": {
          "type": "string",
          "index": "not_analyzed"
        },
        "statusName": {
          "type": "string",
          "index": "not_analyzed"
        },
        "statusColor": {
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
        "dateBegun": {
          "type": "date",
          "format": "basic_date_time_no_millis"
        },
        "begun": {
          "type": "boolean",
          "index": "not_analyzed"
        },
        "dateClosed": {
          "type": "date",
          "format": "basic_date_time_no_millis"
        },
        "dateCompleted": {
          "type": "date",
          "format": "basic_date_time_no_millis"
        },
        "dateCreated": {
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
    "status"   -> "statusName",
    "type"    -> "changeTypeName",
    "owner"   -> "ownerRealName"
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
    println(res)
    Search.parseSearchResponse(pager = pager, response = res)
  }

  /**
   * Shutdown ElasticSearch.
   */
  def shutdown {
    indexer.stop
  }
}
