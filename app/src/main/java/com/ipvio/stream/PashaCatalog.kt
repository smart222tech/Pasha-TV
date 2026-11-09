package com.ipvio.stream

/**
 * PASHA TV's built-in live channel catalog.
 *
 * These are regular IPVIO Item objects so the original LiveTab, favorites,
 * channel zapping, Media3 player and player animations remain in use.
 */
object PashaCatalog {
    val channels = listOf(
        Item("pasha:first", Kind.LIVE, "Первый канал", null,
            "https://streaming.thestream.cyou/live/210-req_offset_28000000-req_window_0-1k_v5.m3u8",
            null, "Россия", lang = "ru"),
        Item("pasha:russia1", Kind.LIVE, "Россия 1", null,
            "http://45.159.74.13:80/Rossia1/tracks-v1a1/mono.m3u8",
            null, "Россия", lang = "ru"),
        Item("pasha:rtr-planet", Kind.LIVE, "РТР Планета", null,
            "http://ott-cdn.ucom.am/s15/index.m3u8",
            null, "Россия", lang = "ru"),
        Item("pasha:russia-rtr", Kind.LIVE, "Россия РТР", null,
            "https://sc.id-tv.kz:443/RossiyaRTR_34_35.m3u8",
            null, "Россия", lang = "ru"),
        Item("pasha:tvc", Kind.LIVE, "ТВЦ — ТВ Центр", null,
            "https://tvc-hls.cdnvideo.ru/tvc-res/smil:vd9221_2.smil/12ed865707c77a12eeecfa3177775684--tvc-res--vd9221_480p_2--tracks-v1a1--mono.m3u8?hls_proxy_host=22b5db40ced763465289cd8236e019fb",
            null, "Россия", lang = "ru"),
        Item("pasha:domashniy", Kind.LIVE, "Домашний", null,
            "http://45.159.74.13:80/Domashniy/tracks-v1a1/mono.m3u8",
            null, "Россия", lang = "ru"),
        Item("pasha:channel9-israel", Kind.LIVE, "Девятый канал HD (Израиль)", null,
            "https://contact.gostreaming.tv/Con-11/tracks-v1a1/mono.ts.m3u8",
            null, "Израиль", lang = "ru"),
        Item("pasha:auto-plus", Kind.LIVE, "Авто Плюс", null,
            "https://stream8.cinerama.uz/1036/tracks-v1a1/mono.m3u8",
            null, "Автомобильные", lang = "ru"),
        Item("pasha:friday", Kind.LIVE, "Пятница!", null,
            "https://vod.tuva.ru/friday/tracks-v1a1/mono.ts.m3u8",
            null, "Россия", lang = "ru")
    )
}


/** Persists the last channel independently from legacy IPVIO history IDs. */
object PashaLastChannel {
    private const val PREFS = "pasha_tv_lite"
    private const val KEY_LAST_CHANNEL = "last_channel_id"

    fun save(context: android.content.Context, id: String) {
        context.getSharedPreferences(PREFS, android.content.Context.MODE_PRIVATE)
            .edit().putString(KEY_LAST_CHANNEL, id).apply()
    }

    fun get(context: android.content.Context): String? =
        context.getSharedPreferences(PREFS, android.content.Context.MODE_PRIVATE)
            .getString(KEY_LAST_CHANNEL, null)
}
