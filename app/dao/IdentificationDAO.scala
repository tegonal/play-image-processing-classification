package dao

import models.Identification
import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent._
import play.modules.reactivemongo.json.collection.JSONCollection
import play.modules.reactivemongo.json.BSONFormats._
import play.api.libs.json._
import reactivemongo.bson.BSONObjectID

trait IdentificationDAOComponent {
  val identificationDAO: IdentificationDAO
}

trait MongoIdentificationDAOComponent extends IdentificationDAOComponent {
  val identificationDAO = new IdentificationMongoDAO
}

trait IdentificationDAO extends BaseDAO[Identification] {
}

class IdentificationMongoDAO extends BaseReactiveMongoDAO[Identification] with IdentificationDAO {
  def coll = db.collection[JSONCollection]("Identification")
}
