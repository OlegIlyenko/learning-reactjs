package models

import play.api.libs.json.Json
import reactivemongo.bson._

case class App(id: Option[String], name: String, status: Option[String] = None)

object App {
  implicit object AppBSON extends BSONDocumentReader[App] with BSONDocumentWriter[App] {
    def read(doc: BSONDocument) =
      App(
        doc.getAs[BSONObjectID]("_id") map (_.stringify),
        doc.getAs[BSONString]("name").get.value,
        doc.getAs[BSONString]("status") map (_.value))

    def write(app: App) =
      BSONDocument(
        "_id" -> (app.id map BSONObjectID.apply getOrElse BSONObjectID.generate),
        "name" -> BSONString(app.name),
        "status" -> app.status.map(BSONString.apply))
  }

  implicit val appJson = Json.format[App]
}