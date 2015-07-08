package models

import play.api.libs.json._
import scala.collection.mutable.ArrayBuffer
import java.util.Locale
import scala.util.Try
import scala.language.implicitConversions
import reactivemongo.bson.BSONObjectID

object BaseFormats {
  implicit object BSONObjectIDFormat extends Format[BSONObjectID] {

    def writes(objectId: BSONObjectID): JsValue = {
      Json.obj("$oid" -> JsString(objectId.stringify))
    }

    def reads(json: JsValue): JsResult[BSONObjectID] = json match {
      case JsString(x) => {
        val maybeOID: Try[BSONObjectID] = BSONObjectID.parse(x)
        if (maybeOID.isSuccess) JsSuccess(maybeOID.get) else {
          JsError("Expected BSONObjectID as JsString")
        }
      }

      case JsObject(Seq((_, oid))) =>
        reads(oid)
      case _ => JsError("Expected BSONObjectID as JsString")
    }
  }

  implicit object EnumUtils {
    def enumReads[E <: Enumeration](enum: E): Reads[E#Value] =
      new Reads[E#Value] {
        def reads(json: JsValue): JsResult[E#Value] = json match {
          case JsString(s) => {
            try {
              JsSuccess(enum.withName(s))
            } catch {
              case _: NoSuchElementException =>
                JsError(s"Enumeration expected of type: '${enum.getClass}',but it does not appear to contain the value: '$s'")
            }
          }
          case _ => JsError("String value expected")
        }
      }

    implicit def enumWrites[E <: Enumeration]: Writes[E#Value] =
      new Writes[E#Value] {
        def writes(v: E#Value): JsValue = JsString(v.toString)
      }

    implicit def enumFormat[E <: Enumeration](enum: E): Format[E#Value] = {
      Format(enumReads(enum), enumWrites)
    }
  }
}