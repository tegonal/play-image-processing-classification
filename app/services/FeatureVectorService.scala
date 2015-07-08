package services

import java.io.ByteArrayOutputStream

import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

import org.opencv.core.MatOfByte
import org.opencv.highgui.Highgui

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.pattern.ask
import akka.util.Timeout
import dao.IdentificationDAOComponent
import dao.MongoIdentificationDAOComponent
import models.Feature
import models.FeatureVector
import models.Identification
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.libs.Akka
import plugins.BlurFilterPlugin
import plugins.ContoursFeaturePlugin
import plugins.FeaturePlugin.FeatureResult
import plugins.GreyScaleFilterPlugin
import plugins.HistogramFeaturePlugin
import plugins.HuMomentsFeaturePlugin
import plugins.Plugin.Consumers
import plugins.Plugin.Image
import plugins.ThresholdFilterPlugin
import reactivemongo.api.gridfs.Implicits.DefaultReadFileReader
import reactivemongo.bson.BSONDocument
import reactivemongo.bson.Producer.nameValue2Producer

trait FeatureVectorService {
  self: IdentificationDAOComponent =>

  def calculateFeatureVector(identification: Identification): Future[FeatureVector] = {
    val featureVectorCalculator = Akka.system().actorOf(Props[FeatureVectorCalculator])

    identificationDAO.gridFS.find(BSONDocument("identification" -> identification._id)).headOption flatMap {
      case Some(file) =>
        val out = new ByteArrayOutputStream
        val done = identificationDAO.gridFS.readToOutputStream(file, out)

        Await.ready(done, 30 seconds)

        val matBuffer = new MatOfByte()
        matBuffer.fromArray(out.toByteArray(): _*)
        val image = Highgui.imdecode(matBuffer, Highgui.CV_LOAD_IMAGE_UNCHANGED)

        implicit val timeout = Timeout(30 seconds)

        (featureVectorCalculator ? Image(image)).mapTo[FeatureVector]

      case None => throw new RuntimeException(s"there is no file for the given identification ${identification._id}")
    }
  }
}

object FeatureVectorService extends FeatureVectorService with MongoIdentificationDAOComponent

class FeatureVectorCalculator extends Actor {
  var features = Seq.empty[Feature]
  var featurePlugins = Set.empty[ActorRef]
  var service = ActorRef.noSender

  val grey = context.actorOf(Props[GreyScaleFilterPlugin], "grey")
  val blur = context.actorOf(Props[BlurFilterPlugin], "blur")
  val binary = context.actorOf(Props[ThresholdFilterPlugin], "binary")

  val histogram = context.actorOf(Props[HistogramFeaturePlugin], "histogram")
  val contours = context.actorOf(Props[ContoursFeaturePlugin], "contours")
  val hu = context.actorOf(Props[HuMomentsFeaturePlugin], "hu")
  featurePlugins += histogram
  featurePlugins += contours
  featurePlugins += hu

  /**
   * Configuration of how plugins are depending on each other
   */
  def prepareDependencies() = {
    grey ! Consumers(Set(blur))
    blur ! Consumers(Set(binary, hu))
    binary ! Consumers(Set(contours))
    histogram ! Consumers(Set(self))
    contours ! Consumers(Set(self))
    hu ! Consumers(Set(self))
  }

  /**
   * Entry point
   * @param image
   */
  def sendInitialMessages(image: Image) = {
    Logger.debug("Sending initial message")
    histogram ! image
    grey ! image
  }

  def receive = {
    case image @ Image(_) =>
      service = sender
      prepareDependencies()
      sendInitialMessages(image)

    case FeatureResult(result) =>
      Logger.debug(s"Got feature result $result")
      features ++= result
      featurePlugins -= sender

      if (featurePlugins.isEmpty) {
        service ! FeatureVector(features.sortBy(_.position))
        context.stop(self)
      }
  }
}