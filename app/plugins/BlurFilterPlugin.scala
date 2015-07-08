package plugins

import org.opencv.core._
import org.opencv.objdetect.CascadeClassifier
import org.opencv.imgproc.Imgproc._
import org.opencv.highgui.Highgui._
import org.opencv.core.CvType._
import scala.util.Random
import scala.collection.JavaConversions._
import scala.concurrent._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

/**
 * The blur plugin creates a blurred copy of the image received.
 */
class BlurFilterPlugin extends FilterPlugin {
  def filter(image: Mat): Mat = {
    val result = new Mat
    blur(image, result, new Size(5, 5))

    result
  }
}