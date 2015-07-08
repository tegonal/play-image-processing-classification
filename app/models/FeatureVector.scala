package models

import play.api.libs.json.Json
import reactivemongo.bson.BSONObjectID
import models.BaseFormats._
import BaseFormats.EnumUtils._

/**
 * A feature with its name and value represented as double.
 * The position is used to guarantee the order within the sequence
 * representing the whole feature vector using asynchronous computation.
 */
case class Feature(position: Int, name: String, value: Double)

object Feature {
  implicit val featureFormat = Json.format[Feature]
}

case class FeatureVector(features: Seq[Feature])

object FeatureVector {
  implicit val featureVectorFormat = Json.format[FeatureVector]
}
