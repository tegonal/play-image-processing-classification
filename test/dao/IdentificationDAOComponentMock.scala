package dao

import org.specs2.mock.Mockito

trait IdentificationDAOComponentMock extends IdentificationDAOComponent with Mockito {
  val identificationDAO = mock[IdentificationDAO]
}