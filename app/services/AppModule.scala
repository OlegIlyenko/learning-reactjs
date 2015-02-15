package services

import akka.actor.ActorSystem
import play.api.libs.iteratee.Concurrent
import reactivemongo.api.{DB, MongoConnection, MongoDriver}
import scaldi.Module
import scala.concurrent.ExecutionContext.Implicits.global
import models.App

class AppModule extends Module {

  lazy val (appUpdatesOut, appUpdatesChannel) = Concurrent.broadcast[App]

  binding identifiedBy 'appUpdates to appUpdatesOut
  binding identifiedBy 'appUpdates to appUpdatesChannel

  binding to ActorSystem("app") destroyWith (_.shutdown())

  binding to new MongoDriver(inject [ActorSystem]) destroyWith (_.close())

  binding to inject[MongoDriver].connection(inject [List[String]] ("mongodb.servers"))
  bind [DB] to inject[MongoConnection].db(inject [String] ("mongodb.db"))

  binding to new AppRepo
}

