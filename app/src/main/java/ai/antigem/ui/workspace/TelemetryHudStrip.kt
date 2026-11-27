package ai.antigem.ui.workspace

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.antigem.i18n.L

/**
 * Compact live session telemetry & quick-action strip right above the input bar.
 */
@Composable
fun TelemetryHudStrip(
    agentLabel: String,
    modelLabel: String?,
    workspaceLabel: String,
    messageCount: Int,
    isStreaming: Boolean,
    onOpenCommandPalette: () -> Unit,
    onOpenPromptVault: () -> Unit,
    onOpenThemes: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primary = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            border = BorderStroke(0.75.dp, onSurface.copy(alpha = 0.08f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Box(
                    Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(if (isStreaming) MaterialTheme.colorScheme.tertiary else primary)
                )
                Text(
                    text = buildString {
                        append(agentLabel)
                        if (!modelLabel.isNullOrBlank()) {
                            append(" · ")
                            append(modelLabel)
                        }
                        append(" · ")
                        append(L.ui.workspace.messagesCount(messageCount))
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = onSurface.copy(alpha = 0.78f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        HudShortcutChip(
            icon = Icons.Outlined.Bolt,
            label = L.ui.workspace.commandPalette,
            accent = primary,
            onClick = onOpenCommandPalette
        )
        HudShortcutChip(
            icon = Icons.Outlined.Bookmarks,
            label = L.ui.workspace.promptVault,
            accent = MaterialTheme.colorScheme.secondary,
            onClick = onOpenPromptVault
        )
        HudShortcutChip(
            icon = Icons.Outlined.Palette,
            label = L.common.themes,
            accent = MaterialTheme.colorScheme.tertiary,
            onClick = onOpenThemes
        )
        Surface(
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            border = BorderStroke(0.75.dp, onSurface.copy(alpha = 0.07f))
        ) {
            Text(
                text = workspaceLabel,
                fontSize = 10.5.sp,
                color = onSurface.copy(alpha = 0.56f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun HudShortcutChip(
    icon: ImageVector,
    label: String,
    accent: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clip(RoundedCornerShape(50)).clickable(onClick = onClick),
        shape = RoundedCornerShape(50),
        color = accent.copy(alpha = 0.11f),
        border = BorderStroke(0.75.dp, accent.copy(alpha = 0.24f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, null, tint = accent, modifier = Modifier.size(13.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = accent,
                maxLines = 1
            )
        }
    }
}
