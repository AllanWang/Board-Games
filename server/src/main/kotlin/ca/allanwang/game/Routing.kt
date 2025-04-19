package ca.allanwang.game

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.autohead.AutoHeadResponse
import io.ktor.server.plugins.requestvalidation.RequestValidation
import io.ktor.server.plugins.requestvalidation.ValidationResult
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun Application.configureRouting() {
  install(AutoHeadResponse)
//  install(RequestValidation) {
//    validate<String> { bodyText ->
//      if (!bodyText.startsWith("Hello"))
//        ValidationResult.Invalid("Body text should start with 'Hello'")
//      else ValidationResult.Valid
//    }
//  }
}
