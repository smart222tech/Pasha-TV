package com.ipvio.stream

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class I18nCoreTest {
    @Before fun setUp() { I18nCore.load(DictA, DictB, DictC, DictD, DictE, DictF, DictG, DictH, DictI, DictJ)
        I18nCore.loadColumn(I18nCore.CODES.indexOf("ru"), DictRu)
        I18nCore.loadColumn(I18nCore.CODES.indexOf("uk"), DictUk) }

    private val EN = 1; private val RU = 2; private val UK = 3; private val DE = 0

    @Test fun exact() {
        assertEquals("Parental controls", I18nCore.translate("Jugendschutz", EN))
        assertEquals("Родительский контроль", I18nCore.translate("Jugendschutz", RU))
        assertEquals("Батьківський контроль", I18nCore.translate("Jugendschutz", UK))
    }
    @Test fun germanIsIdentity() {
        assertEquals("Jugendschutz", I18nCore.translate("Jugendschutz", DE))
    }
    @Test fun onlyFourAppLanguagesAreAvailable() {
        assertEquals(listOf("de", "en", "ru", "uk"), I18nCore.CODES)
    }
    @Test fun surroundingSpacesAreKept() {
        assertEquals("  Reload active playlist ", I18nCore.translate("  Aktive Playlist neu laden ", EN))
    }
    @Test fun noEmojisInDictionaries() {
        // Symbole sind gezeichnet (AppIcons), Texte enthalten keine Emojis/Bildzeichen
        for (d in listOf(DictA, DictB, DictC, DictD, DictE, DictF, DictG, DictH, DictI)) for (ch in d) {
            val c = ch.code
            val pictographic = c in 0x2190..0x21FF && ch != '→' && ch != '↑' && ch != '↓' || c in 0x2300..0x23FF || c in 0x2460..0x27BF ||
                c in 0x2B00..0x2BFF || c in 0xFF01..0xFF0F || Character.isSurrogate(ch)
            org.junit.Assert.assertFalse("Zeichen U+" + Integer.toHexString(c), pictographic)
        }
    }
    @Test fun patternWithNumber() {
        assertEquals("every 5 s", I18nCore.translate("alle 5 s", EN))
    }
    @Test fun unknownFallsBackToOriginal() {
        assertEquals("Völlig unbekannter Text 123", I18nCore.translate("Völlig unbekannter Text 123", EN))
    }
    @Test fun guideLegend() {
        assertEquals("watch", I18nCore.translate("ansehen", EN))
        assertEquals("close", I18nCore.translate("schließen", EN))
        assertEquals("NOW", I18nCore.translate("JETZT", EN))
        assertEquals("Subtitle style", I18nCore.translate("Untertitel-Stil", EN))
        assertEquals("Playback speed", I18nCore.translate("Wiedergabe-Tempo", EN))
    }
    @Test fun systemLanguage() {
        assertEquals(RU, I18nCore.indexForSystem("ru"))
        assertEquals(UK, I18nCore.indexForSystem("uk"))
        assertEquals(DE, I18nCore.indexForSystem("fr"))
    }
}
