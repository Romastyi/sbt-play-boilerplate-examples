package com.github.romastyi.api

import java.io.File
import java.nio.file.{Files, StandardCopyOption}

import com.github.romastyi.api.domain.{UserJwtSession, UserModel}
import com.github.romastyi.api.model.{ApiResponse, NewPet, Pet, PetTag}
import com.github.romastyi.api.service.PetStoreService
import org.scalacheck.{Arbitrary, Gen}
import org.scalatestplus.play.FakeApplicationFactory
import pdi.jwt.JwtSession
import play.api.Application
import play.api.test.FakeRequest
import play.boilerplate.api.Tracer
import scaldi.Module
import scaldi.play.ScaldiApplicationBuilder

import scala.concurrent.Future
import scala.util.Random

trait CommonHelpers extends FakeApplicationFactory {

  import PetStoreService._

  def allPets = List(
    Pet(1, "Cat", None),
    Pet(2, "Dog", Some(Nil)),
    Pet(3, "Parrot", Some(PetTag.Mature :: Nil)),
    Pet(4, "Raccoon", Some(PetTag.Young :: Nil)),
    Pet(5, "Fish", Some(PetTag.Young :: PetTag.Mature :: Nil))
  )

  def petForm(id: Long, name: String, status: Option[String]): Pet = {
    Pet(
      id = id,
      name = name,
      tag = status.map(PetTag.withName).map(List(_))
    )
  }

  implicit class PetsOps(pets: List[Pet]) {

    def filterByTags(tags: Option[List[FindPetsTags.Value]]): List[Pet] = {
      tags match {
        case Some(values) =>
          pets.filter { pet =>
            pet.tag match {
              case Some(xs) => values.intersect(xs).nonEmpty
              case None => false
            }
          }
        case None => pets
      }
    }

    def withPager(pager: FindPetsPager): List[Pet] = pager match {
      case FindPetsPager(Some(drop), Some(limit)) =>
        pets.slice(drop, drop + limit)
      case FindPetsPager(Some(drop), None) =>
        pets.drop(drop)
      case FindPetsPager(None, Some(limit)) =>
        pets.take(limit)
      case FindPetsPager(None, None) =>
        pets
    }

  }

  def getRandomString(length: Int): String = Random.alphanumeric.take(length).mkString("")

  def mockService: PetStoreService[Future] = new PetStoreService[Future] {
    override def findPets(pager: FindPetsPager, tags: Option[List[FindPetsTags.Value]], logged: UserModel)(implicit tracer: Tracer): Future[FindPetsResponse] = {
      Future.successful(FindPetsOk(allPets.filterByTags(tags).withPager(pager), tracer))
    }
    override def addPet(pet: NewPet, logged: UserModel)(implicit tracer: Tracer): Future[AddPetResponse] = ???
    override def findPetById(id: Long, logged: UserModel)(implicit tracer: Tracer): Future[FindPetByIdResponse] = ???
    override def deletePet(id: Long, logged: UserModel)(implicit tracer: Tracer): Future[DeletePetResponse] = ???
    override def findPetByTag(tag: FindPetByTagTag.Value, logged: UserModel)(implicit tracer: Tracer): Future[FindPetByTagResponse] = ???
    override def updatePetWithForm(id: Long, name: String, status: Option[String], logged: UserModel)(implicit tracer: Tracer): Future[UpdatePetWithFormResponse] = {
      Future.successful(UpdatePetWithFormOk(petForm(id, name, status), tracer))
    }
    override def uploadFile(petId: Long, additionalMetadata: Option[String], file: File, logged: UserModel)(implicit tracer: Tracer): Future[UploadFileResponse] = {
      val tmpFile = Files.createTempFile(s"test-$petId-${getRandomString(20)}", ".tmp")
      Future.successful(UploadFileOk(ApiResponse(
        code = Some(200),
        `type` = additionalMetadata,
        message = Some(Files.move(file.toPath, tmpFile, StandardCopyOption.REPLACE_EXISTING).toAbsolutePath.toString)
      ), tracer))
    }
  }

  class FakeService extends Module {
    bind [PetStoreService[Future]] to mockService
  }

  override def fakeApplication(): Application = new ScaldiApplicationBuilder().prependModule(new FakeService).build()

  def withUser[A](request: FakeRequest[A], user: UserModel): FakeRequest[A] = {
    val session = UserJwtSession.newSession(user)
    request.withHeaders(JwtSession.REQUEST_HEADER_NAME -> (JwtSession.TOKEN_PREFIX + session.serialize))
  }

  implicit val pagerGen: Arbitrary[FindPetsPager] = Arbitrary(for {
    drop <- Gen.option(Gen.choose(-1, 10))
    limit <- Gen.option(Gen.choose(-1, 10))
  } yield FindPetsPager(drop = drop, limit = limit))

  implicit val findPetsTagsValueGen: Arbitrary[FindPetsTags.Value] = Arbitrary(Gen.oneOf(FindPetsTags.values.toIndexedSeq))

  implicit val petTagValueGen: Arbitrary[PetTag.Value] = Arbitrary(Gen.oneOf(PetTag.values.toIndexedSeq))

}
