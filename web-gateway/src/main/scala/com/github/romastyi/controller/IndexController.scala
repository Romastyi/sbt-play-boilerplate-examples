package com.github.romastyi.controller

import com.github.romastyi.api.controller.UserAuthConfig
import com.github.romastyi.api.domain.{UserAuthority, UserModel, UserRole}
import com.github.romastyi.api.model.Pet
import com.github.romastyi.api.service.PetStoreService
import jp.t2v.lab.play2.auth.AuthElement
import play.api.Logger
import play.api.mvc.{Action, AnyContent, Controller}
import play.api.libs.concurrent.Execution.Implicits._
import scaldi.{Injectable, Injector}

import scala.concurrent.Future

class IndexController(implicit val inj: Injector)
  extends Controller
    with UserAuthConfigImpl
    with AuthElement
    with Injectable {

  import PetStoreService._

  val logger = Logger(this.getClass.getName)
  val petStore: PetStoreService = inject[PetStoreService]

  private def getList(user: UserModel): Future[List[Pet]] = {
    petStore.findPets(None, None, user).map {
      case FindPetsOk(list) =>
        list
      case FindPetsDefault(error, status) =>
        logger.error(s"STATUS: $status, ERROR: $error")
        Nil
    }.recover {
      case cause =>
        logger.error("Could not get pets list", cause)
        Nil
    }
  }

  def index: Action[AnyContent] = AsyncStack(AuthorityKey -> UserAuthority(UserRole.all)) { implicit request =>
    for {
      list <- getList(loggedIn)
    } yield Ok(html.petStore(list))
  }

}
