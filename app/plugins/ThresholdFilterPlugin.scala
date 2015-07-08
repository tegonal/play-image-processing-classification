package plugins

import org.opencv.core._
import org.opencv.objdetect.CascadeClassifier
import org.opencv.imgproc.Imgproc._
import org.opencv.highgui.Highgui._
import org.opencv.core.CvType._
import scala.util.Random
import scala.collection.JavaConversions._

/**
 * The threshold plugin transforms the given image in a binary image.
 */
class ThresholdFilterPlugin extends FilterPlugin {
  def filter(image: Mat): Mat = {
    val result = new Mat
    threshold(image, result, 180, 255, THRESH_BINARY)

    result
  }
}