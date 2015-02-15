package controllers

import play.api.libs.json.{JsError, Json}
import play.api.mvc.{Action, Controller}
import scaldi.{Injector, Injectable}
import services.AppRepo
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import models.App

class Apps(implicit inj: Injector) extends Controller with Injectable {

  val repo = inject [AppRepo]

  def listApps = Action.async {
    repo.list map (Json.toJson(_)) map (Ok(_))
  }

  def addApp = Action.async(parse.json) { req =>
    req.body.validate[App]
      .map (app => repo saveOrUpdate app map (Json.toJson(_)) map (Created(_)))
      .recoverTotal (e => Future.successful(BadRequest(Json.obj("status" ->"KO", "message" -> JsError.toFlatJson(e)))))
  }

  def updateApp(id: String) = Action.async(parse.json) { req =>
    req.body.validate[App]
      .map (app => repo saveOrUpdate app map (Json.toJson(_)) map (Ok(_)))
      .recoverTotal (e => Future.successful(BadRequest(Json.obj("status" ->"KO", "message" -> JsError.toFlatJson(e)))))
  }

  def deleteApp(id: String) = Action.async {
    repo remove id map (_ => NoContent)
  }

}
