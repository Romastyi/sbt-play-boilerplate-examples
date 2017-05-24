package eu.unicredit

/**
  * Created by romastyi on 05.05.17.
  */

import _root_.swagger.codegen.service.PetStoreService
import _root_.swagger.codegen._

import scala.collection.mutable.{Seq => MSeq}
import scala.concurrent.Future

class PetStoreServiceImpl extends PetStoreService {

  import PetStoreService._

  private var pets: MSeq[pet] = MSeq()

  /**
    * Returns all pets from the system that the user has access to
    *
    *
    */
  override def findPets(tags: Option[List[String]], limit: Option[Int], user: UserModel): Future[FindPetsResponse] = {
    println(user)
    Future.successful(FindPetsOk(pets.toList))
  }

  /**
    * Creates a new pet in the store.  Duplicates are allowed
    *
    *
    */
  override def addPet(p: newPet, user: UserModel): Future[AddPetResponse] = {
    println(user)
    val petToAdd = pet(p.id.getOrElse(0), p.name, p.tag)
    pets :+= petToAdd
    Future.successful(AddPetOk(petToAdd))
  }

  /**
    * deletes a single pet based on the ID supplied
    *
    *
    */
  override def deletePet(id: Long): Future[DeletePetResponse] = {
    pets.find(_.id == id).get
    pets = pets.filter(_.id != id)
    Future.successful(DeletePetNoContent)
  }

  /**
    * Returns a user based on a single ID, if the user does not have access to the pet
    *
    *
    */
  override def findPetById(id: Long): Future[FindPetByIdResponse] = {
    Future.successful(FindPetByIdOk(pets.find(_.id == id).get))
  }

  /**
    * Error handler
    *
    * @param operationId Operation where error was occurred
    * @param cause       An occurred error
    */
  override def onError(operationId: String, cause: Throwable): Future[String] = {
    Future.successful(cause.getMessage)
  }

}
