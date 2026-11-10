package com.ipvio.stream

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class FirstLanguage(val code: String, val nativeName: String)

@Composable
fun FirstLaunchLanguageScreen(onSelected: (String) -> Unit) {
    val languages = remember {
        listOf(
            FirstLanguage("ru", "Русский"),
            FirstLanguage("de", "Deutsch"),
            FirstLanguage("en", "English"),
            FirstLanguage("uk", "Українська")
        )
    }
    val focus = remember { List(languages.size) { FocusRequester() } }
    LaunchedEffect(Unit) { runCatching { focus.first().requestFocus() } }

    Box(
        Modifier.fillMaxSize().background(Color(0xFF080C16)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            Modifier.fillMaxWidth().widthIn(max = 720.dp).padding(horizontal = 48.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text("PASHA TV Lite", color = Color.White, fontSize = 42.sp, fontWeight = FontWeight.ExtraBold)
            Text(
                "Choose language · Sprache wählen · Выберите язык · Оберіть мову",
                color = Color(0xFFB8C2D5), fontSize = 17.sp, fontWeight = FontWeight.Medium
            )
            Column(
                Modifier.fillMaxWidth().padding(top = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                languages.forEachIndexed { index, language ->
                    var selected by remember { mutableStateOf(false) }
                    val shape = RoundedCornerShape(16.dp)
                    Box(
                        Modifier.fillMaxWidth().heightIn(min = 66.dp)
                            .focusRequester(focus[index])
                            .onFocusChanged { selected = it.isFocused }
                            .background(if (selected) Color.White else Color(0xFF172238), shape)
                            .border(2.dp, if (selected) Color(0xFF35C7D0) else Color(0x334B6687), shape)
                            .clickable { onSelected(language.code) }
                            .focusable()
                            .padding(horizontal = 28.dp, vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            language.nativeName,
                            color = if (selected) Color(0xFF101827) else Color.White,
                            fontSize = 25.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
