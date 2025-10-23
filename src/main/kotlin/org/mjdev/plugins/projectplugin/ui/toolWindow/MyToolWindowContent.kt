package org.mjdev.plugins.projectplugin.ui.toolWindow

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.intellij.openapi.project.Project
import org.mjdev.plugins.projectplugin.extensions.CoroutineExt.launch
import org.mjdev.plugins.projectplugin.modules.ModulesManager
import org.mjdev.plugins.projectplugin.ui.RenderLayout

@Composable
fun MyToolWindowContent(
    project: Project,
) {
    val modulesManager by remember(project) {
        mutableStateOf(ModulesManager(project))
    }
    var currentModuleIdx by remember(project) {
        mutableStateOf(0)
    }
    val currentModule by remember(modulesManager.modulesState) {
        derivedStateOf {
            modulesManager.modulesState[currentModuleIdx]
        }
    }
    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        LaunchedEffect(currentModule) {
            currentModule.init()
        }
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            @Suppress("COMPOSE_APPLIER_CALL_MISMATCH")
            TabRow(
                modifier = Modifier.fillMaxWidth(),
                selectedTabIndex = currentModuleIdx
            ) {
                modulesManager.modulesState.forEachIndexed { index, module ->
                    Tab(
                        text = {
                            Text(module.name)
                        },
                        selected = currentModuleIdx == index,
                        onClick = { currentModuleIdx = index }
                    )
                }
            }
            RenderLayout(
                module = currentModule
            ) { id, action, state ->
                launch {
                    if (action.isNotBlank()) {
                        val res = runCatching {
                            currentModule.invoke(action, state)
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
