package com.ipvio.stream

/**
 * Pure rules for the navigation menu (without Android) so they can be tested.
 * Guarantees: every area occurs exactly once; Settings (5) is always available.
 */
object MenuLogic {
    const val COUNT = 11
    const val HOME = 0
    const val SEARCH = 4
    const val SETTINGS = 5
    /** Seit Runde 13 (Entwurf): TV-Guide, Favoriten (Sender), Meine Liste (Filme/Serien). */
    const val EPG = 6
    const val FAVS = 7
    const val MYLIST = 8
    /** Round 15 (addendum 17): History – recently watched, movies and series separate. */
    const val VERLAUF = 9
    /** Round 20: Sports – running and upcoming sports shows, sports channels. */
    const val SPORT = 10
    /** Fixed sidebar order: Live TV, Favorites, Movies, Series, Settings. */
    val DEFAULT_ORDER = listOf(3, FAVS, 1, 2, 5, HOME, SEARCH, EPG, MYLIST, VERLAUF, SPORT)
    /** Only the five requested entries are visible in the sidebar. */
    const val DEFAULT_HIDDEN = "0,4,6,8,9,10"

    fun parseOrder(raw: String?): List<Int> {
        val order = (raw ?: DEFAULT_ORDER.joinToString(",")).split(",").mapNotNull { it.trim().toIntOrNull() }
            .filter { it in 0 until COUNT }.distinct().toMutableList()
        for (id in 0 until COUNT) if (id !in order) {
            // New area (saved order from before): insert before "Settings", not after
            val s = order.indexOf(SETTINGS)
            if (id >= EPG && s >= 0) order.add(s, id) else order.add(id)
        }
        return order
    }

    fun parseHidden(raw: String?): List<Int> =
        (raw ?: "").split(",").mapNotNull { it.trim().toIntOrNull() }
            .filter { it in 0 until COUNT && it != SETTINGS }.distinct()

    fun move(order: List<Int>, id: Int, delta: Int): List<Int> {
        val i = order.indexOf(id)
        val j = i + delta
        if (i < 0 || j < 0 || j >= order.size) return order
        val m = order.toMutableList()
        m.removeAt(i); m.add(j, id)
        return m
    }

    fun toggle(hidden: List<Int>, id: Int): List<Int> {
        if (id == HOME || id == SETTINGS || id !in 0 until COUNT) return hidden
        return if (id in hidden) hidden - id else hidden + id
    }

    fun visible(order: List<Int>, hidden: List<Int>): List<Int> = order.filter { it !in hidden }
}
