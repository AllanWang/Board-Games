plugins {
  alias(libs.plugins.kotlinJvm)
  alias(libs.plugins.ktor)
  alias(libs.plugins.kotlinSerialization)
  application
}

group = "ca.allanwang"
version = "1.0.0"
application {
  mainClass.set("ca.allanwang.game.ApplicationKt")
  applicationDefaultJvmArgs = listOf("-Dio.ktor.development=${extra["io.ktor.development"] ?: "false"}")
}

dependencies {
  implementation(projects.shared)
  implementation(libs.logback)
  implementation(libs.ktor.server.core)
  implementation(libs.ktor.server.netty)
  implementation(libs.ktor.server.auto.head.response)
  implementation(libs.ktor.server.request.validation)
  implementation(libs.ktor.server.http.redirect)
  implementation(libs.ktor.server.call.logging)
  implementation(libs.ktor.server.call.id)
  implementation(libs.ktor.serialization.kotlinx.protobuf)
  implementation(libs.ktor.server.content.negotiation)
  implementation(libs.ktor.server.sessions)
  implementation(libs.ktor.server.auth)
  implementation(libs.ktor.server.websockets)
  implementation(libs.ktor.server.config.yaml)

  testImplementation(libs.ktor.server.test.host)
  testImplementation(libs.kotlin.test.junit)
}