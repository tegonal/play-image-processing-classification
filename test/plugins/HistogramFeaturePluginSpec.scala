package plugins

import org.junit.runner._
import org.specs2.mock.Mockito
import org.specs2.mutable._
import org.specs2.runner._
import org.specs2.specification.Scope
import org.mockito.Mockito._
import org.mockito.Matchers._
import dao.IdentificationDAOComponentMock
import models.Identification
import play.api.test._
import play.api.test.Helpers._
import org.opencv.core.Mat
import org.opencv.core.Core
import org.opencv.highgui.Highgui._
import models.Feature
import akka.testkit.TestKit
import akka.actor.ActorSystem
import akka.actor.Props
import Plugin._
import akka.testkit.TestProbe
import FeaturePlugin.FeatureResult

@RunWith(classOf[JUnitRunner])
class HistogramFeaturePluginSpec extends PlaySpecification {

  class Actors extends TestKit(ActorSystem("test")) with Scope

  "HuHistogramFeaturePlugin" should {
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME)

    "extract mean and standard deviations of RGB" in new Actors {
      val histogram = system.actorOf(Props[HistogramFeaturePlugin])
      val image = imread(getClass.getResource("/papilio_demoleus_cut.jpg").getPath)

      val probe = TestProbe()

      histogram ! Consumers(Set(probe.ref))

      histogram ! Image(image)

      probe.expectMsg(FeatureResult(Seq(
        Feature(0, "redMean", 46.033178782359116),
        Feature(1, "greenMean", 45.121868962852574),
        Feature(2, "blueMean", 47.08196721311476),
        Feature(3, "redStdDev", 70.02099360959271),
        Feature(4, "greenStdDev", 70.31453216864905),
        Feature(5, "blueStdDev", 70.56169608655291))))
    }

  }
}
