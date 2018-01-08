package com.github.romastyi.api.silhouette

import com.github.romastyi.api.domain.UserModel
import com.mohiva.play.silhouette.api.crypto.{AuthenticatorEncoder, CrypterAuthenticatorEncoder}
import com.mohiva.play.silhouette.api.util.{Clock, PasswordHasherRegistry, PasswordInfo}
import com.mohiva.play.silhouette.api.{Env, Environment, EventBus}
import com.mohiva.play.silhouette.crypto.{JcaCrypter, JcaCrypterSettings}
import com.mohiva.play.silhouette.impl.authenticators._
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import com.mohiva.play.silhouette.impl.util.{DefaultFingerprintGenerator, SecureRandomIDGenerator}
import com.mohiva.play.silhouette.password.BCryptPasswordHasher
import com.mohiva.play.silhouette.persistence.daos.InMemoryAuthInfoDAO
import com.mohiva.play.silhouette.persistence.repositories.DelegableAuthInfoRepository
import play.api.Configuration
import play.api.mvc.SessionCookieBaker

import scala.concurrent.ExecutionContext

sealed trait UserEnv extends Env {
  override type I = UserModel
}

trait SessionEnv extends UserEnv {
  override type A = SessionAuthenticator
}

trait JWTEnv extends UserEnv {
  override type A = JWTAuthenticator
}

object UserEnvironment {

  def encoder(configuration: Configuration): AuthenticatorEncoder = {
    val config = JcaCrypterSettings(configuration.get[String]("silhouette.authenticator.crypter.key"))
    new CrypterAuthenticatorEncoder(new JcaCrypter(config))
  }

  def session(configuration: Configuration,
              sessionCookieBaker: SessionCookieBaker,
              clock: Clock,
              eventBus: EventBus)
             (implicit ec: ExecutionContext): Environment[SessionEnv] = {
//    val config = configuration.get[SessionAuthenticatorSettings]("silhouette.authenticator.cookie")
    val config = SessionAuthenticatorSettings()
    val fingerprint = new DefaultFingerprintGenerator()
    val authService = new SessionAuthenticatorService(config, fingerprint, encoder(configuration), sessionCookieBaker, clock)
    Environment[SessionEnv](
      UserService,
      authService,
      Nil,
      eventBus
    )
  }

  def jwt(configuration: Configuration,
          clock: Clock,
          eventBus: EventBus)
         (implicit ec: ExecutionContext): Environment[JWTEnv] = {
//    val config = configuration.get[JWTAuthenticatorSettings]("silhouette.authenticator.jwt")
    val config = JWTAuthenticatorSettings(sharedSecret = configuration.get[String]("silhouette.authenticator.sharedSecret"))
    val idGenerator = new SecureRandomIDGenerator()
    val authService = new JWTAuthenticatorService(config, None, encoder(configuration), idGenerator, clock)
    Environment[JWTEnv](
      UserService,
      authService,
      Nil,
      eventBus
    )
  }

  def credentialsProvider(implicit ec: ExecutionContext): CredentialsProvider = {
    new CredentialsProvider(
      new DelegableAuthInfoRepository(new InMemoryAuthInfoDAO[PasswordInfo]),
      PasswordHasherRegistry(new BCryptPasswordHasher)
    )
  }

}
