package com.ipvio.stream.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object RemoteChannelCatalog {
    const val CATALOG_URL =
        "https://raw.githubusercontent.com/smart222tech/PASHA-TV-LITE-UPDATE/main/channels.json"

    data class Result(val channels: List<Channel>, val updatedAt: String)

    suspend fun fetch(): Result = withContext(Dispatchers.IO) {
        val connection = (URL(CATALOG_URL).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 8_000
            readTimeout = 8_000
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Cache-Control", "no-cache")
        }
        try {
            val status = connection.responseCode
            require(status in 200..299) { "HTTP $status" }
            val body = connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            require(body.length <= 1_000_000) { "Каталог слишком большой" }
            val root = JSONObject(body)
            require(root.optInt("schemaVersion", -1) == 1) { "Неподдерживаемая версия каталога" }
            val array = root.optJSONArray("channels")
                ?: throw IllegalArgumentException("В каталоге нет массива channels")
            val parsed = buildList {
                for (index in 0 until array.length().coerceAtMost(300)) {
                    val item = array.optJSONObject(index) ?: continue
                    val id = item.optString("id").trim()
                    val name = item.optString("name").trim()
                    val streamUrl = item.optString("url").trim()
                    val group = item.optString("group", "Без категории").trim()
                    if (id.isBlank() || name.isBlank()) continue
                    if (!streamUrl.startsWith("https://", true) &&
                        !streamUrl.startsWith("http://", true)) continue
                    add(Channel(
                        id = id,
                        name = name,
                        url = streamUrl,
                        group = group.ifBlank { "Без категории" },
                        logoUrl = item.optString("logoUrl").takeIf { it.isNotBlank() },
                        tvgId = item.optString("tvgId").takeIf { it.isNotBlank() }
                    ))
                }
            }.distinctBy { it.id }
            require(parsed.isNotEmpty()) { "Удалённый каталог пока пуст" }
            Result(parsed, root.optString("updatedAt", "дата не указана"))
        } finally {
            connection.disconnect()
        }
    }
}
