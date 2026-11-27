package ai.antigem.ui.workspace

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.antigem.i18n.L
import ai.antigem.ui.components.AppToastHelper
import ai.antigem.ui.components.ChatToastType

/**
 * Reusable Prompt Vault modal bottom sheet: browse built-in developer workflows,
 * pin favorites to the empty-chat hub, and create/delete custom prompts.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromptVaultSheet(
    onDismiss: () -> Unit,
    onInsertPrompt: (String) -> Unit
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) { PromptVaultStore.init(context) }

    val prompts = PromptVaultStore.prompts
    var showCreate by remember { mutableStateOf(false) }
    var draftTitle by remember { mutableStateOf("") }
    var draftBody by remember { mutableStateOf("") }
    val primary = MaterialTheme.colorScheme.primary

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
                .padding(bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Bookmarks, null, tint = primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = L.ui.workspace.promptVault,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = L.ui.workspace.promptVaultHint,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f)
                    )
                }
                FilledTonalButton(
                    onClick = { showCreate = !showCreate },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(if (showCreate) Icons.Outlined.Close else Icons.Outlined.Add, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(if (showCreate) L.common.cancel else L.ui.workspace.newPrompt, fontSize = 12.sp)
                }
            }

            if (showCreate) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, primary.copy(alpha = 0.25f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = draftTitle,
                            onValueChange = { draftTitle = it },
                            label = { Text(L.ui.workspace.promptTitle) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = draftBody,
                            onValueChange = { draftBody = it },
                            label = { Text(L.ui.workspace.promptBody) },
                            minLines = 3,
                            maxLines = 5,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            Button(
                                enabled = draftTitle.isNotBlank() && draftBody.isNotBlank(),
                                onClick = {
                                    PromptVaultStore.addPrompt(draftTitle, draftBody)
                                    draftTitle = ""
                                    draftBody = ""
                                    showCreate = false
                                    AppToastHelper.showToast(L.ui.workspace.promptSaved, ChatToastType.SUCCESS)
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(L.common.save)
                            }
                        }
                    }
                }
            }

            if (prompts.isEmpty()) {
                Box(Modifier.fillMaxWidth().padding(28.dp), contentAlignment = Alignment.Center) {
                    Text(L.ui.workspace.promptEmpty, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(prompts, key = { it.id }) { p ->
                        val title = p.resolvedTitle()
                        val body = p.resolvedBody()
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    onInsertPrompt(body)
                                    onDismiss()
                                },
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(
                                1.dp,
                                if (p.pinned) primary.copy(alpha = 0.30f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = title,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = { PromptVaultStore.togglePin(p.id) },
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (p.pinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                                            contentDescription = if (p.pinned) L.ui.workspace.unpinPrompt else L.ui.workspace.pinPrompt,
                                            tint = if (p.pinned) primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    if (!p.builtIn) {
                                        IconButton(
                                            onClick = { PromptVaultStore.deletePrompt(p.id) },
                                            modifier = Modifier.size(30.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Delete,
                                                contentDescription = L.ui.workspace.deletePrompt,
                                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.78f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = body,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(primary.copy(alpha = 0.12f))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = L.ui.workspace.insertPrompt,
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = primary
                                        )
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
