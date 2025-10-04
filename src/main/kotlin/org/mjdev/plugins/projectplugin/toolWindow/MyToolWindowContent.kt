package org.mjdev.plugins.projectplugin.toolWindow

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.mjdev.plugins.projectplugin.modules.HotModule
import org.mjdev.plugins.projectplugin.modules.HotModuleLoader
import org.mjdev.plugins.projectplugin.renderer.RenderLayout
import org.mjdev.plugins.projectplugin.engines.ScriptEngineHolder.Companion.rememberScriptEngineHolder

@Composable
fun MyToolWindowContent(
    project: Project,
    moduleName: MutableState<String> = mutableStateOf("welcome")
) {
    val moduleState = remember {
        derivedStateOf {
            HotModuleLoader.load(moduleName.value)
        }
    }
    val scriptEngine = rememberScriptEngineHolder(moduleState.value.script)

    // Ensure scaffold exists and start watcher
//    LaunchedEffect(Unit) {
//        HotModuleLoader.ensureScaffold()
//        moduleState.value = HotModuleLoader.load()
//        val watcher = FileSystems.getDefault().newWatchService()
//        val dir = Paths.get(System.getProperty("user.dir"), "hot-modules", "active")
//        dir.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE)
//        try {
//            while (true) {
//                val key = watcher.take()
//                val changed = key.pollEvents().any()
//                if (changed) {
//                    ApplicationManager.getApplication().invokeLater {
//                        scriptEngine.reload()
//                        moduleState.value = HotModuleLoader.load()
//                    }
//                }
//                if (!key.reset()) break
//            }
//        } catch (_: Throwable) {
//        } finally {
//            watcher.close()
//        }
//    }

    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        val module = moduleState.value
        val layout = module.layoutJson?.takeIf { string ->
            string.isNotBlank()
        } ?: HotModule.error("Module: ${moduleName.value} not found.").layoutJson
        val script = module.script
        if (script != null) {
            LaunchedEffect(script) {
                val evalRes = runCatching {
                    scriptEngine.eval(script)
                }.onFailure { e ->
                    e.printStackTrace()
                }
                when {
                    evalRes.isFailure -> println("Script eval failed: ${evalRes.exceptionOrNull()?.message}")
                    evalRes.isSuccess -> println("Script ok.")
                }
            }
        }
        RenderLayout(
            json = layout ?: "{}"
        ) { id, action, state ->
            CoroutineScope(Dispatchers.Default).launch {
                if (action.isNotBlank()) {
                    val res = runCatching {
                        scriptEngine.invoke(action, state)
                    }.getOrNull()
                    when (res) {
                        null -> {
                            println("Something went wrong, script action returns null.")
                        }

                        is Map<*, *> -> {
                            println("Action map result: $res")
                        }

                        is String -> {
                            println("Action result: $res")
                        }
                    }
                }
            }
        }
    }
}
