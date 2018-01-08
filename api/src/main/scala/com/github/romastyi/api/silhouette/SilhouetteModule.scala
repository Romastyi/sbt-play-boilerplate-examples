package com.github.romastyi.api.silhouette

import com.mohiva.play.silhouette.api.actions.{SecuredAction, UnsecuredAction, UserAwareAction}
import com.mohiva.play.silhouette.api.util.Clock
import com.mohiva.play.silhouette.api.{EventBus, Silhouette, SilhouetteProvider}
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import play.api.Configuration
import play.api.mvc.SessionCookieBaker
import scaldi.Module

import scala.concurrent.ExecutionContext

class SilhouetteModule extends Module {

  bind [Clock] to Clock()
  bind [EventBus] to EventBus()
  bind [CredentialsProvider] to UserEnvironment.credentialsProvider(inject[ExecutionContext])

  bind [Silhouette[SessionEnv]] identifiedBy 'session to new SilhouetteProvider[SessionEnv](
    UserEnvironment.session(inject[Configuration], inject[SessionCookieBaker], inject[Clock], inject[EventBus])(inject[ExecutionContext]),
    inject[SecuredAction],
    inject[UnsecuredAction],
    inject[UserAwareAction]
  )
/*
  bind [Silhouette[JWTEnv]] identifiedBy 'jwt to new SilhouetteProvider[JWTEnv](
    UserEnvironment.jwt(inject[Configuration], inject[Clock], inject[EventBus])(inject[ExecutionContext]),
    inject[SecuredAction],
    inject[UnsecuredAction],
    inject[UserAwareAction]
  )
*/

}
