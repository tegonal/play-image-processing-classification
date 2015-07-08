package controllers

import java.io.File
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import org.junit.runner._
import org.specs2.mock.Mockito
import org.specs2.mutable._
import org.specs2.runner._
import org.mockito.Mockito._
import org.mockito.Matchers._
import dao.IdentificationDAOComponentMock
import models.Identification
import play.api.libs.Files.TemporaryFile
import play.api.mvc.MultipartFormData
import play.api.mvc.MultipartFormData._
import play.api.test._
import play.api.test.Helpers._
import reactivemongo.bson.BSONObjectID
import play.api.Logger
import java.nio.file._

@RunWith(classOf[JUnitRunner])
class IdentificationControllerSpec extends Specification {

  "IdentificationController" should {

    "return created" in new WithApplication {
      val controller = new IdentificationControllerMock

      when(controller.identificationDAO.insert(any[Identification])(any[ExecutionContext])) thenReturn Future.successful(BSONObjectID("5433baed2600002600455999"))

      val result = controller.create()(FakeRequest(POST, "/identifications"))
      status(result) must equalTo(CREATED)
      contentType(result) must beSome.which(_ == "application/json")
      val identification = contentAsJson(result).as[Identification]
      identification.imageUrl === s"/identifications/${identification._id.stringify}/image"
    }

    "return existing identification" in new WithApplication {
      val controller = new IdentificationControllerMock
      val id = "5433baed2600002600455999"
      when(controller.identificationDAO.get(BSONObjectID(id))) thenReturn Future.successful(Some((Identification(BSONObjectID(id), ""), BSONObjectID(id))))
      val result = controller.get(id)(FakeRequest(GET, s"/identifications/$id"))
      status(result) must equalTo(OK)
    }

    "upload image" in new WithApplication {
      val controller = new IdentificationControllerMock
      val file = new File(getClass.getResource("/papilio_demoleus_cut.jpg").getFile)
      Files.copy(Paths.get(file.getPath()), Paths.get("/tmp/upload.jpg"))
      val tempFile = TemporaryFile(new File("/tmp/upload.jpg"))
      val part = FilePart("file", "papilio_demoleus_cut.jpg", None, tempFile)
      val formData = MultipartFormData(Map(), Seq(part), Nil, Nil)
      val request = FakeRequest(POST, "/identifications/5433baed2600002600455999/image").withBody(formData)

      val result = (controller.uploadImage("5433baed2600002600455999")(request)).run

      status(result) must equalTo(CREATED)
    }.pendingUntilFixed("multipart formdata upload seems to fail using a FakeRequest")

  }
}

class IdentificationControllerMock extends IdentificationController with IdentificationDAOComponentMock