package org.mjdev.plugins.projectplugin.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import org.mjdev.plugins.projectplugin.MyBundle

@Service(Service.Level.PROJECT)
class MyProjectService(
    project: Project
) {
    init {
        thisLogger().info(MyBundle.message("projectService", project.name))
    }
}
