package ai.antigem.ui.workspace

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.antigem.i18n.L
import ai.antigem.i18n.LanguageManager

/**
 * Modern Bento empty-chat dashboard rendered before the first message of a session.
 * Gives instant 1-tap access to workspaces, Command Palette, Prompt Vault, themes, and starter prompts.
 */
@Composable
fun CommandHubDashboard(
    agentName: String,
    modelName: String?,
    workspaceName: String,
    onInsertPrompt: (String) -> Unit,
    onOpenCommandPalette: () -> Unit,
    onOpenPromptVault: () -> Unit,
    onOpenThemePanel: () -> Unit,
    onToggleLanguage: () -> Unit,
    onOpenIde: () -> Unit,
    onOpenTerminal: () -> Unit,
    onOpenBrowser: () -> Unit,
    onOpenFiles: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) { PromptVaultStore.init(context) }

    val vaultPrompts = PromptVaultStore.prompts
    val pinnedPrompts = remember(vaultPrompts.toList()) {
        vaultPrompts.filter { it.pinned }.ifEmpty { vaultPrompts.take(4) }.take(6)
    }

    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val tertiary = MaterialTheme.colorScheme.tertiary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val cardBg = MaterialTheme.colorScheme.surfaceContainerLow

    Column(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 560.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero glass-inspired banner card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            color = cardBg,
            border = BorderStroke(1.dp, primary.copy(alpha = 0.22f)),
            tonalElevation = 2.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            listOf(
                                primary.copy(alpha = 0.14f),
                                tertiary.copy(alpha = 0.08f),
                                Color.Transparent
                            )
                        )
                    )
                    .padding(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(primary.copy(alpha = 0.16f))
                                .border(0.75.dp, primary.copy(alpha = 0.35f), RoundedCornerShape(50))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.AutoAwesome, null, tint = primary, modifier = Modifier.size(13.dp))
                                Spacer(Modifier.width(5.dp))
                                Text(
                                    text = buildString {
                                        append(agentName)
                                        if (!modelName.isNullOrBlank()) {
                                            append(" · ")
                                            append(modelName)
                                        }
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = primary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = L.ui.workspace.workspaceLabel(workspaceName),
                            fontSize = 11.sp,
                            color = onSurface.copy(alpha = 0.62f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Text(
                        text = L.chat.screen.howCanIHelpYouToday,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold,
                        color = onSurface,
                        modifier = Modifier.semantics { heading() }
                    )
                    Text(
                        text = L.ui.workspace.howCanWeBuild,
                        fontSize = 12.5.sp,
                        lineHeight = 18.sp,
                        color = onSurface.copy(alpha = 0.68f)
                    )

                    Spacer(Modifier.height(2.dp))
                    // Primary action pills: Command Palette, Prompt Vault, Themes, Language
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HeroActionPill(
                            icon = Icons.Outlined.Bolt,
                            label = L.ui.workspace.commandPalette,
                            highlighted = true,
                            accent = primary,
                            onClick = onOpenCommandPalette
                        )
                        HeroActionPill(
                            icon = Icons.Outlined.Bookmarks,
                            label = L.ui.workspace.promptVault,
                            highlighted = false,
                            accent = secondary,
                            onClick = onOpenPromptVault
                        )
                        HeroActionPill(
                            icon = Icons.Outlined.Palette,
                            label = L.common.themes,
                            highlighted = false,
                            accent = tertiary,
                            onClick = onOpenThemePanel
                        )
                        HeroActionPill(
                            icon = Icons.Outlined.Translate,
                            label = if (LanguageManager.currentId == "vi") "EN / VI" else "VI / EN",
                            highlighted = false,
                            accent = primary,
                            onClick = onToggleLanguage
                        )
                    }
                }
            }
        }

        // Bento 2-column workspace tiles
        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                BentoWorkspaceTile(
                    icon = Icons.Outlined.Code,
                    title = L.ui.workspace.openIdeTitle,
                    subtitle = L.ui.workspace.openIdeSub,
                    tint = primary,
                    onClick = onOpenIde,
                    modifier = Modifier.weight(1f)
                )
                BentoWorkspaceTile(
                    icon = Icons.Outlined.Terminal,
                    title = L.ui.workspace.openTermTitle,
                    subtitle = L.ui.workspace.openTermSub,
                    tint = secondary,
                    onClick = onOpenTerminal,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                BentoWorkspaceTile(
                    icon = Icons.Outlined.Language,
                    title = L.ui.workspace.openBrowserTitle,
                    subtitle = L.ui.workspace.openBrowserSub,
                    tint = tertiary,
                    onClick = onOpenBrowser,
                    modifier = Modifier.weight(1f)
                )
                if (onOpenFiles != null) {
                    BentoWorkspaceTile(
                        icon = Icons.Outlined.FolderOpen,
                        title = L.ui.workspace.openFilesTitle,
                        subtitle = L.ui.workspace.openFilesSub,
                        tint = primary,
                        onClick = onOpenFiles,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    BentoWorkspaceTile(
                        icon = Icons.Outlined.Bookmarks,
                        title = L.ui.workspace.promptVault,
                        subtitle = L.ui.workspace.promptVaultHint,
                        tint = primary,
                        onClick = onOpenPromptVault,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Pinned Starter Prompts row
        if (pinnedPrompts.isNotEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = L.ui.workspace.quickHub,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = onSurface.copy(alpha = 0.72f)
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = L.ui.workspace.promptVault,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = primary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onOpenPromptVault)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    pinnedPrompts.forEach { p ->
                        StarterPromptChip(
                            title = p.resolvedTitle(),
                            preview = p.resolvedBody(),
                            accent = primary,
                            onClick = { onInsertPrompt(p.resolvedBody()) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroActionPill(
    icon: ImageVector,
    label: String,
    highlighted: Boolean,
    accent: Color,
    onClick: () -> Unit
) {
    val bg = if (highlighted) accent else MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
    val fg = if (highlighted) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    val border = if (highlighted) Color.Transparent else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
    Surface(
        shape = RoundedCornerShape(50),
        color = bg,
        border = BorderStroke(1.dp, border),
        modifier = Modifier.clip(RoundedCornerShape(50)).clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, null, tint = fg, modifier = Modifier.size(15.dp))
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = fg, maxLines = 1)
        }
    }
}

@Composable
private fun BentoWorkspaceTile(
    icon: ImageVector,
    title: String,
    subtitle: String,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(tint.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = tint, modifier = Modifier.size(18.dp))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun StarterPromptChip(
    title: String,
    preview: String,
    accent: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(196.dp)
            .clip(RoundedCornerShape(15.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(15.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, accent.copy(alpha = 0.18f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(accent)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = title,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = preview,
                fontSize = 11.sp,
                lineHeight = 15.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
