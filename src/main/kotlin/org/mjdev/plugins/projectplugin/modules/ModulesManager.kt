package org.mjdev.plugins.projectplugin.modules

import androidx.compose.runtime.mutableStateListOf
import com.intellij.openapi.project.Project
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

    private var modulesWatcher: FilesWatcher? = null

    init {
        if (projectHasModules) {
            modulesWatcher = FilesWatcher("$projectRootDir/$baseDir") {
                // todo better performance
                reload()
            }
        }
        loadModules()
    }

    private fun reload() {
        modulesWatcher?.stop()
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
        val classLoader = javaClass.classLoader
        val resourceUrl = classLoader.getResource("modules")
            ?: return emptyList()
        return when (resourceUrl.protocol) {
            "jar" -> {
                val connection = resourceUrl.openConnection() as java.net.JarURLConnection
                connection.jarFile.entries().toList()
                    .filter { it.name.startsWith("modules/") && !it.isDirectory }
                    .map { it.name }
            }
            "file" -> {
                java.io.File(resourceUrl.toURI()).walk()
                    .filter { it.isFile }
                    .map { it.absolutePath }
                    .toList()
            }
            else -> emptyList()
        }
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
        modulesWatcher?.start()
    }

    @Suppress("SameParameterValue")
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
        fun isAvailable(project: Project): Boolean = true
    }
}
