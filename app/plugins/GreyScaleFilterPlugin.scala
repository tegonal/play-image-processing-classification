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
 * This plugin converts an image into grayscale mode.
 */
class GreyScaleFilterPlugin extends FilterPlugin {
  def filter(image: Mat): Mat = {
    val greyImage = new Mat
    cvtColor(image, greyImage, COLOR_BGR2GRAY)

    greyImage
  }
}