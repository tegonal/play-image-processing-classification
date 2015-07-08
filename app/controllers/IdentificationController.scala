package controllers

import java.io.File
import play.api._
import play.api.mvc._
import dao.IdentificationDAOComponent
import dao.MongoIdentificationDAOComponent
import play.api.mvc.Action
import play.api.mvc.Controller
import models.Identification
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.Json
import reactivemongo.bson.BSONObjectID
import play.modules.reactivemongo.MongoController
import reactivemongo.api.gridfs.Implicits.DefaultReadFileReader
import reactivemongo.bson.BSONObjectID
import reactivemongo.bson.BSONDocument
import services.FeatureVectorService
import play.modules.reactivemongo.json.BSONFormats._
import services.ClassificationService
import scala.concurrent.Future
import reactivemongo.api.gridfs.ReadFile
import reactivemongo.bson.BSONValue

trait IdentificationController extends Controller with MongoController {
  this: IdentificationDAOComponent =>

  def get(id: String) = Action.async {
    identificationDAO.get(BSONObjectID(id)) map {
      case Some((result, _)) => Ok(Json.toJson(result))
      case None              => NotFound(s"There is no identification with the id $id")
    } recover {
      case e => InternalServerError(e.getMessage)
    }
  }

  /**
   * Create an Identification stub that contains the imageUrl where the caller can then upload the image data.
   * The resulting identification contains the url to which the user can then upload the image.
   */
  def create() = Action.async { request =>
    createIdentification() map {
      case (_, identification) =>
        Created(Json.toJson(identification))
    }
  }

  private def createIdentification() = {
    val id = BSONObjectID.generate
    val identification = Identification(id, s"/identifications/${id.stringify}/image")

    identificationDAO.insert(identification) map { id =>
      (id.stringify, identification)
    }
  }

  /**
   * Upload the image data, start a feature vector computation and then the classification.
   * FIXME: re-uploading to the same id does not seem to overwrite the file in gridFS
   */
  def uploadImage(id: String) = Action.async(gridFSBodyParser(identificationDAO.gridFS)) { request =>
    request.body.files.head.ref flatMap { file =>
      storeImage(id, file) map {
        case Some(identification) => Ok(Json.toJson(identification))
        case None                 => NotFound("identification not found")
      }
    }
  }

  /**
   * Upload the image data directly creating an identification on the way.
   */
  def uploadImageDirectly() = Action.async(gridFSBodyParser(identificationDAO.gridFS)) { request =>
    request.body.files.head.ref flatMap { file =>
      createIdentification() flatMap {
        case (id, identification) =>
          storeImage(id, file) map {
            case Some(identification) => Ok(Json.toJson(identification))
            case None                 => BadRequest("could not create identification or store image")
          }
      }
    }
  }

  def storeImage(id: String, file: ReadFile[BSONValue]) = {
    identificationDAO.gridFS.files.update(
      BSONDocument("_id" -> file.id),
      BSONDocument("$set" -> BSONDocument("identification" -> BSONObjectID(id)))) flatMap { lastError =>
        identificationDAO.get(BSONObjectID(id)) flatMap {
          case Some((identification, _)) =>
            startFeatureVectorService(identification) flatMap { withFeatureVector =>
              startClassificationService(withFeatureVector) map { result =>
                Some(result)
              }
            }
          case None =>
            Future.successful(None)
        }
      }
  }

  private def startFeatureVectorService(identification: Identification) = {
    FeatureVectorService.calculateFeatureVector(identification) flatMap { result =>
      identificationDAO.update(Json.obj("_id" -> identification._id), Json.obj("$set" -> Json.obj("featureVector" -> result))) map { updated =>
        Logger.debug(s"updated the identification ${identification._id} with the featureVector: $result")
        identification.copy(featureVector = Some(result))
      } recover {
        case e =>
          Logger.error(s"failed to update the identification ${identification._id}, cause: ${e.getMessage}")
          identification
      }
    } recover {
      case e =>
        Logger.error(s"calculation of feature vector failed, cause: ${e.getMessage}")
        identification
    }
  }

  private def startClassificationService(identification: Identification) = {
    ClassificationService.classify(identification) flatMap { result =>
      identificationDAO.update(Json.obj("_id" -> identification._id), Json.obj("$set" -> Json.obj("classification" -> result))) map { updated =>
        Logger.debug(s"updated the identification ${identification._id} with the classification: $result")
        identification.copy(classification = Some(result))
      } recover {
        case e =>
          Logger.error(s"failed to update the identification ${identification._id}, cause: ${e.getMessage}")
          identification
      }
    } recover {
      case e =>
        Logger.error(s"classification failed, cause: ${e.getMessage}")
        identification
    }
  }

}

object IdentificationController extends IdentificationController with MongoIdentificationDAOComponent
