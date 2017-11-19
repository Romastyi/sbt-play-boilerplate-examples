package com.github.romastyi.api.module

import com.github.romastyi.api.service.{PetStoreService, PetStoreServiceImpl}
import scaldi.Module

class PetStoreImplModule extends Module {
  bind [PetStoreService] to PetStoreServiceImpl
}
