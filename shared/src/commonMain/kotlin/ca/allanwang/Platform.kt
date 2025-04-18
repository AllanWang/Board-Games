package ca.allanwang

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform