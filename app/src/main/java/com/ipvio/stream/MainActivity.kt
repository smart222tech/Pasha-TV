package com.ipvio.stream

import android.os.Bundle
import android.app.Activity
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import kotlinx.coroutines.delay
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.ipvio.stream.data.Channel
import com.ipvio.stream.data.RemoteChannelCatalog
import kotlinx.coroutines.launch

private val builtInChannels = listOf(
    Channel("first", "Первый канал", "https://streaming.thestream.cyou/live/210-req_offset_28000000-req_window_0-1k_v5.m3u8", "Россия"),
    Channel("russia1", "Россия 1", "http://45.159.74.13:80/Rossia1/tracks-v1a1/mono.m3u8", "Россия"),
    Channel("rtr-planet", "РТР Планета", "http://ott-cdn.ucom.am/s15/index.m3u8", "Россия"),
    Channel("russia-rtr", "Россия РТР", "https://sc.id-tv.kz:443/RossiyaRTR_34_35.m3u8", "Россия"),
    Channel("tvc", "ТВЦ — ТВ Центр", "https://tvc-hls.cdnvideo.ru/tvc-res/smil:vd9221_2.smil/12ed865707c77a12eeecfa3177775684--tvc-res--vd9221_480p_2--tracks-v1a1--mono.m3u8?hls_proxy_host=22b5db40ced763465289cd8236e019fb", "Россия"),
    Channel("domashniy", "Домашний", "http://45.159.74.13:80/Domashniy/tracks-v1a1/mono.m3u8", "Россия"),
    Channel("channel9-israel", "Девятый канал HD (Израиль)", "https://contact.gostreaming.tv/Con-11/tracks-v1a1/mono.ts.m3u8", "Израиль"),
    Channel("auto-plus", "Авто Плюс", "https://stream8.cinerama.uz/1036/tracks-v1a1/mono.m3u8", "Автомобильные"),
    Channel("friday", "Пятница!", "https://vod.tuva.ru/friday/tracks-v1a1/mono.ts.m3u8", "Россия")
)

private val languageNames = linkedMapOf(
    "ru" to "Русский",
    "de" to "Deutsch",
    "en" to "English",
    "uk" to "Українська",
    "ro" to "Română",
    "tr" to "Türkçe",
    "pl" to "Polski",
    "system" to "Язык системы"
)

private val translations = mapOf(
    "ru" to listOf("Каналы", "Настройки", "Язык приложения", "Обновить список", "Обновляем список…", "Список обновлён", "Каналов загружено", "Не удалось обновить список", "Используется встроенный список", "Избранное", "Все каналы", "Назад к каналам", "Предыдущий", "Следующий", "Последний канал будет открываться автоматически."),
    "de" to listOf("Sender", "Einstellungen", "App-Sprache", "Liste aktualisieren", "Senderliste wird aktualisiert…", "Liste aktualisiert", "Geladene Sender", "Senderliste konnte nicht aktualisiert werden", "Integrierte Liste wird verwendet", "Favoriten", "Alle Sender", "Zurück zur Senderliste", "Vorheriger", "Nächster", "Der zuletzt gesehene Sender wird automatisch geöffnet."),
    "en" to listOf("Channels", "Settings", "App language", "Refresh list", "Updating channel list…", "List updated", "Channels loaded", "Could not update channel list", "Using built-in list", "Favorites", "All channels", "Back to channels", "Previous", "Next", "The last watched channel opens automatically."),
    "uk" to listOf("Канали", "Налаштування", "Мова застосунку", "Оновити список", "Оновлюємо список…", "Список оновлено", "Каналів завантажено", "Не вдалося оновити список", "Використовується вбудований список", "Обране", "Усі канали", "Назад до каналів", "Попередній", "Наступний", "Останній переглянутий канал відкриватиметься автоматично."),
    "ro" to listOf("Canale", "Setări", "Limba aplicației", "Actualizează lista", "Se actualizează lista…", "Lista a fost actualizată", "Canale încărcate", "Lista nu a putut fi actualizată", "Se folosește lista integrată", "Favorite", "Toate canalele", "Înapoi la canale", "Anterior", "Următorul", "Ultimul canal vizionat se va deschide automat."),
    "tr" to listOf("Kanallar", "Ayarlar", "Uygulama dili", "Listeyi yenile", "Kanal listesi güncelleniyor…", "Liste güncellendi", "Yüklenen kanallar", "Kanal listesi güncellenemedi", "Yerleşik liste kullanılıyor", "Favoriler", "Tüm kanallar", "Kanallara dön", "Önceki", "Sonraki", "Son izlenen kanal otomatik açılır."),
    "pl" to listOf("Kanały", "Ustawienia", "Język aplikacji", "Odśwież listę", "Aktualizowanie listy…", "Lista zaktualizowana", "Wczytane kanały", "Nie udało się zaktualizować listy", "Używana jest lista wbudowana", "Ulubione", "Wszystkie kanały", "Wróć do kanałów", "Poprzedni", "Następny", "Ostatnio oglądany kanał otworzy się automatycznie.")
)

