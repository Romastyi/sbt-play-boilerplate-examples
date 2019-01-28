package com.github.romastyi.api.client

import java.net.URI
import java.nio.file.{Files, Paths}

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.github.romastyi.api.CommonHelpers
import com.github.romastyi.api.controller.PetStoreController
import com.github.romastyi.api.domain.UserModel
import com.github.romastyi.api.model.PetTag
import com.github.romastyi.api.service.PetStoreService
import org.scalacheck.Gen
import org.scalatest.prop.PropertyChecks
import org.scalatestplus.play.{BaseOneServerPerSuite, PlaySpec}
import play.api.libs.ws.ahc.StandaloneAhcWSClient
import play.api.mvc.QueryStringBindable
import play.api.test.Helpers._
import play.boilerplate.api.client.dsl.{QueryParameter, ServiceLocator}
import play.shaded.ahc.org.asynchttpclient.AsyncHttpClient

import scala.concurrent.ExecutionContext

class PetStoreClientSpec extends PlaySpec with BaseOneServerPerSuite with PropertyChecks with CommonHelpers {

  import PetStoreController._
  import PetStoreService._

  def fakeClient: PetStoreClient = {
    implicit val sys: ActorSystem = app.injector.instanceOf[ActorSystem]
    implicit val mat: ActorMaterializer = ActorMaterializer()
    implicit val ec: ExecutionContext = app.injector.instanceOf[ExecutionContext]
    implicit val ws: StandaloneAhcWSClient = new StandaloneAhcWSClient(app.injector.instanceOf[AsyncHttpClient])
    implicit val sl: ServiceLocator = ServiceLocator.Static {
      case _ => new URI(s"http://localhost:$port")
    }
    new PetStoreClient(PetStoreClientRequestHandler)
  }

  private def trimAmp(s: String): String = {
    s.dropWhile(_ == '&').reverse.dropWhile(_ == '&').reverse
  }

  private def parseQueryParams(query: String): Map[String, Seq[String]] = {
    query.split('&').map(_.split('=')).groupBy(_.head).mapValues { arr =>
      val values = arr.flatMap(_.tail)
      if (values.isEmpty) Vector("") else values.toVector
    }
  }

  "FindPetsPager render" in {
    forAll { pager: FindPetsPager =>
      val renderer = fakeClient.FindPetsPagerQueryParameter
      val bindable = FindPetsPagerQueryBindable
      val query = renderer.render("pager", pager)
      val params = parseQueryParams(query)
      query must be(trimAmp(bindable.unbind("pager", pager)))
      bindable.bind("pager", params) must be(Some(Right(pager.copy(
        drop = pager.drop match {
          case None => Some(-1) // default value
          case other => other
        }
      ))))
    }
  }

  "FindPetsTags render" in {
    forAll { tags: Option[List[FindPetsTags.Value]] =>
      val renderer: QueryParameter[Option[List[FindPetsTags.Value]]] = QueryParameter.optionQueryParameter(fakeClient.FindPetsTagsValueListQueryParameter)
      val bindable: QueryStringBindable[Option[List[FindPetsTags.Value]]] = QueryStringBindable.bindableOption[List[FindPetsTags.Value]]
      val query = renderer.render("tags", tags)
      val params = parseQueryParams(query)
      query must be(trimAmp(bindable.unbind("tags", tags)))
      bindable.bind("tags", params) must be(Some(Right(tags)))
    }
  }

  "findPets with query parameters" in {
    forAll(minSuccessful(300)) { (pager: FindPetsPager, tagsSet: Option[Set[FindPetsTags.Value]]) =>
      val tags = tagsSet.map(_.toList)
      await(fakeClient.findPets(pager, tags, UserModel.Admin)) must be(FindPetsOk(allPets.filterByTags(tags).withPager(pager)))
    }
  }

  "updatePetWithForm" in {
    forAll(for {
      id <- Gen.oneOf(-1l, 0l, 1l)
      name <- Gen.alphaStr
      status <- Gen.option(Gen.oneOf(PetTag.values.toIndexedSeq).map(_.toString))
    } yield (id, name, status), minSuccessful(300)) { case (id, name, maybeStatus) =>
      await(fakeClient.updatePetWithForm(id, name, maybeStatus, UserModel.Admin)) must be(UpdatePetWithFormOk(petForm(id, name, maybeStatus)))
    }
  }

  "uploadFile" in {
    val petId = 2l
    val additionalMetadata = getRandomString(100)
    val content = getRandomString(20).getBytes("UTF-8")
    val tmpFile = Files.createTempFile(null, null)
    Files.write(tmpFile, content)
    await(fakeClient.uploadFile(petId, Some(additionalMetadata), tmpFile.toFile, UserModel.Admin)) match {
      case UploadFileOk(body) =>
        body.code must be(Some(200))
        body.`type` must be(Some(additionalMetadata))
        body.message.map(
          file => Files.readAllBytes(Paths.get(file))
        ).getOrElse(Array.emptyByteArray) must be(content)
      case other =>
        fail(s"Wrong response class (${other.getClass.getName})")
    }
    Files.delete(tmpFile)
  }

}
