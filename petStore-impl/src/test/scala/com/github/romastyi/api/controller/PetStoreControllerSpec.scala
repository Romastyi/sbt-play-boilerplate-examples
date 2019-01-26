package com.github.romastyi.api.controller

import com.github.romastyi.api.CommonHelpers
import com.github.romastyi.api.domain.UserModel
import com.github.romastyi.api.model.Pet
import com.github.romastyi.api.model.json.PetStoreJson._
import com.github.romastyi.api.service.PetStoreService
import org.scalatest.prop.PropertyChecks
import org.scalatestplus.play.{BaseOneAppPerSuite, PlaySpec}
import play.api.mvc.QueryStringBindable
import play.api.test.FakeRequest
import play.api.test.Helpers._

class PetStoreControllerSpec extends PlaySpec with BaseOneAppPerSuite with PropertyChecks with CommonHelpers {

  import PetStoreController._
  import PetStoreService._

  "findPets with query parameters" in {
    forAll(minSuccessful(100)) { (pager: FindPetsPager, tagsSet: Option[Set[FindPetsTags.Value]]) =>
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

}
