package org.mjdev.plugins.projectplugin.app

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

class MyProjectActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
    }
}