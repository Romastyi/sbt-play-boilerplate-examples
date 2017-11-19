package com.github.romastyi.api.service

/**
  * Created by romastyi on 05.05.17.
  */

import com.github.romastyi.api.domain.UserModel
import com.github.romastyi.api.model._

import scala.collection.mutable.{Seq => MSeq}
import scala.concurrent.Future

object PetStoreServiceImpl extends PetStoreService {

  import PetStoreService._

  private var pets: MSeq[Pet] = MSeq()

  /**
    * Returns all pets from the system that the user has access to
    *
    *
    */
  override def findPets(tags: Option[List[FindPetsTags.Value]], limit: Option[Int], user: UserModel): Future[FindPetsResponse] = {
    logger.debug(user.toString)
    Future.successful(FindPetsOk(pets.toList))
  }

  /**
    * Creates a new pet in the store.  Duplicates are allowed
    *
    *
    */
  override def addPet(p: NewPet, user: UserModel): Future[AddPetResponse] = {
    logger.debug(user.toString)
    val petToAdd = Pet(p.id.getOrElse(0), p.name, p.tag)
    pets = pets :+ petToAdd
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
    Future.successful(
      pets.find(_.id == id).map(FindPetByIdOk).getOrElse(
        FindPetByIdDefault(ErrorModel(100, s"Pet with ID $id not found!", None, java.util.UUID.randomUUID()), 400)
      )
    )
  }

  /**
    * Returns a user based on a single tag, if the user does not have access to the pet
    *
    *
    */
  override def findPetByTag(tag: PetStoreService.FindPetByTagTag.Value): Future[FindPetByTagResponse] = {
    Future.successful(FindPetByTagOk(pets.find(_.tag.getOrElse(Nil).contains(tag)).get))
  }

}
