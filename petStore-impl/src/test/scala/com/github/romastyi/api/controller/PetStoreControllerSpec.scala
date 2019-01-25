package com.github.romastyi.api.controller

import com.github.romastyi.api.domain.{UserJwtSession, UserModel}
import com.github.romastyi.api.model.json.PetStoreJson._
import com.github.romastyi.api.model.{NewPet, Pet, PetTag}
import com.github.romastyi.api.service.PetStoreService
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.prop.PropertyChecks
import org.scalatestplus.play.MixedPlaySpec
import pdi.jwt.JwtSession
import play.api.Application
import play.api.test.FakeRequest
import play.api.test.Helpers._
import scaldi.Module
import scaldi.play.ScaldiApplicationBuilder

import scala.concurrent.Future

class PetStoreControllerSpec extends MixedPlaySpec with PropertyChecks {

  import PetStoreController._
  import PetStoreService._

  def allPets = List(
    Pet(1, "Cat", None),
    Pet(2, "Dog", Some(Nil)),
    Pet(3, "Parrot", Some(PetTag.Mature :: Nil)),
    Pet(4, "Raccoon", Some(PetTag.Young :: Nil)),
    Pet(5, "Fish", Some(PetTag.Young :: PetTag.Mature :: Nil))
  )

  def petsWithPager(pager: FindPetsPager): List[Pet] = pager match {
    case FindPetsPager(Some(drop), Some(limit)) =>
      allPets.slice(drop, drop + limit)
    case FindPetsPager(Some(drop), None) =>
      allPets.drop(drop)
    case FindPetsPager(None, Some(limit)) =>
      allPets.take(limit)
    case FindPetsPager(None, None) =>
      allPets
  }

  def mockService: PetStoreService[Future] = new PetStoreService[Future] {
    override def findPets(pager: FindPetsPager, tags: Option[List[PetStoreService.FindPetsTags.Value]], logged: UserModel): Future[FindPetsResponse] = {
      Future.successful(FindPetsOk(petsWithPager(pager)))
    }
    override def addPet(pet: NewPet, logged: UserModel): Future[AddPetResponse] = ???
    override def findPetById(id: Long, logged: UserModel): Future[FindPetByIdResponse] = ???
    override def deletePet(id: Long, logged: UserModel): Future[DeletePetResponse] = ???
    override def findPetByTag(tag: PetStoreService.FindPetByTagTag.Value, logged: UserModel): Future[FindPetByTagResponse] = ???
  }

  class FakeService extends Module {
    bind [PetStoreService[Future]] to mockService
  }

  def withUser[A](request: FakeRequest[A], user: UserModel): FakeRequest[A] = {
    val session = UserJwtSession.newSession(user)
    request.withHeaders(JwtSession.REQUEST_HEADER_NAME -> (JwtSession.TOKEN_PREFIX + session.serialize))
  }

  implicit val pagerGen: Arbitrary[FindPetsPager] = Arbitrary(for {
    drop <- Gen.option(Gen.choose(-1, 10))
    limit <- Gen.option(Gen.choose(-1, 10))
  } yield FindPetsPager(drop = drop, limit = limit))

  val application: Application = new ScaldiApplicationBuilder().prependModule(new FakeService).build()

  "findPets with pager" in new App(application) {
    forAll { pager: FindPetsPager =>
      val Some(result) = route(app, withUser(FakeRequest(GET, "/api/pets?" + FindPetsPagerQueryBindable.unbind("pager", pager)), UserModel.Admin))
      status(result) must be(OK)
      contentType(result) must be(Some("application/json"))
      charset(result) must be(Some("utf-8"))
      contentAsJson(result).as[List[Pet]] must be(petsWithPager(pager))
    }
  }

}
