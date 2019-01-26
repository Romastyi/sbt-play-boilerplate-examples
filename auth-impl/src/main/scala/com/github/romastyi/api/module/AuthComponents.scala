package com.github.romastyi.api.module

import com.github.romastyi.api.controller.AuthRouter
import play.boilerplate.api.server.dsl.InjectedRoutes
import scaldi.Module

class AuthComponents extends Module {
  bind [InjectedRoutes] identifiedBy 'auth to InjectedRoutes(injected[AuthRouter])
}
