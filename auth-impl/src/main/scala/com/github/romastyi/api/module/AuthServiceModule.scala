package com.github.romastyi.api.module

import com.github.romastyi.api.service.{AuthService, AuthServiceImpl}
import scaldi.Module

class AuthServiceModule extends Module {
  bind [AuthService] to new AuthServiceImpl
}
