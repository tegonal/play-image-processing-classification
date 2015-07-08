import play.api._
import services.ClassificationService
import org.opencv.core.Core
import play.api.Play.current

object Global extends GlobalSettings {
  override def onStart(app: Application) = {
    // use sbt start to launch the app: https://github.com/playframework/playframework/issues/2212
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME)

    ClassificationService.initialize()
  }
}
