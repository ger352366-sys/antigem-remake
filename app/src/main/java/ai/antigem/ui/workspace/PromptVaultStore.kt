package ai.antigem.ui.workspace

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import ai.antigem.i18n.L
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

data class VaultPrompt(
    val id: String,
    val title: String,
    val body: String,
    val category: String = "custom",
    val pinned: Boolean = false,
    val builtIn: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun resolvedTitle(): String = when (id) {
        "builtin_debug" -> L.ui.workspace.starterDebugTitle
        "builtin_arch" -> L.ui.workspace.starterArchTitle
        "builtin_refactor" -> L.ui.workspace.starterRefactorTitle
        "builtin_tests" -> L.ui.workspace.starterTestsTitle
        "builtin_review" -> L.ui.workspace.starterReviewTitle
        "builtin_perf" -> L.ui.workspace.starterPerfTitle
        else -> title
    }

    fun resolvedBody(): String = when (id) {
        "builtin_debug" -> L.ui.workspace.starterDebugBody
        "builtin_arch" -> L.ui.workspace.starterArchBody
        "builtin_refactor" -> L.ui.workspace.starterRefactorBody
        "builtin_tests" -> L.ui.workspace.starterTestsBody
        "builtin_review" -> L.ui.workspace.starterReviewBody
        "builtin_perf" -> L.ui.workspace.starterPerfBody
        else -> body
    }
}

/**
 * Persistent Prompt Vault backed by `filesDir/antigem_prompt_vault.json`.
 * Built-in templates resolve dynamically via [L.ui.workspace], so language switches update them live.
 */
object PromptVaultStore {
    private const val FILE_NAME = "antigem_prompt_vault.json"

    private val builtInSeeds = listOf(
        VaultPrompt(
            id = "builtin_debug",
            title = "Debug & Root Cause",
            body = "",
            category = "debug",
            pinned = true,
            builtIn = true
        ),
        VaultPrompt(
            id = "builtin_arch",
            title = "Architecture Blueprint",
            body = "",
            category = "architecture",
            pinned = true,
            builtIn = true
        ),
        VaultPrompt(
            id = "builtin_refactor",
            title = "Clean Refactor",
            body = "",
            category = "refactor",
            pinned = false,
            builtIn = true
        ),
        VaultPrompt(
            id = "builtin_tests",
            title = "Generate Tests",
            body = "",
            category = "testing",
            pinned = false,
            builtIn = true
        ),
        VaultPrompt(
            id = "builtin_review",
            title = "Security & Diff Review",
            body = "",
            category = "review",
            pinned = true,
            builtIn = true
        ),
        VaultPrompt(
            id = "builtin_perf",
            title = "Profile & Optimize",
            body = "",
            category = "performance",
            pinned = false,
            builtIn = true
        )
    )

    val prompts = mutableStateListOf<VaultPrompt>()
    private val lock = Any()
    private var appContext: Context? = null
    @Volatile
    private var loaded = false

    fun init(context: Context) {
        if (loaded) return
        synchronized(lock) {
            if (loaded) return
            appContext = context.applicationContext
            reloadLocked()
            loaded = true
        }
    }

    private fun storeFile(ctx: Context): File = File(ctx.filesDir, FILE_NAME)

    private fun reloadLocked() {
        val ctx = appContext ?: return
        val f = storeFile(ctx)
        val customs = ArrayList<VaultPrompt>()
        val pinnedOverrides = HashMap<String, Boolean>()

        if (f.exists()) {
            runCatching {
                val root = JSONObject(f.readText())
                root.optJSONObject("pinned")?.let { p ->
                    p.keys().forEach { k -> pinnedOverrides[k] = p.optBoolean(k, false) }
                }
                root.optJSONArray("custom")?.let { arr ->
                    for (i in 0 until arr.length()) {
                        val obj = arr.optJSONObject(i) ?: continue
                        val id = obj.optString("id").takeIf { it.isNotBlank() } ?: continue
                        val title = obj.optString("title").takeIf { it.isNotBlank() } ?: continue
                        val body = obj.optString("body")
                        customs.add(
                            VaultPrompt(
                                id = id,
                                title = title,
                                body = body,
                                category = obj.optString("category", "custom").ifBlank { "custom" },
                                pinned = obj.optBoolean("pinned", false),
                                builtIn = false,
                                createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                            )
                        )
                    }
                }
            }
        }

        val builtIns = builtInSeeds.map { seed ->
            seed.copy(pinned = pinnedOverrides[seed.id] ?: seed.pinned)
        }
        val combined = (customs.sortedByDescending { it.createdAt } + builtIns)
            .sortedWith(compareByDescending<VaultPrompt> { it.pinned }.thenBy { it.builtIn }.thenByDescending { it.createdAt })

        prompts.clear()
        prompts.addAll(combined)
    }

    private fun persistLocked() {
        val ctx = appContext ?: return
        runCatching {
            val root = JSONObject()
            val pinnedObj = JSONObject()
            val customArr = JSONArray()
            prompts.forEach { p ->
                if (p.builtIn) {
                    pinnedObj.put(p.id, p.pinned)
                } else {
                    customArr.put(
                        JSONObject()
                            .put("id", p.id)
                            .put("title", p.title)
                            .put("body", p.body)
                            .put("category", p.category)
                            .put("pinned", p.pinned)
                            .put("createdAt", p.createdAt)
                    )
                }
            }
            root.put("pinned", pinnedObj)
            root.put("custom", customArr)
            storeFile(ctx).writeText(root.toString(2))
        }
    }

    fun addPrompt(title: String, body: String, category: String = "custom", pinned: Boolean = true): VaultPrompt {
        synchronized(lock) {
            val item = VaultPrompt(
                id = "custom_" + UUID.randomUUID().toString().take(8),
                title = title.trim(),
                body = body.trim(),
                category = category,
                pinned = pinned,
                builtIn = false
            )
            prompts.add(0, item)
            resortLocked()
            persistLocked()
            return item
        }
    }

    fun togglePin(id: String) {
        synchronized(lock) {
            val idx = prompts.indexOfFirst { it.id == id }
            if (idx >= 0) {
                val cur = prompts[idx]
                prompts[idx] = cur.copy(pinned = !cur.pinned)
                resortLocked()
                persistLocked()
            }
        }
    }

    fun deletePrompt(id: String) {
        synchronized(lock) {
            val removed = prompts.removeAll { it.id == id && !it.builtIn }
            if (removed) persistLocked()
        }
    }

    private fun resortLocked() {
        val sorted = prompts.sortedWith(
            compareByDescending<VaultPrompt> { it.pinned }
                .thenBy { it.builtIn }
                .thenByDescending { it.createdAt }
        )
        prompts.clear()
        prompts.addAll(sorted)
    }
}
