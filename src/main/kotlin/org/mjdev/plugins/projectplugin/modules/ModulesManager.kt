package org.mjdev.plugins.projectplugin.modules

import com.intellij.openapi.project.Project
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.name

class ModulesManager(
    project: Project,
) {
    val baseDir = "hot-modules"
    val basePath = project.basePath ?: ""
    val modulesDir = Paths.get("$basePath/$baseDir")
    val moduleNames = listModulesInProject() ?: listModulesInResources() ?: emptyList()
    val modules = mutableListOf<Module>()
    val projectHasModules = Files.exists(modulesDir)

    fun listModulesInProject() : List<String>? {
        return if (!projectHasModules) null
        else Files.list(modulesDir).map { f -> f.name }.toList()
    }

    fun listModulesInResources() : List<String>? {
        val resources = this::class.java.classLoader.getResources(baseDir)
        val allNames = mutableSetOf<String>()
        while (resources.hasMoreElements()) {
            val url = resources.nextElement()
            try {
                val uri = url.toURI()
                val path = Paths.get(uri)
                if (Files.exists(path)) {
                    Files.list(path).forEach { f ->
                        allNames.add(f.fileName.toString())
                    }
                }
            } catch (_: Exception) {
                // ignore errors on unsupported URLs, e.g. in production jar
            }
        }
        return if (allNames.isNotEmpty()) allNames.toList() else null
    }

    companion object {
        fun isAvailable(project: Project): Boolean {
            return true // todo
        }
    }
}