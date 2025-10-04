package org.mjdev.plugins.projectplugin.toolWindow

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.awt.ComposePanel
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import org.mjdev.plugins.projectplugin.modules.ModulesManager
import org.mjdev.plugins.projectplugin.theme.AppTheme

class MyToolWindowFactory(
) : ToolWindowFactory {
    private val composePanel by lazy { ComposePanel() }
    private val contentFactory by lazy { ContentFactory.getInstance() }

    @OptIn(ExperimentalComposeUiApi::class)
    override fun createToolWindowContent(
        project: Project,
        toolWindow: ToolWindow
    ) {
        invokeLater {
            composePanel.setContent {
                AppTheme {
                    MyToolWindowContent(project)
                }
            }
            contentFactory.createContent(
                composePanel,
                "",
                false
            ).apply {
                setDisposer {
                    composePanel.dispose()
                }
            }.also { tab ->
                toolWindow.contentManager.addContent(tab)
            }
        }
    }

    override fun shouldBeAvailable(
        project: Project
    ) = ModulesManager.isAvailable(project)
}
