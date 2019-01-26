package com.github.romastyi.api.client

import java.net.URI

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.github.romastyi.api.CommonHelpers
import com.github.romastyi.api.controller.PetStoreController
import com.github.romastyi.api.domain.UserModel
import com.github.romastyi.api.service.PetStoreService
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

  "FindPetsPager render" in {
    forAll { pager: FindPetsPager =>
      fakeClient.FindPetsPagerQueryParameter.render("pager", pager) must be(trimAmp(FindPetsPagerQueryBindable.unbind("pager", pager)))
    }
  }

  "FindPetsTags render" in {
    forAll { tags: Option[List[FindPetsTags.Value]] =>
      QueryParameter.optionQueryParameter(fakeClient.FindPetsTagsValueListQueryParameter).render("tags", tags) must be(trimAmp(QueryStringBindable.bindableOption[List[FindPetsTags.Value]].unbind("tags", tags)))
    }
  }

  "findPets with query parameters" in {
    forAll { tags: Option[List[FindPetsTags.Value]] =>
      val pager = FindPetsPager(Some(1), None)
      await(fakeClient.findPets(pager, tags, UserModel.Admin)) must be(FindPetsOk(allPets.filterByTags(tags).withPager(pager)))
    }
  }

}
