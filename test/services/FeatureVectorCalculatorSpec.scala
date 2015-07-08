package services

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
import akka.testkit.TestProbe
import plugins.FeaturePlugin.FeatureResult
import plugins.Plugin.Image
import akka.testkit.ImplicitSender
import models.FeatureVector

@RunWith(classOf[JUnitRunner])
class FeatureVectorCalculatorSpec extends PlaySpecification {

  class Actors extends TestKit(ActorSystem("test")) with Scope with ImplicitSender

  "FeatureVectorCalculator" should {
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME)

    "caculateFeatureVector on receive image message" in new Actors {
      val calculator = system.actorOf(Props[FeatureVectorCalculator])
      val image = imread(getClass.getResource("/papilio_demoleus_cut.jpg").getPath)

      calculator ! Image(image)

      expectMsg(FeatureVector(Seq(
        Feature(0, "redMean", 46.033178782359116),
        Feature(1, "greenMean", 45.121868962852574),
        Feature(2, "blueMean", 47.08196721311476),
        Feature(3, "redStdDev", 70.02099360959271),
        Feature(4, "greenStdDev", 70.31453216864905),
        Feature(5, "blueStdDev", 70.56169608655291),
        Feature(6, "segments", 77.0),
        Feature(7, "medianAreaOfSegments", 74.5),
        Feature(8, "hu1", 0.7586653709212051),
        Feature(9, "hu2", 0.5294922572208498),
        Feature(10, "hu3", 0.047134844405231836),
        Feature(11, "hu4", 0.0371983418976044),
        Feature(12, "hu5", 0.0015563971379822814),
        Feature(13, "hu6", 0.024583287120933217),
        Feature(14, "hu7", -6.124536255751141E-5))))
    }

  }
}
