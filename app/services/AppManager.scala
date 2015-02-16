package services

import akka.actor._
import models.App
import scaldi.Injector
import scaldi.akka.AkkaInjectable._

import scala.util.Failure
import akka.pattern.pipe
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class AppsManager(implicit inj: Injector) extends Actor with Stash with ActorLogging {
  import AppsManager._


  val repo = inject [AppRepo]
  val appProps = injectActorProps[AppMonitor]

  var apps: List[(String, ActorRef)] = Nil

  def initial: Receive = {
    case Reload =>
      repo.list map Loaded.apply pipeTo self

    case Loaded(list) =>
      apps.foreach(_._2 ! PoisonPill)

      apps = list map { a =>
        val monitor = context.actorOf(appProps)
        context watch monitor
        monitor ! AppMonitor.Use(a)
        a.id.get -> monitor
      }

      unstashAll()
      context.become(active)
    case Failure(e) =>
      log.error("Can't load apps! Rescheduling", e)

      context.system.scheduler.scheduleOnce(5 seconds, self, Reload)
    case _ => stash()
  }

  def active: Receive = {
    case CreateApp(app) =>
      val monitor = context.actorOf(appProps)

      monitor ! AppMonitor.CreateApp(app, sender())

    case AppMonitor.CreatedApp(app, origin) =>
      val monitor = sender()

      context watch monitor

      apps = apps :+ (app.id.get, monitor)

      origin ! app

    case msg @ UpdateApp(app) =>
      app.id.fold (sender() ! Failure(new IllegalStateException("No ID for update"))) { id =>
        apps.find(_._1 == id).get._2 ! AppMonitor.UpdateApp(app, sender())
      }

    case msg @ AppMonitor.UpdatedApp(app, origin) =>
      origin ! app

    case DeleteApp(id) =>
      apps.find(_._1 == id).get._2 ! AppMonitor.DeleteApp(id, sender())

    case AppMonitor.DeletedApp(id, origin) =>
      apps.find(_._1 == id) foreach (_._2 ! PoisonPill)

      apps = apps.filterNot(_._1 == id)

      origin ! id

    case Terminated(ref) =>
      println("TODO", ref)

  }

  def receive = initial

  override def preStart(): Unit = {
    self ! Reload
  }

  override def postRestart(reason: Throwable): Unit = {
    preStart()
  }
}

object AppsManager {
  case class CreateApp(app: App)
  case class UpdateApp(app: App)
  case class DeleteApp(id: String)

  private case object Reload
  private case class Loaded(apps: List[App])
  private case class CreatedApp(app: App)
}