private fun currentLanguage(choice: String): String =
    if (choice == "system") android.content.res.Resources.getSystem().configuration.locales[0].language.let {
        if (it in translations) it else "ru"
    } else choice

private fun tr(language: String, index: Int): String =
    translations[currentLanguage(language)]?.get(index) ?: translations.getValue("ru")[index]

private val extraLabels = mapOf(
    "ru" to listOf("Выберите язык", "Все каналы", "Избранное", "В избранном пока нет каналов", "Панель управления", "Автоскрытие панели", "1 секунда", "3 секунды", "5 секунд", "10 секунд", "Всегда показывать", "Цвет интерфейса", "Бирюзовый", "Синий", "Янтарный", "Видеоплеер", "ExoPlayer / Media3", "К списку", "Предыдущий", "Пауза", "Продолжить", "Следующий", "В избранное", "Убрать из избранного", "Настройки", "Выберите язык, затем откроется последний канал.", "Каналы", "Внешний вид"),
    "de" to listOf("Sprache wählen", "Alle Sender", "Favoriten", "Noch keine Favoriten", "Steuerung", "Automatisches Ausblenden", "1 Sekunde", "3 Sekunden", "5 Sekunden", "10 Sekunden", "Immer anzeigen", "Oberflächenfarbe", "Türkis", "Blau", "Bernstein", "Videoplayer", "ExoPlayer / Media3", "Zur Senderliste", "Vorheriger", "Pause", "Weiter", "Nächster", "Zu Favoriten", "Aus Favoriten entfernen", "Einstellungen", "Sprache wählen, danach startet der letzte Sender.", "Sender", "Darstellung"),
    "en" to listOf("Choose language", "All channels", "Favorites", "No favorite channels yet", "Controls", "Auto-hide controls", "1 second", "3 seconds", "5 seconds", "10 seconds", "Always visible", "Interface color", "Teal", "Blue", "Amber", "Video player", "ExoPlayer / Media3", "Channel list", "Previous", "Pause", "Resume", "Next", "Add to favorites", "Remove from favorites", "Settings", "Choose a language, then the last channel will start.", "Channels", "Appearance"),
    "uk" to listOf("Оберіть мову", "Усі канали", "Обране", "В обраному ще немає каналів", "Керування", "Автоприховування панелі", "1 секунда", "3 секунди", "5 секунд", "10 секунд", "Завжди показувати", "Колір інтерфейсу", "Бірюзовий", "Синій", "Бурштиновий", "Відеоплеєр", "ExoPlayer / Media3", "До списку", "Попередній", "Пауза", "Продовжити", "Наступний", "До обраного", "Прибрати з обраного", "Налаштування", "Оберіть мову, після цього запуститься останній канал.", "Канали", "Вигляд"),
    "ro" to listOf("Alege limba", "Toate canalele", "Favorite", "Nu există încă favorite", "Comenzi", "Ascundere automată", "1 secundă", "3 secunde", "5 secunde", "10 secunde", "Mereu vizibile", "Culoarea interfeței", "Turcoaz", "Albastru", "Chihlimbar", "Player video", "ExoPlayer / Media3", "La lista de canale", "Anterior", "Pauză", "Continuă", "Următorul", "Adaugă la favorite", "Elimină din favorite", "Setări", "Alege limba, apoi va porni ultimul canal.", "Canale", "Aspect"),
    "tr" to listOf("Dil seçin", "Tüm kanallar", "Favoriler", "Henüz favori kanal yok", "Kontroller", "Otomatik gizleme", "1 saniye", "3 saniye", "5 saniye", "10 saniye", "Her zaman göster", "Arayüz rengi", "Turkuaz", "Mavi", "Kehribar", "Video oynatıcı", "ExoPlayer / Media3", "Kanal listesi", "Önceki", "Duraklat", "Devam et", "Sonraki", "Favorilere ekle", "Favorilerden kaldır", "Ayarlar", "Bir dil seçin, ardından son kanal açılır.", "Kanallar", "Görünüm")
)
private fun extra(language: String, index: Int): String =
    extraLabels[currentLanguage(language)]?.get(index) ?: extraLabels.getValue("ru")[index]

