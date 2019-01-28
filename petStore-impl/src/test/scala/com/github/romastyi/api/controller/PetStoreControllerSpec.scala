package com.github.romastyi.api.controller

import java.nio.file.{Files, Paths}

import com.github.romastyi.api.CommonHelpers
import com.github.romastyi.api.domain.UserModel
import com.github.romastyi.api.model.{ApiResponse, Pet, PetTag}
import com.github.romastyi.api.model.json.PetStoreJson._
import com.github.romastyi.api.service.PetStoreService
import org.scalacheck.Gen
import org.scalatest.prop.PropertyChecks
import org.scalatestplus.play.{BaseOneAppPerSuite, PlaySpec}
import play.api.libs.Files.SingletonTemporaryFileCreator
import play.api.mvc.MultipartFormData.FilePart
import play.api.mvc.{MultipartFormData, QueryStringBindable}
import play.api.test.FakeRequest
import play.api.test.Helpers._

class PetStoreControllerSpec extends PlaySpec with BaseOneAppPerSuite with PropertyChecks with CommonHelpers {

  import PetStoreController._
  import PetStoreService._

  "findPets with query parameters" in {
    forAll(minSuccessful(500)) { (pager: FindPetsPager, tagsSet: Option[Set[FindPetsTags.Value]]) =>
      val tags = tagsSet.map(_.toList)
      val urlPath = "/api/pets?" + FindPetsPagerQueryBindable.unbind("pager", pager) +
        "&" + QueryStringBindable.bindableOption[List[FindPetsTags.Value]].unbind("tags", tags)
      val Some(result) = route(app, withUser(FakeRequest(GET, urlPath), UserModel.Admin))
      status(result) must be(OK)
      contentType(result) must be(Some("application/json"))
      charset(result) must be(Some("utf-8"))
      contentAsJson(result).as[List[Pet]] must be(allPets.filterByTags(tags).withPager(pager))
    }
  }

  "updatePetWithForm" in {
    forAll(for {
      id <- Gen.oneOf(-1l, 0l, 1l)
      name <- Gen.alphaStr
      status <- Gen.option(Gen.oneOf(PetTag.values.toIndexedSeq).map(_.toString))
    } yield (id, name, status), minSuccessful(500)) { case (id, name, maybeStatus) =>
      val fakeRequest = FakeRequest(POST, s"/api/pets/$id").withBody(Map("name" -> Seq(name), "status" -> maybeStatus.toSeq))
      val Some(result) = route(app, withUser(fakeRequest, UserModel.Admin))
      status(result) must be(OK)
      contentType(result) must be(Some("application/json"))
      charset(result) must be(Some("utf-8"))
      contentAsJson(result).as[Pet] must be(petForm(id, name, maybeStatus))
    }
  }

  "uploadFile" in {
    val petId = 1l
    val additionalMetadata = getRandomString(100)
    val content = getRandomString(20).getBytes("UTF-8")
    val tmpFile = Files.createTempFile(null, null)
    Files.write(tmpFile, content)
    val temporaryFileCreator = SingletonTemporaryFileCreator
    val fakeRequest = FakeRequest(POST, s"/api/pet/$petId/uploadImage").withMultipartFormDataBody(MultipartFormData(
      dataParts = Map("additionalMetadata" -> Seq(additionalMetadata)),
      files = Seq(FilePart("file", "formuploaded", Some("application/octet-stream"), temporaryFileCreator.create(tmpFile))),
      badParts = Nil
    ))
    val Some(result) = route(app, withUser(fakeRequest, UserModel.Admin))
    status(result) must be(OK)
    contentType(result) must be(Some("application/json"))
    charset(result) must be(Some("utf-8"))
    contentAsJson(result).as[ApiResponse].code must be(Some(200))
    contentAsJson(result).as[ApiResponse].`type` must be(Some(additionalMetadata))
    contentAsJson(result).as[ApiResponse].message.map(
      file => Files.readAllBytes(Paths.get(file))
    ).getOrElse(Array.emptyByteArray) must be(content)
    Files.delete(tmpFile)
  }

}
