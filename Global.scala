import play.api._
import play.api.Play.current
import play.db.DB
import models.SearchModel

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    Logger.info("Application has started")
    if(!Play.isTest) {
      SearchModel.checkIndices
    }
  }

  override def onStop(app: Application) {
    Logger.info("Application shutdown...")
    if(!Play.isTest) {
      SearchModel.shutdown
    }
  }
}