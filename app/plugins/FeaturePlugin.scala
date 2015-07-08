package plugins

import org.opencv.core.Mat
import models.Feature
import Plugin._

import FeaturePlugin._
import akka.actor.PoisonPill

object FeaturePlugin {
  case class FeatureResult(result: Seq[Feature])
}

/**
 * A FeaturePlugin reacts to the image message with a FeatureResult containing the calculated sequence of Features.
 * After calculation the result is sent to the registered consumers.
 */
trait FeaturePlugin extends Plugin {
  def calculate(image: Mat): Seq[Feature]

  def handleImageMessage(image: Image) = image match {
    case Image(mat) =>
      val result = calculate(mat)
      consumers foreach (_ ! FeatureResult(result))
  }
}