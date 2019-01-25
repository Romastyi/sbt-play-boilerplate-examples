package com.github.romastyi.api.module

import com.github.romastyi.api.service.{PetStoreService, PetStoreServiceImpl}
import play.boilerplate.api.server.dsl.InjectedRoutes
import scaldi.Module

import scala.concurrent.Future

class PetStoreComponents extends Module {
  bind [PetStoreService[Future]] to PetStoreServiceImpl
  bind [InjectedRoutes] identifiedBy 'petStore to InjectedRoutes(injected[petStore.Routes])
}
