package eu.unicredit

/**
  * Created by romastyi on 07.05.17.
  */

/* Guice Module */
/*
import com.google.inject.AbstractModule
import play.api.{Configuration, Environment}
import _root_.swagger.codegen.service.PetStoreService

class ServicesModule(environment: Environment, configuration: Configuration)
  extends AbstractModule {

  override def configure() = {
    bind(classOf[PetStoreService]).to(classOf[PetStoreServiceImpl])
  }
}
*/

/* Scaldi Module */
import test.api.service.PetStoreService
import scaldi.Module

class ServicesModule extends Module {
  bind [PetStoreService] to new PetStoreServiceImpl
}
