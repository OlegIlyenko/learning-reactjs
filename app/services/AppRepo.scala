package services

import models.App
import reactivemongo.api.DB
import reactivemongo.bson.{BSONObjectID, BSONDocument}
import scaldi.Injectable._
import scaldi.Injector
import reactivemongo.bson._
import reactivemongo.api.collections.bson.BSONCollectionProducer
import scala.concurrent.ExecutionContext.Implicits.global

class AppRepo(implicit inj: Injector) {

  val db = inject [DB]

  def list = db.collection("apps").find(BSONDocument.empty).cursor[App].collect[List]()

  def saveOrUpdate(app: App) =
    app.id map { id =>
      db.collection("apps").update(BSONDocument("_id" -> BSONObjectID(id)), app) map (_ => app)
    } getOrElse {
      val appWithId = app.copy(id = Some(BSONObjectID.generate.stringify))
      db.collection("apps").insert(appWithId) map (_ => appWithId)
    }

  def remove(id: String) =
    db.collection("apps").remove(BSONDocument("_id" -> BSONObjectID(id))) map (_ => ())

}
