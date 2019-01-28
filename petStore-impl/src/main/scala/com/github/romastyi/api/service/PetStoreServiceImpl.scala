package com.github.romastyi.api.service

/**
  * Created by romastyi on 05.05.17.
  */

import java.io.File

import com.github.romastyi.api.domain.UserModel
import com.github.romastyi.api.model._

import scala.collection.mutable.{Seq => MSeq}
import scala.concurrent.Future

object PetStoreServiceImpl extends PetStoreService[Future] {

  import PetStoreService._

  private val ids = new java.util.concurrent.atomic.AtomicLong(0l)
  private var pets: MSeq[Pet] = MSeq.empty


  /**
    * Returns all pets from the system that the user has access to
    *
    * @param pager Class, that composed query parameters: pager.drop, pager.limit
    * @param tags  tags to filter by
    */
  override def findPets(pager: FindPetsPager, tags: Option[List[FindPetsTags.Value]], user: UserModel): Future[FindPetsResponse] = {

    def dropN(n: Int): Seq[Pet] => Seq[Pet] = _.drop(n)
    def takeN(n: Int): Seq[Pet] => Seq[Pet] = _.take(n)

    logger.debug(user.toString)

    val modifiers: Seq[Pet] => Seq[Pet] = Function.chain(
      pager.drop.filter(_ >= 0).toList.map(dropN) ++
      pager.limit.filter(_ >= 0).toList.map(takeN)
    )

    Future.successful(FindPetsOk(modifiers(pets).toList))

  }

  /**
    * Creates a new pet in the store.  Duplicates are allowed
    *
    *
    */
  override def addPet(p: NewPet, user: UserModel): Future[AddPetResponse] = {
    val petToAdd = Pet(ids.incrementAndGet(), p.name, p.tag)
    pets = pets :+ petToAdd
    Future.successful(AddPetOk(petToAdd))
  }

  /**
    * deletes a single pet based on the ID supplied
    *
    *
    */
  override def deletePet(id: Long, user: UserModel): Future[DeletePetResponse] = {
    pets.find(_.id == id).get
    pets = pets.filter(_.id != id)
    Future.successful(DeletePetNoContent)
  }

  /**
    * Returns a user based on a single ID, if the user does not have access to the pet
    *
    *
    */
  override def findPetById(id: Long, user: UserModel): Future[FindPetByIdResponse] = {
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
  override def findPetByTag(tag: FindPetByTagTag.Value, user: UserModel): Future[FindPetByTagResponse] = {
    Future.successful(FindPetByTagOk(pets.find(_.tag.getOrElse(Nil).contains(tag)).get))
  }

  /**
    *
    *
    * @param id     ID of pet that needs to be updated
    * @param name   Updated name of the pet
    * @param status Updated status of the pet
    * @param logged Current logged user
    */
  override def updatePetWithForm(id: Long, name: String, status: Option[String], logged: UserModel): Future[UpdatePetWithFormResponse] = {
    Future.successful(UpdatePetWithFormOk(Pet(
      id = id,
      name = name,
      tag = status.map(PetTag.withName).map(List(_))
    )))
  }

  /**
    *
    *
    * @param petId              ID of pet to update
    * @param additionalMetadata Additional data to pass to server
    * @param file               file to upload
    * @param logged             Current logged user
    */
  override def uploadFile(petId: Long, additionalMetadata: Option[String], file: File, logged: UserModel): Future[UploadFileResponse] = {
    Future.successful(UploadFileOk(ApiResponse(
      code = Some(200),
      `type` = additionalMetadata,
      message = None
    )))
  }

}
