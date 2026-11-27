package ai.antigem.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.antigem.theme.AppTheme
import ai.antigem.theme.BuiltInThemes
import ai.antigem.theme.ThemeManager
import ai.antigem.i18n.L

/**
 * Settings → Themes: pick the light theme and the dark theme from previews of every built-in and custom theme.
 * Light / Dark / Auto (the mode cards above) decides which of the two shows.
 */
@Composable
fun ThemeGallery(cardBg: Color, themeMode: String, onSetThemeMode: (String) -> Unit) {
    val lightId = ThemeManager.lightId
    val darkId = ThemeManager.darkId
    val custom = ThemeManager.customThemes
    val all = remember(custom) { ThemeManager.all() }
    var toDelete by remember { mutableStateOf<AppTheme?>(null) }

    fun pick(t: AppTheme) {
        // a fixed mode switches to the picked theme's side so it shows at once; Auto keeps following the system
        ThemeManager.select(t, switchMode = false)
        val wantMode = if (t.dark) "DARK" else "LIGHT"
        if (themeMode != "SYSTEM" && themeMode != wantMode) onSetThemeMode(wantMode)
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        GalleryHeader(L.settings.themeGallery.lightTheme, L.settings.themeGallery.usedInLightModeAndBy)
        ThemeGrid(all.filter { !it.dark }, lightId, cardBg, ::pick) { toDelete = it }
        GalleryHeader(L.settings.themeGallery.darkTheme, L.settings.themeGallery.usedInDarkModeAndBy)
        ThemeGrid(all.filter { it.dark }, darkId, cardBg, ::pick) { toDelete = it }
        Text(
            L.settings.themeGallery.makeYourOwnAddTheTheme,
            fontSize = 11.5.sp, lineHeight = 16.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }

    toDelete?.let { t ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("Delete “${t.name}”?") },
            text = { Text(L.settings.themeGallery.thisCustomThemeIsRemovedIf) },
            confirmButton = { TextButton(onClick = { ThemeManager.deleteCustom(t.id); toDelete = null }) { Text(L.common.delete) } },
            dismissButton = { TextButton(onClick = { toDelete = null }) { Text(L.common.cancel) } }
        )
    }
}

