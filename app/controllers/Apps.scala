package controllers

import akka.actor.ActorRef
import akka.util.Timeout
import play.api.libs.json.{JsError, Json}
import play.api.mvc.{Action, Controller}
import scaldi.{Injector, Injectable}
import services.AppRepo
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import models.App
import akka.pattern.ask
import services.AppsManager._

class Apps(implicit inj: Injector) extends Controller with Injectable {
  implicit val timeout = inject [Timeout] (identified by 'requestTimeout)

  val repo = inject [AppRepo]
  val appManager = inject [ActorRef] (identified by 'appManager)

  def listApps = Action.async {
    repo.list map (Json.toJson(_)) map (Ok(_))
  }

  def addApp = Action.async(parse.json) { req =>
    req.body.validate[App]
      .map (app => appManager.ask(CreateApp(app)).mapTo[App] map (Json.toJson(_)) map (Created(_)))
      .recoverTotal (e => Future.successful(BadRequest(Json.obj("status" ->"KO", "message" -> JsError.toFlatJson(e)))))
  }

  def updateApp(id: String) = Action.async(parse.json) { req =>
    req.body.validate[App]
      .map (app => appManager.ask(UpdateApp(app)).mapTo[App] map (Json.toJson(_)) map (Ok(_)))
      .recoverTotal (e => Future.successful(BadRequest(Json.obj("status" ->"KO", "message" -> JsError.toFlatJson(e)))))
  }

  def deleteApp(id: String) = Action.async {
    appManager ? DeleteApp(id) map (_ => NoContent)
  }
}
