package com.github.romastyi

/**
  * Created by romastyi on 08.05.17.
  */

import play.api.GlobalSettings
import scaldi.Injector
import scaldi.play.ScaldiSupport

object Global extends GlobalSettings with ScaldiSupport {
  lazy val applicationModule: Injector = new WebModule ++ new ServicesModule
}