@Composable
private fun GalleryHeader(title: String, hint: String) {
    Column {
        Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Text(hint, fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
    }
}

@Composable
private fun ThemeGrid(themes: List<AppTheme>, selectedId: String, cardBg: Color, onPick: (AppTheme) -> Unit, onDelete: (AppTheme) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        themes.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { t ->
                    ThemeCard(t, t.id == selectedId, cardBg, Modifier.weight(1f), { onPick(t) }, if (t.builtIn) null else ({ onDelete(t) }))
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

private val NEW_FLAGSHIP_THEME_IDS = setOf(
    "kuro_obsidian",
    "cyber_jade",
    "crimson_velvet",
    "royal_sapphire",
    "pearl_glass",
    "lotus_silk"
)

/**
 * A small live preview: gradient accent header, theme background, frosted card with text lines and CTA button,
 * user bubble, syntax strip, and status dots. Also used by the floating [ThemeQuickPanel].
 */
@Composable
internal fun ThemeCard(t: AppTheme, selected: Boolean, cardBg: Color, modifier: Modifier, onClick: () -> Unit, onDelete: (() -> Unit)?) {
    val c = remember(t) { ThemeManager.resolvedColors(t).mapValues { Color(it.value.toInt()) } }
    fun col(k: String, d: Color = Color.Gray) = c[k] ?: d
    val accent = MaterialTheme.colorScheme.primary
    val isNewFlagship = t.builtIn && t.id in NEW_FLAGSHIP_THEME_IDS
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier
            .clip(shape)
            .background(cardBg)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) accent else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                shape = shape
            )
            .clickable { onClick() }
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(90.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            col("background"),
                            col("surface_variant", col("background")).copy(alpha = 0.45f)
                        )
                    )
                )
        ) {
            // Top gradient accent rail
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.horizontalGradient(
                            listOf(col("primary"), col("secondary", col("primary")), col("tertiary", col("primary")))
                        )
                    )
            )
            Box(Modifier.fillMaxSize().padding(start = 8.dp, end = 8.dp, top = 9.dp, bottom = 8.dp)) {
                // a frosted assistant card with text lines and CTA pill
                Column(
                    Modifier
                        .fillMaxWidth(0.70f)
                        .clip(RoundedCornerShape(9.dp))
                        .background(col("card", col("surface")))
                        .border(0.75.dp, col("primary").copy(alpha = 0.18f), RoundedCornerShape(9.dp))
                        .padding(7.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(Modifier.fillMaxWidth(0.82f).height(5.dp).clip(RoundedCornerShape(50)).background(col("on_background")))
                    Box(Modifier.fillMaxWidth(0.56f).height(4.dp).clip(RoundedCornerShape(50)).background(col("text_secondary", col("on_surface_variant"))))
                    Box(
                        Modifier
                            .width(32.dp)
                            .height(10.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Brush.horizontalGradient(listOf(col("primary"), col("secondary", col("primary")))))
                    )
                }
                // your message bubble
                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 12.dp)
                        .width(34.dp)
                        .height(14.dp)
                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 8.dp, bottomEnd = 3.dp))
                        .background(col("user_bubble", col("surface_variant")))
                )
                // syntax strip + semantic dots
                Row(Modifier.align(Alignment.BottomStart), verticalAlignment = Alignment.CenterVertically) {
                    Row(
                        Modifier
                            .clip(RoundedCornerShape(5.dp))
                            .background(col("code_bg"))
                            .padding(horizontal = 5.dp, vertical = 3.5.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.5.dp)
                    ) {
                        listOf("syntax_keyword", "syntax_string", "syntax_function", "syntax_number").forEach { k ->
                            Box(Modifier.width(8.dp).height(3.dp).clip(RoundedCornerShape(50)).background(col(k)))
                        }
                    }
                    Spacer(Modifier.width(6.dp))
                    listOf("success", "warning", "danger", "info").forEach { k ->
                        Box(Modifier.padding(end = 3.dp).size(6.5.dp).clip(CircleShape).background(col(k)))
                    }
                }
                // Top-right star for default theme or brush for custom theme
                if (BuiltInThemes.recommendedRank(t) == 0) {
                    Box(
                        Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 3.dp, y = (-3).dp)
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(accent)
                            .border(1.5.dp, col("background"), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Star, L.settings.themeGallery.defaultTheme, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(11.dp))
                    }
                }
                if (isNewFlagship) {
                    Box(
                        Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 2.dp, y = (-3).dp)
                            .clip(RoundedCornerShape(50))
                            .background(col("primary"))
                            .border(1.dp, col("background"), RoundedCornerShape(50))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = L.ui.workspace.badgeNew,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = col("on_primary", Color.White),
                            maxLines = 1
                        )
                    }
                }
                if (!t.builtIn) {
                    Box(
                        Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 3.dp, y = (-3).dp)
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.tertiary)
                            .border(1.5.dp, col("background"), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Brush, L.settings.themeGallery.customTheme, tint = MaterialTheme.colorScheme.onTertiary, modifier = Modifier.size(11.dp))
                    }
                }
            }
        }
        Row(
            Modifier
                .fillMaxWidth()
                .background(if (selected) accent.copy(alpha = 0.08f) else Color.Transparent)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(col("primary"))
            )
            Spacer(Modifier.width(6.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    t.name,
                    fontSize = 12.5.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            if (onDelete != null) {
                Icon(
                    Icons.Outlined.Delete,
                    L.common.delete,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                    modifier = Modifier.size(18.dp).clip(CircleShape).clickable { onDelete() }
                )
                Spacer(Modifier.width(4.dp))
            }
            if (selected) Icon(Icons.Filled.CheckCircle, L.common.inUse, tint = accent, modifier = Modifier.size(18.dp))
        }
    }
}
