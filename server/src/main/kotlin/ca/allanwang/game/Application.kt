package ca.allanwang.game

import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun main() {
  embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
    .start(wait = true)
}

/**
 * For setup, see https://start.ktor.io/
 */
fun Application.module() {
  configureMonitoring()
  configureRouting()
  configureSockets()

  val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
  routing {
    get("/") {
      call.respondText("Ktor: ${Greeting().greet()}\nStarted $now")
    }
  }
}