package ai.antigem.ui.workspace

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ai.antigem.i18n.L
import ai.antigem.i18n.LanguageManager

data class PaletteAction(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val accent: Color,
    val badge: String? = null,
    val onExecute: () -> Unit
)

/**
 * Universal Command Palette for rapid fuzzy navigation across IDE/Terminal/Browser/Files,
 * live Vietnamese/English toggling, Theme Picker, Prompt Vault, Network Inspector, and Settings.
 */
@Composable
fun CommandPaletteDialog(
    onDismiss: () -> Unit,
    onNewChat: () -> Unit,
    onOpenIde: () -> Unit,
    onOpenTerminal: () -> Unit,
    onOpenBrowser: () -> Unit,
    onOpenFiles: (() -> Unit)?,
    onOpenNotes: (() -> Unit)?,
    onOpenRunPanel: () -> Unit,
    onOpenThemes: () -> Unit,
    onToggleThemeDarkLight: () -> Unit,
    onToggleLanguage: () -> Unit,
    onOpenLanguageSettings: () -> Unit,
    onOpenThemeSettings: () -> Unit,
    onOpenPromptVault: () -> Unit,
    onInsertPrompt: (String) -> Unit,
    onOpenNetworkInspector: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenSpotify: () -> Unit
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) { PromptVaultStore.init(context) }

    var query by remember { mutableStateOf("") }
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val tertiary = MaterialTheme.colorScheme.tertiary
    val isVi = LanguageManager.currentId == "vi"

    val vaultPrompts = PromptVaultStore.prompts
    val actions = remember(isVi, vaultPrompts.toList()) {
        buildList {
            add(
                PaletteAction(
                    id = "new_chat",
                    title = L.chat.screen.newChat,
                    subtitle = L.ui.workspace.quickHub,
                    icon = Icons.Outlined.AddComment,
                    accent = primary,
                    onExecute = { onDismiss(); onNewChat() }
                )
            )
            add(
                PaletteAction(
                    id = "prompt_vault",
                    title = L.ui.workspace.promptVault,
                    subtitle = L.ui.workspace.promptVaultHint,
                    icon = Icons.Outlined.Bookmarks,
                    accent = secondary,
                    badge = L.ui.workspace.badgeNew,
                    onExecute = { onDismiss(); onOpenPromptVault() }
                )
            )
            add(
                PaletteAction(
                    id = "toggle_lang",
                    title = if (isVi) L.ui.workspace.switchToEn else L.ui.workspace.switchToVi,
                    subtitle = L.common.language,
                    icon = Icons.Outlined.Translate,
                    accent = tertiary,
                    onExecute = { onToggleLanguage(); onDismiss() }
                )
            )
            add(
                PaletteAction(
                    id = "theme_picker",
                    title = L.ui.workspace.openThemePicker,
                    subtitle = L.common.themes,
                    icon = Icons.Outlined.Palette,
                    accent = primary,
                    onExecute = { onDismiss(); onOpenThemes() }
                )
            )
            add(
                PaletteAction(
                    id = "toggle_dark_light",
                    title = L.ui.workspace.toggleDarkLight,
                    subtitle = L.common.themes,
                    icon = Icons.Outlined.DarkMode,
                    accent = secondary,
                    onExecute = { onToggleThemeDarkLight(); onDismiss() }
                )
            )
            add(
                PaletteAction(
                    id = "open_ide",
                    title = L.ui.workspace.openIdeTitle,
                    subtitle = L.ui.workspace.openIdeSub,
                    icon = Icons.Outlined.Code,
                    accent = primary,
                    onExecute = { onDismiss(); onOpenIde() }
                )
            )
            add(
                PaletteAction(
                    id = "open_term",
                    title = L.ui.workspace.openTermTitle,
                    subtitle = L.ui.workspace.openTermSub,
                    icon = Icons.Outlined.Terminal,
                    accent = secondary,
                    onExecute = { onDismiss(); onOpenTerminal() }
                )
            )
            add(
                PaletteAction(
                    id = "open_browser",
                    title = L.ui.workspace.openBrowserTitle,
                    subtitle = L.ui.workspace.openBrowserSub,
                    icon = Icons.Outlined.Language,
                    accent = tertiary,
                    onExecute = { onDismiss(); onOpenBrowser() }
                )
            )
            if (onOpenFiles != null) {
                add(
                    PaletteAction(
                        id = "open_files",
                        title = L.ui.workspace.openFilesTitle,
                        subtitle = L.ui.workspace.openFilesSub,
                        icon = Icons.Outlined.FolderOpen,
                        accent = primary,
                        onExecute = { onDismiss(); onOpenFiles() }
                    )
                )
            }
            if (onOpenNotes != null) {
                add(
                    PaletteAction(
                        id = "open_notes",
                        title = L.chat.screen.openNotes,
                        subtitle = L.ui.workspace.quickHub,
                        icon = Icons.Outlined.EditNote,
                        accent = secondary,
                        onExecute = { onDismiss(); onOpenNotes() }
                    )
                )
            }
            add(
                PaletteAction(
                    id = "open_run",
                    title = L.chat.screen.openRunPanel,
                    subtitle = L.ui.workspace.quickHub,
                    icon = Icons.Outlined.PlayCircle,
                    accent = tertiary,
                    onExecute = { onDismiss(); onOpenRunPanel() }
                )
            )
            add(
                PaletteAction(
                    id = "open_lang_settings",
                    title = L.ui.workspace.openLanguageSettings,
                    subtitle = L.common.settings,
                    icon = Icons.Outlined.Language,
                    accent = primary,
                    onExecute = { onDismiss(); onOpenLanguageSettings() }
                )
            )
            add(
                PaletteAction(
                    id = "open_theme_settings",
                    title = L.common.themes,
                    subtitle = L.common.settings,
                    icon = Icons.Outlined.Palette,
                    accent = secondary,
                    onExecute = { onDismiss(); onOpenThemeSettings() }
                )
            )
            add(
                PaletteAction(
                    id = "net_inspector",
                    title = L.ui.workspace.openNetworkInspector,
                    subtitle = L.ui.workspace.sessionTelemetry,
                    icon = Icons.Outlined.NetworkCheck,
                    accent = tertiary,
                    onExecute = { onDismiss(); onOpenNetworkInspector() }
                )
            )
            add(
                PaletteAction(
                    id = "settings",
                    title = L.common.settings,
                    subtitle = "AntiGem",
                    icon = Icons.Outlined.Settings,
                    accent = primary,
                    onExecute = { onDismiss(); onOpenSettings() }
                )
            )
            add(
                PaletteAction(
                    id = "spotify",
                    title = L.spotify.player.nowPlaying,
                    subtitle = "Spotify",
                    icon = Icons.Outlined.MusicNote,
                    accent = ai.antigem.ui.spotify.SpotifyGreen,
                    onExecute = { onDismiss(); onOpenSpotify() }
                )
            )
            vaultPrompts.take(8).forEach { vp ->
                add(
                    PaletteAction(
                        id = "vault_${vp.id}",
                        title = vp.resolvedTitle(),
                        subtitle = vp.resolvedBody(),
                        icon = Icons.Outlined.AutoAwesome,
                        accent = secondary,
                        badge = L.ui.workspace.insertPrompt,
                        onExecute = { onDismiss(); onInsertPrompt(vp.resolvedBody()) }
                    )
                )
            }
        }
    }

    val filtered = remember(query, actions) {
        val q = query.trim().lowercase()
        if (q.isEmpty()) actions
        else actions.filter {
            it.title.lowercase().contains(q) ||
                it.subtitle.lowercase().contains(q) ||
                it.id.lowercase().contains(q)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 560.dp)
                .heightIn(max = 580.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)),
            shadowElevation = 24.dp
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Bolt, null, tint = primary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = L.ui.workspace.commandPalette,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Outlined.Close, L.common.close, modifier = Modifier.size(18.dp))
                    }
                }

                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text(L.ui.workspace.commandPaletteHint, fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Outlined.Search, null, modifier = Modifier.size(18.dp)) },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { query = "" }) {
                                Icon(Icons.Outlined.Close, L.common.close, modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                if (filtered.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = L.ui.workspace.noCommandsMatch,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f, fill = false),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(filtered, key = { it.id }) { item ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable(onClick = item.onExecute),
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(item.accent.copy(alpha = 0.14f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(item.icon, null, tint = item.accent, modifier = Modifier.size(17.dp))
                                    }
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            text = item.title,
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = item.subtitle,
                                            fontSize = 11.5.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    item.badge?.let { b ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(50))
                                                .background(item.accent.copy(alpha = 0.15f))
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(b, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = item.accent)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