@Composable
private fun PashaLiteApp() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("pasha_lite", 0) }
    val scope = rememberCoroutineScope()
    var favorites by remember {
        mutableStateOf(prefs.getStringSet("favorites", emptySet())?.toSet() ?: emptySet())
    }
    var language by remember { mutableStateOf(prefs.getString("language", "ru") ?: "ru") }
    var languageChosen by remember { mutableStateOf(prefs.getBoolean("language_selected", false)) }
    var showSettings by remember { mutableStateOf(false) }
    var channelList by remember { mutableStateOf(builtInChannels) }
    var selectedChannelId by remember {
        mutableStateOf<String?>(prefs.getString("last_channel_id", null) ?: builtInChannels.first().id)
    }
    var isRefreshing by remember { mutableStateOf(false) }
    var catalogStatus by remember { mutableStateOf("Встроенный список каналов") }
    var catalogUpdatedAt by remember { mutableStateOf<String?>(null) }
    var refreshError by remember { mutableStateOf<String?>(null) }
    var favoritesTab by remember { mutableStateOf(false) }
    var controlsTimeout by remember { mutableIntStateOf(prefs.getInt("controls_timeout", 5)) }
    var themeChoice by remember { mutableIntStateOf(prefs.getInt("theme_choice", 0)) }
    val settingsFocusRequester = remember { FocusRequester() }

    val colors = when (themeChoice) {
        1 -> darkColorScheme(primary = Color(0xFF82B1FF), secondary = Color(0xFFB3C7FF), background = Color(0xFF10151D), surface = Color(0xFF1B2634), surfaceVariant = Color(0xFF2A394A))
        2 -> darkColorScheme(primary = Color(0xFFFFC46B), secondary = Color(0xFFFFD9A3), background = Color(0xFF19130F), surface = Color(0xFF2A211A), surfaceVariant = Color(0xFF3A2E23))
        else -> darkColorScheme(primary = Color(0xFF70D6C5), secondary = Color(0xFF9AB7FF), background = Color(0xFF101419), surface = Color(0xFF192128), surfaceVariant = Color(0xFF26323B))
    }

    fun persistFavorites(updated: Set<String>) {
        favorites = updated
        prefs.edit().putStringSet("favorites", updated.toSet()).apply()
    }
    fun selectChannel(id: String) {
        selectedChannelId = id
        prefs.edit().putString("last_channel_id", id).apply()
    }
    fun changeChannel(step: Int) {
        if (channelList.isEmpty()) return
        val index = channelList.indexOfFirst { it.id == selectedChannelId }.let { if (it < 0) 0 else it }
        val next = (index + step + channelList.size) % channelList.size
        selectChannel(channelList[next].id)
    }
    fun refreshChannels() {
        if (isRefreshing) return
        scope.launch {
            isRefreshing = true
            refreshError = null
            catalogStatus = tr(language, 4)
            try {
                val result = RemoteChannelCatalog.fetch()
                channelList = (result.channels + builtInChannels).distinctBy { it.id }
                if (selectedChannelId != null && channelList.none { it.id == selectedChannelId }) {
                    selectChannel(channelList.first().id)
                }
                catalogUpdatedAt = result.updatedAt
                catalogStatus = "${tr(language, 5)} · ${tr(language, 6)}: ${result.channels.size}"
            } catch (error: Exception) {
                if (channelList.isEmpty()) channelList = builtInChannels
                catalogStatus = tr(language, 8)
                refreshError = error.message ?: tr(language, 7)
            } finally {
                isRefreshing = false
            }
        }
    }

    BackHandler(enabled = selectedChannelId != null && languageChosen && !showSettings) {
        selectedChannelId = null
        favoritesTab = false
    }
    DisposableEffect(selectedChannelId) {
        val activity = context as? Activity
        val decor = activity?.window?.decorView
        val originalFlags = decor?.systemUiVisibility ?: 0
        if (selectedChannelId != null && languageChosen) {
            decor?.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                )
        } else {
            decor?.systemUiVisibility = originalFlags and View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY.inv()
        }
        onDispose { decor?.systemUiVisibility = originalFlags }
    }

    LaunchedEffect(Unit) { refreshChannels() }
    LaunchedEffect(selectedChannelId) {
        if (selectedChannelId == null && languageChosen) {
            runCatching { settingsFocusRequester.requestFocus() }
        }
    }

    MaterialTheme(colorScheme = colors) {
        val selectedChannel = channelList.firstOrNull { it.id == selectedChannelId }
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            if (!languageChosen) {
                LanguagePicker(language = language, onChoose = { code ->
                    language = code
                    languageChosen = true
                    prefs.edit().putString("language", code).putBoolean("language_selected", true).apply()
                    selectChannel(selectedChannelId ?: channelList.first().id)
                })
            } else if (selectedChannel != null) {
                LitePlayer(
                    channel = selectedChannel,
                    language = language,
                    controlsTimeout = controlsTimeout,
                    isFavorite = selectedChannel.id in favorites,
                    onBack = { selectedChannelId = null },
                    onPrevious = { changeChannel(-1) },
                    onNext = { changeChannel(1) },
                    onToggleFavorite = {
                        persistFavorites(if (selectedChannel.id in favorites) favorites - selectedChannel.id else favorites + selectedChannel.id)
                    },
                    onOpenSettings = { showSettings = true },
                    onOpenList = { selectedChannelId = null }
                )
            } else {
                ChannelHome(
                    channelList = channelList,
                    language = language,
                    favorites = favorites,
                    favoritesTab = favoritesTab,
                    isRefreshing = isRefreshing,
                    catalogStatus = catalogStatus,
                    catalogUpdatedAt = catalogUpdatedAt,
                    refreshError = refreshError,
                    settingsFocusRequester = settingsFocusRequester,
                    onTabChange = { favoritesTab = it },
                    onSelect = { selectChannel(it) },
                    onToggleFavorite = { id ->
                        persistFavorites(if (id in favorites) favorites - id else favorites + id)
                    },
                    onRefresh = { refreshChannels() },
                    onSettings = { showSettings = true }
                )
            }
        }
        if (showSettings) {
            SettingsDialog(
                language = language,
                controlsTimeout = controlsTimeout,
                themeChoice = themeChoice,
                onLanguage = {
                    language = it
                    prefs.edit().putString("language", it).apply()
                },
                onTimeout = {
                    controlsTimeout = it
                    prefs.edit().putInt("controls_timeout", it).apply()
                },
                onTheme = {
                    themeChoice = it
                    prefs.edit().putInt("theme_choice", it).apply()
                },
                onDismiss = { showSettings = false }
            )
        }
    }
}

