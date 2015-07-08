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
import play.api.Logger
import java.io.File
import akka.pattern.{ ask, pipe }
import scala.concurrent.duration._
import java.nio.file.Files
import java.nio.file.Paths

/**
 * This spec can be used as a training set generator using a directory full of already classified images (the file name is the class).
 *
 * The trainingset
 */
@RunWith(classOf[JUnitRunner])
class TrainingSetServiceSpec extends PlaySpecification {

  val directory = new File("../trainingset") // the directory containing all the classified images of the training set

  class Actors extends TestKit(ActorSystem("test")) with Scope with ImplicitSender

  "FeatureVectorCalculator" should {
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME)

    "create arff format out of images" in new Actors {
      val files = directory.listFiles filter (_.isFile) map (_.getAbsolutePath)
      val trainingSetService = system.actorOf(Props[TrainingSetService], "trainingSet")

      trainingSetService ! FilePaths(files)

      val result = receiveOne(30 seconds)

      // create a new arff classification file from existing manually classified images.
      Files.write(Paths.get("./conf/trainingset.arff"), result.toString.getBytes);

      result must not be empty
    }.pendingUntilFixed("unlock to create a new trainigset arff file")

  }
}
