package org.mjdev.plugins.projectplugin.modules

import androidx.compose.runtime.mutableStateListOf
import com.intellij.openapi.project.Project
import io.github.classgraph.ClassGraph
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.name

class ModulesManager(
    private val project: Project,
) {
    private val baseDir = "hot-modules"
    private val manifestPath = "manifest.json"
    private val layoutPath = "layout.json"
    private val scriptPath = "script.kts"

    private val projectRootDir
        get() = Paths.get(project.basePath ?: "")

    private val projectHasModules
        get() = Files.exists(projectRootDir.resolve(baseDir))

    var modulesState = mutableStateListOf<HotModule>()

    private val modulesWatcher = FilesWatcher("$projectRootDir/$baseDir") {
        // todo better performance
        reload()
    }

    init {
        loadModules()
    }

    private fun reload() {
        modulesWatcher.stop()
        loadModules()
    }

    private fun listModulesInProject(): List<String>? = if (projectHasModules)
        Files.list(
            projectRootDir.resolve(baseDir)
        ).filter {
            Files.isDirectory(it)
        }.map {
            it.name
        }.toList()
    else null

    private fun listModulesInResources(): List<String> {
        val modules = mutableSetOf<String>()
        ClassGraph()
            .acceptPaths(baseDir)
            .scan()
            .use { scanResult ->
                scanResult.allResources.forEach { resource ->
                    val path = resource.path
                    if (path.startsWith("$baseDir/")) {
                        val moduleName = path
                            .removePrefix("$baseDir/")
                            .substringBefore("/")
                        if (moduleName.isNotEmpty()) {
                            modules.add(moduleName)
                        }
                    }
                }
            }
        return modules.toList()
    }

    private fun loadModules() {
        (listModulesInProject() ?: listModulesInResources()).map {
            load(it)
        }.sortedBy {
            it.index
        }.let { modules ->
            modulesState.clear()
            modulesState.addAll(modules)
        }
        modulesWatcher.start()
    }

    private fun getFileData(
        filesDir: Path,
        baseDir: String,
        moduleName: String,
        fileName: String
    ) = FileData(
        filesDir = filesDir,
        baseDir = baseDir,
        moduleName = moduleName,
        fileName = fileName,
    )

    fun load(
        moduleName: String
    ) = HotModule(
        moduleDirPath = "$projectRootDir/$baseDir/$moduleName/",
        manifestData = getFileData(
            projectRootDir,
            baseDir,
            moduleName,
            manifestPath
        ),
        layoutData = getFileData(
            projectRootDir,
            baseDir,
            moduleName,
            layoutPath
        ),
        scriptData = getFileData(
            projectRootDir,
            baseDir,
            moduleName,
            scriptPath
        )
    )

    companion object {
        fun isAvailable(project: Project): Boolean {
            val path: Path = Paths.get(project.basePath ?: "")
            return Files.exists(path)
        }
    }
}
