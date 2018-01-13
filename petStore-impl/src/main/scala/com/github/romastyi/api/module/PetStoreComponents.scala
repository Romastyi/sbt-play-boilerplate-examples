package com.github.romastyi.api.module

import com.github.romastyi.api.service.{PetStoreService, PetStoreServiceImpl}
import play.api.routing.Router
import scaldi.Module

class PetStoreComponents extends Module {
  bind [PetStoreService] to PetStoreServiceImpl
  bind [Router] to injected [petStore.Routes]
}
