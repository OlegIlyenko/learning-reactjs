package services

import akka.actor.{ActorRef, Stash, Actor}
import models.App
import scaldi.Injector
import scaldi.akka.AkkaInjectable._

import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

class AppMonitor(implicit inj: Injector) extends Actor with Stash {
  import AppMonitor._

  val repo = inject [AppRepo]

  def initial: Receive = {
    case Use(a) =>
      unstashAll()
      context become active(a)

    case CreateApp(a, origin) =>
      val ref = sender()

      repo.saveOrUpdate(a).onComplete {
        case Success(a) => self ! (ref, CreatedApp(a, origin))
        case f: Failure[_] => origin ! f
      }

    case (ref: ActorRef, msg @ CreatedApp(a, _)) =>
      ref ! msg

      unstashAll()
      context become active(a)

    case _ => stash()
  }

  def active(app: App): Receive = {
    case UpdateApp(a, origin) =>
      val ref = sender()

      repo.saveOrUpdate(a).onComplete {
        case Success(a) => self ! (ref, UpdatedApp(a, origin))
        case f: Failure[_] => origin ! f
      }

    case (ref: ActorRef, msg @ UpdatedApp(a, _)) =>
      ref ! msg
      context become active(a)

    case DeleteApp(id: String, origin: ActorRef) =>
      val ref = sender()

      repo.remove(id).onComplete {
        case Success(_) => self ! (ref, DeletedApp(id, origin))
        case f: Failure[_] => origin ! f
      }

    case (ref: ActorRef, msg @ DeletedApp(id, _)) =>
      ref ! msg
      context become deleted

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
}
