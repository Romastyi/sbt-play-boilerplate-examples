package com.github.romastyi.api.module

import com.github.romastyi.api.service.{PetStoreService, PetStoreServiceImpl}
import play.boilerplate.api.server.dsl.InjectedRoutes
import scaldi.Module

class PetStoreComponents extends Module {
  bind [PetStoreService] to PetStoreServiceImpl
  bind [InjectedRoutes] identifiedBy 'petStore to InjectedRoutes(petStore.Routes)
}
