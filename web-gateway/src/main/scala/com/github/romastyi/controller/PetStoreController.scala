package com.github.romastyi.controller

import java.util.UUID

import com.github.romastyi.api.domain.{UserModel, UserRole}
import com.github.romastyi.api.model.{NewPet, Pet}
import com.github.romastyi.api.service.PetStoreService
import com.github.romastyi.api.silhouette.{SessionEnv, WithRoles}
import com.mohiva.play.silhouette.api.Silhouette
import play.api.data._
import play.api.data.Forms._
import play.api.mvc._
import scaldi.{Injectable, Injector}

import scala.concurrent.{ExecutionContext, Future}

class PetStoreController(implicit val inj: Injector) extends InjectedController with Injectable {

  import PetStoreService._

  implicit lazy val ec: ExecutionContext = controllerComponents.executionContext

  private lazy val silhouette = inject[Silhouette[SessionEnv]]
  private lazy val petStore = inject[PetStoreService[Future]]

  val petForm = Form {
    mapping("petName" -> nonEmptyText)(
      name => NewPet(id = None, name = name, tag = None)
    )(
      pet => Some(pet.name)
    )
  }

  private def traceId: String = UUID.randomUUID().toString

  private def getPetList(user: UserModel): Future[Either[String, List[Pet]]] = {
    petStore.findPets(FindPetsPager(drop = Some(0), limit = None), None, traceId, user).map {
      case FindPetsOk(list, _) =>
        Right(list)
      case FindPetsDefault(error, code, _) =>
        Left(s"STATUS: $code, ERROR: $error")
      case UnexpectedResult(error, code, _) =>
        Left(s"STATUS: $code, ERROR: $error")
    }.recover {
      case cause =>
        Left("ERROR: " + cause.getMessage)
    }
  }

  def index: Action[AnyContent] = silhouette.SecuredAction(WithRoles[SessionEnv#A](UserRole.all)).async { implicit request =>
    for {
      result <- getPetList(request.identity)
    } yield result.fold(
      error => Ok(html.petStore(Nil, petForm.withGlobalError(error))),
      list => Ok(html.petStore(list, petForm))
    )
  }

  private def createNewPet(newPet: NewPet, user: UserModel): Future[Either[String, Pet]] = {
    petStore.addPet(newPet, traceId, user).map {
      case AddPetOk(pet, _) =>
        Right(pet)
      case AddPetDefault(error, code, _) =>
        Left(s"STATUS: $code, ERROR: $error")
      case UnexpectedResult(error, code, _) =>
        Left(s"STATUS: $code, ERROR: $error")
    }.recover {
      case cause =>
        Left("ERROR: " + cause.getMessage)
    }
  }

  def addPet(): Action[AnyContent] = silhouette.SecuredAction(WithRoles[SessionEnv#A](Set(UserRole.admin))).async { implicit request =>
    petForm.bindFromRequest().fold(
      errors => Future.successful(Redirect(routes.PetStoreController.index()).flashing(
        ("error", errors.globalError.map(_.message).getOrElse(""))
      )),
      newPet => for {
        result <- createNewPet(newPet, request.identity)
      } yield result.fold(
        error => Redirect(routes.PetStoreController.index()).flashing(("error", error)),
        _ => Redirect(routes.PetStoreController.index())
      )
    )
  }

  private def deletePet(id: Long, user: UserModel): Future[Either[String, Unit]] = {
    petStore.deletePet(id, traceId, user).map {
      case DeletePetNoContent(_) =>
        Right(())
      case DeletePetDefault(error, code, _) =>
        Left(s"STATUS: $code, ERROR: $error")
      case UnexpectedResult(error, code, _) =>
        Left(s"STATUS: $code, ERROR: $error")
    }.recover {
      case cause =>
        Left("ERROR: " + cause.getMessage)
    }
  }

  def delete(id: Long): Action[AnyContent] = silhouette.SecuredAction(WithRoles[SessionEnv#A](Set(UserRole.admin))).async { implicit request =>
    for {
      result <- deletePet(id, request.identity)
    } yield result.fold(
      error => Redirect(routes.PetStoreController.index()).flashing(("error", error)),
      _ => Redirect(routes.PetStoreController.index())
    )
  }

}
