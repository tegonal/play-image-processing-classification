package services

import models.Identification
import models.FeatureVector
import scala.concurrent.Future
import plugins._
import plugins.Plugin._
import org.opencv.highgui.Highgui._
import plugins.FeaturePlugin._
import play.libs.Akka
import scala.concurrent.ExecutionContext.global
import akka.actor.Props
import akka.actor.Actor
import org.opencv.core.Mat
import models.Feature
import akka.actor.ActorRef
import akka.util.Timeout
import scala.concurrent.duration._
import scala.language.postfixOps
import akka.pattern.{ ask, pipe }
import dao.IdentificationDAOComponent
import dao.MongoIdentificationDAOComponent
import reactivemongo.bson.BSONDocument
import play.api.libs.concurrent.Execution.Implicits._
import reactivemongo.api.gridfs._
import reactivemongo.api.gridfs.Implicits._
import java.io.OutputStream
import java.io.FileOutputStream
import java.io.ByteArrayOutputStream
import org.opencv.highgui.Highgui
import org.opencv.core.MatOfByte
import org.opencv.core.Core
import play.api.Logger
import akka.actor.PoisonPill
import scala.concurrent.Await

/**
 * Arff file meta information header.
 */
case class ArffMeta(name: String, featureVector: FeatureVector, classes: Set[String]) {
  def toArff = {
    s"""@relation $name
       |${
      (featureVector.features map { f =>
        s"""@attribute ${f.name} real"""
      }).mkString("\n")
    }
       |@attribute class {${classes.map(c => "\"" + c + "\"").mkString(",")}}
       |@data
       |""".stripMargin
  }
}

/**
 * Arff file data entry with its class name.
 */
case class ArffData(featureVector: FeatureVector, className: String) {
  def toArff = {
    s"""${featureVector.features.map(_.value).mkString(",")},\"$className\""""
  }
}

case class FilePaths(paths: Seq[String])

/**
 * This service can be used to extract all feature vector data from the given file names. You need a manually classified
 * data set of images to use this service.
 * The file name will be used as the class of the given instance, underscores will be treated as spaces (e.g. class_name_...).
 * Each file of the FilePaths message will be sent to the FeatureVectorCalculator. The received FeatureVector message is
 * converted to its corresponding ArffData. The ArffData results will then be used to create the whole arff file content.
 * The result is sent back to the sender as string.
 */
class TrainingSetService extends Actor {
  val ClassName = """([^\.]*)\..*""".r

  def receive = {
    case FilePaths(paths: Seq[String]) =>
      val replyTo = sender
      val data = paths map { path =>
        val fileName = path.substring(path.lastIndexOf('/') + 1)

        val className = fileName match {
          case ClassName(name) => name.split("_").take(2).mkString(" ") // change this to support your naming convention
        }

        val calculator = context.actorOf(Props[FeatureVectorCalculator], s"featureVector_$fileName")

        val image = imread(path)

        implicit val timeout = Timeout(5 seconds)

        (calculator ? Image(image)).mapTo[FeatureVector] map { result =>
          Some(ArffData(result, className))
        } recover {
          case e =>
            Logger.warn(s"could not create arffdata: ${e.getMessage}")
            None
        }
      }

      Future.sequence(data) map { result =>
        val arffDatas = result.flatten
        val arff = new StringBuilder
        arff ++= ArffMeta("butterfly", arffDatas.head.featureVector, (arffDatas map (_.className)).toSet).toArff
        arffDatas foreach { arffData =>
          arff ++= arffData.toArff
          arff ++= "\n"
        }

        replyTo ! arff.toString
        self ! PoisonPill
      } recover {
        case e => Logger.warn(s"could not create arff file: ${e.getMessage}")
      }

      ()
  }

}