@Composable
private fun LanguagePicker(language: String, onChoose: (String) -> Unit) {
    val choices = listOf("ru", "de", "en", "uk", "ro", "tr")
    Column(
        Modifier.fillMaxSize().padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("PASHA TV", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Text(extra(language, 0), style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text(extra(language, 25), color = MaterialTheme.colorScheme.secondary)
        Spacer(Modifier.height(22.dp))
        choices.forEach { code ->
            val name = languageNames[code] ?: code
            FocusActionButton(
                label = name,
                onClick = { onChoose(code) },
                modifier = Modifier.widthIn(min = 240.dp).fillMaxWidth(0.55f).padding(vertical = 3.dp)
            )
        }
    }
}

@Composable
private fun ChannelHome(
    channelList: List<Channel>,
    language: String,
    favorites: Set<String>,
    favoritesTab: Boolean,
    isRefreshing: Boolean,
    catalogStatus: String,
    catalogUpdatedAt: String?,
    refreshError: String?,
    settingsFocusRequester: FocusRequester,
    onTabChange: (Boolean) -> Unit,
    onSelect: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onRefresh: () -> Unit,
    onSettings: () -> Unit
) {
    val compact = true
    var selectedGroup by remember(favoritesTab) { mutableStateOf("all") }
    val visibleChannels = if (favoritesTab) channelList.filter { it.id in favorites } else channelList
    val groups = visibleChannels.map { it.group }.distinct().sorted()
    val filteredChannels = if (selectedGroup == "all" || selectedGroup !in groups) visibleChannels else visibleChannels.filter { it.group == selectedGroup }
    Column(Modifier.fillMaxSize().padding(horizontal = 22.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("PASHA TV", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(extra(language, 26), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
            }
            FocusActionButton(label = "⚙  ${extra(language, 24)}", onClick = onSettings, modifier = Modifier.focusRequester(settingsFocusRequester))
            Spacer(Modifier.width(10.dp))
            FocusActionButton(label = if (isRefreshing) "⟳  Обновление…" else "⟳  Обновить", onClick = onRefresh)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FocusActionButton(label = "▤  ${extra(language, 1)}", onClick = { onTabChange(false) }, selected = !favoritesTab)
            FocusActionButton(label = "★  ${extra(language, 2)} (${favorites.size})", onClick = { onTabChange(true) }, selected = favoritesTab)
        }
        if (groups.isNotEmpty()) {
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                FocusActionButton(
                    label = extra(language, 1),
                    onClick = { selectedGroup = "all" },
                    selected = selectedGroup == "all"
                )
                groups.forEach { group ->
                    FocusActionButton(
                        label = group,
                        onClick = { selectedGroup = group },
                        selected = selectedGroup == group
                    )
                }
            }
        }
        Column {
            Text(catalogStatus, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
            if (catalogUpdatedAt != null) Text(catalogUpdatedAt, style = MaterialTheme.typography.labelSmall)
            if (refreshError != null) Text(refreshError, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
        }
        if (filteredChannels.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    if (favoritesTab && visibleChannels.isEmpty()) extra(language, 3) else extra(language, 1),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 14.dp)
            ) {
                items(filteredChannels, key = { it.id }) { channel ->
                    ChannelRow(
                        channel = channel,
                        isFavorite = channel.id in favorites,
                        onSelect = { onSelect(channel.id) },
                        onToggleFavorite = { onToggleFavorite(channel.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ChannelRow(
    channel: Channel,
    isFavorite: Boolean,
    onSelect: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    var focused by remember { mutableStateOf(false) }
    val borderWidth by animateDpAsState(if (focused) 3.dp else 0.dp, label = "channelFocus")
    Card(
        modifier = Modifier.fillMaxWidth()
            .onFocusChanged { focused = it.isFocused }
            .border(BorderStroke(borderWidth, MaterialTheme.colorScheme.primary), RoundedCornerShape(14.dp))
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = if (focused) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface)
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(channel.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(channel.group, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
            }
            FocusActionButton(
                label = if (isFavorite) "★" else "☆",
                onClick = onToggleFavorite,
                selected = isFavorite
            )
        }
    }
}

@Composable
private fun FocusActionButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false
) {
    var focused by remember { mutableStateOf(false) }
    val borderWidth by animateDpAsState(if (focused) 3.dp else 1.dp, label = "buttonFocus")
    Button(
        onClick = onClick,
        modifier = modifier.onFocusChanged { focused = it.isFocused }
            .border(BorderStroke(borderWidth, if (focused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline), RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected || focused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (selected || focused) Color(0xFF07142F) else MaterialTheme.colorScheme.onSurface
        ),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SettingsDialog(
    language: String,
    controlsTimeout: Int,
    themeChoice: Int,
    onLanguage: (String) -> Unit,
    onTimeout: (Int) -> Unit,
    onTheme: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val languages = listOf("ru", "de", "en", "uk", "ro", "tr")
    val timeoutOptions = listOf(1, 3, 5, 10, 0)
    val themeNames = listOf(extra(language, 12), extra(language, 13), extra(language, 14))
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(extra(language, 24), style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(
                Modifier.heightIn(max = 540.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(tr(language, 2), style = MaterialTheme.typography.titleMedium)
                languages.forEach { code ->
                    SettingsChoice(languageNames[code] ?: code, language == code) { onLanguage(code) }
                }
                Divider()
                Text(extra(language, 5), style = MaterialTheme.typography.titleMedium)
                timeoutOptions.forEach { value ->
                    val label = when (value) {
                        1 -> extra(language, 6)
                        3 -> extra(language, 7)
                        5 -> extra(language, 8)
                        10 -> extra(language, 9)
                        else -> extra(language, 10)
                    }
                    SettingsChoice(label, controlsTimeout == value) { onTimeout(value) }
                }
                Divider()
                Text(extra(language, 27), style = MaterialTheme.typography.titleMedium)
                themeNames.forEachIndexed { index, name ->
                    SettingsChoice(name, themeChoice == index) { onTheme(index) }
                }
                Divider()
                Text(extra(language, 15), style = MaterialTheme.typography.titleMedium)
                Text(extra(language, 16), color = MaterialTheme.colorScheme.secondary)
            }
        },
        confirmButton = { FocusActionButton(label = "OK", onClick = onDismiss) }
    )
}

@Composable
private fun SettingsChoice(label: String, selected: Boolean, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val borderWidth by animateDpAsState(if (focused) 2.dp else 0.dp, label = "settingFocus")
    Row(
        Modifier.fillMaxWidth().onFocusChanged { focused = it.isFocused }
            .border(BorderStroke(borderWidth, MaterialTheme.colorScheme.primary), RoundedCornerShape(10.dp))
            .clickable(onClick = onClick).padding(horizontal = 8.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(label, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun LitePlayer(
    channel: Channel,
    language: String,
    controlsTimeout: Int,
    isFavorite: Boolean,
    onBack: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onToggleFavorite: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenList: () -> Unit
) {
    val context = LocalContext.current
    val exoPlayer = remember(context) { ExoPlayer.Builder(context).build() }
    val playerFocusRequester = remember { FocusRequester() }
    val firstControlFocusRequester = remember { FocusRequester() }
    var controlsVisible by remember { mutableStateOf(controlsTimeout == 0) }
    var isPlaying by remember { mutableStateOf(true) }
    var interactionTick by remember { mutableIntStateOf(0) }

    LaunchedEffect(channel.url) {
        exoPlayer.setMediaItem(MediaItem.fromUri(channel.url))
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
        isPlaying = true
    }
    DisposableEffect(exoPlayer) { onDispose { exoPlayer.release() } }

    LaunchedEffect(controlsTimeout) {
        if (controlsTimeout == 0) controlsVisible = true
    }
    LaunchedEffect(controlsVisible, controlsTimeout, channel.id, interactionTick) {
        if (controlsVisible && controlsTimeout > 0) {
            delay(controlsTimeout * 1000L)
            controlsVisible = false
        }
    }
    LaunchedEffect(controlsVisible) {
        if (controlsVisible) {
            runCatching { firstControlFocusRequester.requestFocus() }
        } else {
            runCatching { playerFocusRequester.requestFocus() }
        }
    }
    LaunchedEffect(Unit) { runCatching { playerFocusRequester.requestFocus() } }

    Box(
        Modifier.fillMaxSize()
            .onPreviewKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                if (event.key == Key.Back || event.key == Key.Escape) {
                    onBack()
                    return@onPreviewKeyEvent true
                }
                if (controlsVisible) {
                    interactionTick++
                    return@onPreviewKeyEvent false
                }
                when (event.key) {
                    Key.DirectionUp, Key.DirectionLeft -> { interactionTick++; onPrevious(); true }
                    Key.DirectionDown, Key.DirectionRight -> { interactionTick++; onNext(); true }
                    Key.DirectionCenter, Key.Enter, Key.NumPadEnter -> { interactionTick++; controlsVisible = true; true }
                    else -> false
                }
            }
            .focusRequester(playerFocusRequester)
            .focusable()
    ) {
        AndroidView(
            factory = { viewContext ->
                PlayerView(viewContext).apply {
                    player = exoPlayer
                    useController = false
                    resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    keepScreenOn = true
                    isFocusable = false
                    isFocusableInTouchMode = false
                }
            },
            update = { view ->
                view.player = exoPlayer
                view.resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            },
            modifier = Modifier.fillMaxSize()
        )
        if (controlsVisible) {
            Surface(
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 18.dp),
                color = Color(0xDD101820),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(Modifier.padding(horizontal = 12.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(channel.name, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(16.dp))
                    FocusActionButton(label = extra(language, 17), onClick = onOpenList, modifier = Modifier.focusRequester(firstControlFocusRequester))
                }
            }
            Surface(
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 22.dp),
                color = Color(0xDD101820),
                shape = RoundedCornerShape(18.dp)
            ) {
                Row(
                    Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FocusActionButton(label = "◀  ${extra(language, 18)}", onClick = onPrevious)
                    FocusActionButton(
                        label = if (isPlaying) "Ⅱ  ${extra(language, 19)}" else "▶  ${extra(language, 20)}",
                        onClick = {
                            if (exoPlayer.isPlaying) {
                                exoPlayer.pause()
                                isPlaying = false
                            } else {
                                exoPlayer.play()
                                isPlaying = true
                            }
                        }
                    )
                    FocusActionButton(label = "${extra(language, 21)}  ▶", onClick = onNext)
                    FocusActionButton(label = if (isFavorite) "★" else "☆", onClick = onToggleFavorite, selected = isFavorite)
                    FocusActionButton(label = "⚙", onClick = onOpenSettings)
                }
            }
        }
    }
}