import play.api.GlobalSettings
import scaldi.play.{ControllerInjector, ScaldiSupport}
import services.AppModule

object Global extends GlobalSettings with ScaldiSupport {
  def applicationModule = new AppModule :: new ControllerInjector
}

