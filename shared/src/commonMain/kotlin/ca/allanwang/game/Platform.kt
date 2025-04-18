package ca.allanwang.game

interface Platform {
  val name: String
}

expect fun getPlatform(): Platform