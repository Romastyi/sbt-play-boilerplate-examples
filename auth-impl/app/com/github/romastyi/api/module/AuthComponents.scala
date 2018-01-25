package com.github.romastyi.api.module

import play.boilerplate.api.server.dsl.InjectedRoutes
import scaldi.Module

class AuthComponents extends Module {
  bind [InjectedRoutes] identifiedBy 'auth to InjectedRoutes(auth.Routes)
}
