package com.ipvio.stream.data

data class Channel(
    val id: String,
    val name: String,
    val url: String,
    val group: String = "Все каналы",
    val logoUrl: String? = null,
    val tvgId: String? = null
)
