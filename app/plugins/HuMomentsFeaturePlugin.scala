package plugins

import org.opencv.core._
import org.opencv.objdetect.CascadeClassifier
import org.opencv.imgproc.Imgproc._
import org.opencv.highgui.Highgui._
import org.opencv.core.CvType._
import scala.util.Random
import scala.collection.JavaConversions._
import models.Feature

/**
 * Calculate the Hu-Moments of an image.
 */
class HuMomentsFeaturePlugin extends FeaturePlugin {
  def calculate(image: Mat): Seq[Feature] = {
    val contours: java.util.List[MatOfPoint] = new java.util.ArrayList[MatOfPoint]
    val hierarchy = new Mat
    val cannyOutput = new Mat
    val contoursImage = Mat.zeros(image.size, CV_8UC3)
    val thresh = 120

    Canny(image, cannyOutput, thresh, thresh * 2, 3, false)

    findContours(cannyOutput, contours, hierarchy, RETR_TREE, CHAIN_APPROX_SIMPLE, new Point(0, 0))

    val mom = moments(contours.head, false)
    val hu = new Mat
    HuMoments(mom, hu)

    for (i <- 0 until hu.rows) yield Feature(i + 8, s"hu${i + 1}", hu.get(i, 0)(0))
  }
}