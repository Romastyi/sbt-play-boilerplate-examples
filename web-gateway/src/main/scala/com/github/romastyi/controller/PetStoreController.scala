package com.github.romastyi.controller

import com.github.romastyi.api.domain.{UserAuthority, UserModel, UserRole}
import com.github.romastyi.api.model.{NewPet, Pet}
import com.github.romastyi.api.service.PetStoreService
import jp.t2v.lab.play2.auth.AuthElement
import play.api.data._
import play.api.data.Forms._
import play.api.Logger
import play.api.mvc.{Action, AnyContent, Controller}
import play.api.libs.concurrent.Execution.Implicits._
import scaldi.{Injectable, Injector}

import scala.concurrent.Future

class PetStoreController(implicit val inj: Injector)
  extends Controller
    with UserAuthConfigImpl
    with AuthElement
    with Injectable {

  import PetStoreService._

  val logger = Logger(this.getClass.getName)
  val petStore: PetStoreService = inject[PetStoreService]

  val petForm = Form {
    mapping("petName" -> nonEmptyText)(
      name => NewPet(id = None, name = name, tag = None)
    )(
      pet => Some(pet.name)
    )
  }

  private def getPetList(user: UserModel): Future[Either[String, List[Pet]]] = {
    petStore.findPets(None, None, user).map {
      case FindPetsOk(list) =>
        Right(list)
      case FindPetsDefault(error, status) =>
        Left(s"STATUS: $status, ERROR: $error")
    }.recover {
      case cause =>
        Left("ERROR: " + cause.getMessage)
    }
  }

  def index: Action[AnyContent] = AsyncStack(AuthorityKey -> UserAuthority(UserRole.all)) { implicit request =>
    for {
      result <- getPetList(loggedIn)
    } yield result.fold(
      error => Ok(html.petStore(Nil, petForm.withGlobalError(error))),
      list => Ok(html.petStore(list, petForm))
    )
  }

  private def createNewPet(newPet: NewPet, user: UserModel): Future[Either[String, Pet]] = {
    petStore.addPet(newPet, user).map {
      case AddPetOk(pet) =>
        Right(pet)
      case AddPetDefault(error, status) =>
        Left(s"STATUS: $status, ERROR: $error")
    }.recover {
      case cause =>
        Left("ERROR: " + cause.getMessage)
    }
  }

  def addPet(): Action[AnyContent] = AsyncStack(AuthorityKey -> UserAuthority(Set(UserRole.admin))) { implicit request =>
    petForm.bindFromRequest().fold(
      errors => Future.successful(Redirect(routes.PetStoreController.index()).flashing(
        ("error", errors.globalError.map(_.message).getOrElse(""))
      )),
      newPet => for {
        result <- createNewPet(newPet, loggedIn)
      } yield result.fold(
        error => Redirect(routes.PetStoreController.index()).flashing(("error", error)),
        _ => Redirect(routes.PetStoreController.index())
      )
    )
  }

  private def deletePet(id: Long, user: UserModel): Future[Either[String, Unit]] = {
    petStore.deletePet(id, user).map {
      case DeletePetNoContent =>
        Right(())
      case DeletePetDefault(error, status) =>
        Left(s"STATUS: $status, ERROR: $error")
    }.recover {
      case cause =>
        Left("ERROR: " + cause.getMessage)
    }
  }

  def delete(id: Long): Action[AnyContent] = AsyncStack(AuthorityKey -> UserAuthority(Set(UserRole.admin))) { implicit request =>
    for {
      result <- deletePet(id, loggedIn)
    } yield result.fold(
      error => Redirect(routes.PetStoreController.index()).flashing(("error", error)),
      _ => Redirect(routes.PetStoreController.index())
    )
  }

}
