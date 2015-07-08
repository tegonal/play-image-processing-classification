package models

import play.api.libs.json.Json
import reactivemongo.bson.BSONObjectID
import models.BaseFormats._

/**
 * A class with its probability
 */
case class `Class`(name: String, probability: Double)

object `Class` {
  implicit val classFormat = Json.format[`Class`]
}

/**
 * A classification is represented by a sequence of `Class`es.
 */
case class Classification(classes: Seq[`Class`])

object Classification {
  implicit val classificationFormat = Json.format[Classification]
}