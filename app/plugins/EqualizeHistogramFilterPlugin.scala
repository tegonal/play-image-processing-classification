package plugins

import org.opencv.core._
import org.opencv.objdetect.CascadeClassifier
import org.opencv.imgproc.Imgproc._
import org.opencv.highgui.Highgui._
import org.opencv.core.CvType._
import scala.util.Random
import scala.collection.JavaConversions._
import akka.actor.Actor

/**
 * This plugin equalizes the histogram of an image.
 */
class EqualizeHistogramFilterPlugin extends FilterPlugin {
  def filter(image: Mat): Mat = {
    val result = new Mat
    equalizeHist(image, result)

    result
  }
}