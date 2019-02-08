package com.github.romastyi.api.service

/**
  * Created by romastyi on 05.05.17.
  */

import com.github.romastyi.api.domain.UserModel
import com.github.romastyi.api.model._
import play.boilerplate.api.Tracer

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
    * @param tags tags to filter by
    * @param logged Current logged user
    * @param tracer Request Trace ID
    */
  override def findPets(pager: FindPetsPager, tags: Option[List[FindPetsTags.Value]], logged: UserModel)(implicit tracer: Tracer): Future[FindPetsResponse] = {

    def dropN(n: Int): Seq[Pet] => Seq[Pet] = _.drop(n)
    def takeN(n: Int): Seq[Pet] => Seq[Pet] = _.take(n)

    logger.debug(logged.toString)

    val modifiers: Seq[Pet] => Seq[Pet] = Function.chain(
      pager.drop.filter(_ >= 0).toList.map(dropN) ++
      pager.limit.filter(_ >= 0).toList.map(takeN)
    )

    Future.successful(FindPetsOk(modifiers(pets).toList, tracer))

  }

  /**
    * Creates a new pet in the store.  Duplicates are allowed
    *
    * @param pet Pet to add to the store
    * @param logged Current logged user
    * @param tracer Request Trace ID
    */
  override def addPet(pet: NewPet, logged: UserModel)(implicit tracer: Tracer): Future[AddPetResponse] = {
    val petToAdd = Pet(ids.incrementAndGet(), pet.name, pet.tag)
    pets = pets :+ petToAdd
    Future.successful(AddPetOk(petToAdd, tracer))
  }

  /**
    * deletes a single pet based on the ID supplied
    *
    * @param id ID of pet to delete
    * @param logged Current logged user
    * @param tracer Request Trace ID
    */
  override def deletePet(id: Long, logged: UserModel)(implicit tracer: Tracer): Future[DeletePetResponse] = {
    pets.find(_.id == id).get
    pets = pets.filter(_.id != id)
    Future.successful(DeletePetNoContent(tracer))
  }

  /**
    * Returns a user based on a single ID, if the user does not have access to the pet
    *
    * @param id ID of pet to fetch
    * @param logged Current logged user
    * @param tracer Request Trace ID
    */
  override def findPetById(id: Long, logged: UserModel)(implicit tracer: Tracer): Future[FindPetByIdResponse] = {
    Future.successful(
      pets.find(_.id == id).map(FindPetByIdOk(_, tracer)).getOrElse(
        FindPetByIdDefault(ErrorModel(100, s"Pet with ID $id not found!", None, java.util.UUID.randomUUID()), 400, tracer)
      )
    )
  }

  /**
    * Returns a user based on a single tag, if the user does not have access to the pet
    *
    * @param tag Tag of pet to fetch
    * @param logged Current logged user
    * @param tracer Request Trace ID
    */
  override def findPetByTag(tag: FindPetByTagTag.Value, logged: UserModel)(implicit tracer: Tracer): Future[FindPetByTagResponse] = {
    Future.successful(FindPetByTagOk(pets.find(_.tag.getOrElse(Nil).contains(tag)).get, tracer))
  }

  /**
    *
    *
    * @param id ID of pet that needs to be updated
    * @param name Updated name of the pet
    * @param status Updated status of the pet
    * @param logged Current logged user
    * @param tracer Request Trace ID
    */
  override def updatePetWithForm(id: Long, name: String, status: Option[String], logged: UserModel)(implicit tracer: Tracer): Future[UpdatePetWithFormResponse] = {
    Future.successful(UpdatePetWithFormOk(Pet(
      id = id,
      name = name,
      tag = status.map(PetTag.withName).map(List(_))
    ), tracer))
  }

  /**
    *
    *
    * @param petId ID of pet to update
    * @param additionalMetadata Additional data to pass to server
    * @param file file to upload
    * @param logged Current logged user
    * @param tracer Request Trace ID
    */
  override def uploadFile(petId: Long, additionalMetadata: Option[String], file: java.io.File, logged: UserModel)(implicit tracer: Tracer): Future[UploadFileResponse] = {
    Future.successful(UploadFileOk(ApiResponse(
      code = Some(200),
      `type` = additionalMetadata,
      message = None
    ), tracer))
  }

}
