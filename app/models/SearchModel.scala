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

import royal.ends._
import scala.collection.JavaConversions._

object SearchModel {

  val dateFormatter = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'")

  val config = Play.configuration.getConfig("emperor")
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
    "type"        -> "type_name"
  )
  val ticketSortMap = Map(
    "date_created"-> "date_created",
    "id"          -> "id"
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
        "duration": {
          "type": "long",
          "index": "not_analyzed"
        },
        "type_id": {
          "type": "long",
          "index": "not_analyzed"
        },
        "type_color": {
          "type": "string",
          "index": "not_analyzed"
        },
        "type_name": {
          "type": "string",
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
    "resolution" -> "resolution_name",
    "type" -> "type_name",
    "project" -> "project_name",
    "priority" -> "priority_name",
    "severity" -> "severity_name",
    "status" -> "status_name",
    "assignee" -> "assignee_name",
    "reporter" ->"reporter_name"
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
   * Index a ticket.
   */
  def indexTicket(ticket: FullTicket) {

    indexer.index(ticketIndex, ticketType, ticket.ticketId, toJson(ticket).toString)

    indexer.refresh()
  }

  /**
   * Delete all the existing indexes and recreate them. Then iterate over
   * all the tickets and index each one and it's history.  Finally
   * reindex all the ticket comments.
   */
  def reIndex {

    indexer.deleteIndex(changeIndex)
    checkIndices

    // Reindex all tickets and their history
    // TicketModel.getAllCurrentFull.foreach { ticket =>
    //   indexTicket(ticket)
    //   val count = TicketModel.getAllFullCountById(ticket.ticketId)
    //   if(count > 1) {
    //     TicketModel.getAllFullById(ticket.ticketId).foldLeft(None: Option[FullTicket])((oldTick, newTick) => {
    //       // First run will NOT index history because oldTick is None (as None starts the fold)
    //       oldTick.map { ot => indexHistory(oldTick = ot, newTick = newTick) }

    //       Some(newTick)
    //     })
    //   }
    // }
    // Reindex all ticket comments
    // XXX This should be nested within the above loop to avoid having to
    // re-fetch every fullticket.
    // TicketModel.getAllComments.foreach { comment =>
    //   indexComment(comment)
    // }
  }

  /**
   * Search for a ticket.
   */
  def searchTicket(query: SearchQuery): SearchResult[FullTicket] = {

    val res = runQuery(indexer, ticketIndex, query, ticketFilterMap, ticketSortMap, ticketFacets)
    val hits = res.hits.map { hit => Json.fromJson[FullTicket](Json.parse(hit.sourceAsString())) }

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
