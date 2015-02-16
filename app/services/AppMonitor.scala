package services

import akka.actor.{ActorLogging, ActorRef, Stash, Actor}
import models.App
import play.api.libs.iteratee.Concurrent
import scaldi.Injector
import scaldi.akka.AkkaInjectable._

import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class AppMonitor(implicit inj: Injector) extends Actor with Stash with ActorLogging {
  import AppMonitor._

  val repo = inject [AppRepo]
  val updates = inject [Concurrent.Channel[String]] (identified by 'appUpdates)

  def initial: Receive = {
    case Use(a) =>
      unstashAll()
      scheduleStatusChange()
      context become active(a)

    case CreateApp(a, origin) =>
      val ref = sender()

      repo.saveOrUpdate(a).onComplete {
        case Success(a) => self ! (ref, CreatedApp(a, origin))
        case f: Failure[_] => origin ! f
      }

    case (ref: ActorRef, msg @ CreatedApp(a, _)) =>
      ref ! msg

      updates push a.id.get
      unstashAll()
      scheduleStatusChange()
      context become active(a)

    case _ => stash()
  }

  def scheduleStatusChange() =
    context.system.scheduler.scheduleOnce((math.random * 20).toInt seconds, self, ChangeAppStatus)

  def active(app: App): Receive = {
    case UpdateApp(a, origin) =>
      val ref = sender()

      repo.saveOrUpdate(a).onComplete {
        case Success(a) => self ! (ref, UpdatedApp(a, origin))
        case f: Failure[_] => origin ! f
      }

    case (ref: ActorRef, msg @ UpdatedApp(a, _)) =>
      ref ! msg
      updates push a.id.get
      context become active(a)

    case DeleteApp(id: String, origin: ActorRef) =>
      val ref = sender()

      repo.remove(id).onComplete {
        case Success(_) => self ! (ref, DeletedApp(id, origin))
        case f: Failure[_] => origin ! f
      }

    case (ref: ActorRef, msg @ DeletedApp(id, _)) =>
      ref ! msg
      updates push id
      context become deleted

    case ChangeAppStatus =>
      self ! UpdateApp(app.copy(status = Some(app.status.fold("ok")(s => if (s =="ok") "error" else "ok"))), self)

    case UpdatedApp(a, _) =>
      scheduleStatusChange()

    case Failure(e) =>
      log.error(s"Can't update status of app ${app.id}", e)
  }

  def deleted: Receive = PartialFunction.empty

  def receive = initial
}

object AppMonitor {
  case class Use(app: App)

  case class CreateApp(app: App, origin: ActorRef)
  case class CreatedApp(app: App, origin: ActorRef)

  case class UpdateApp(app: App, origin: ActorRef)
  case class UpdatedApp(app: App, origin: ActorRef)

  case class DeleteApp(id: String, origin: ActorRef)
  case class DeletedApp(id: String, origin: ActorRef)

  case object ChangeAppStatus
}
