package plugins

import org.opencv.core._
import org.opencv.objdetect.CascadeClassifier
import org.opencv.imgproc.Imgproc._
import org.opencv.highgui.Highgui._
import org.opencv.core.CvType._
import scala.util.Random
import scala.collection.JavaConversions._
import akka.actor.Actor
import akka.actor.Props
import play.api.Logger

object ExtractionFilterPlugin {
  def props(cascadeFilePath: String): Props = Props(classOf[ExtractionFilterPlugin], cascadeFilePath)
}

class ExtractionFilterPlugin(cascadeFilePath: String) extends FilterPlugin {

  def filter(image: Mat): Mat = {
    // use this plugin to perform an extraction of searched objects.
    // For example using a org.opencv.objdetect.CascadeClassifier with a given HAAR/LBP xml file.

    image
  }
}