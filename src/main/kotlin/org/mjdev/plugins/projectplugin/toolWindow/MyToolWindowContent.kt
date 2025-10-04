package org.mjdev.plugins.projectplugin.toolWindow

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Surface
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.mjdev.plugins.projectplugin.modules.ModulesManager
import org.mjdev.plugins.projectplugin.renderer.RenderLayout

@Composable
fun MyToolWindowContent(
    project: Project,
) {
    val modulesLoader = remember(project) {
        ModulesManager(project)
    }
    val currentModuleIdx = remember(project) {
        mutableStateOf(0)
    }
    val currentModule = remember(project) {
        derivedStateOf {
            modulesLoader.modules[currentModuleIdx.value]
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        val module = currentModule.value
        LaunchedEffect(module) {
            module.init()
        }
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            TabRow(
                modifier = Modifier.fillMaxWidth(),
                selectedTabIndex = currentModuleIdx.value
            ) {
                modulesLoader.modules.forEachIndexed { index, module ->
                    Tab(
                        text = { Text(module.name) },
                        selected = currentModuleIdx.value == index,
                        onClick = { currentModuleIdx.value = index }
                    )
                }
            }
            RenderLayout(
                json = module.layout
            ) { id, action, state ->
                CoroutineScope(Dispatchers.Default).launch {
                    if (action.isNotBlank()) {
                        val res = runCatching {
                            module.invoke(action, state)
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
}
