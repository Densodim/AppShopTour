package com.example.appshoptour

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform