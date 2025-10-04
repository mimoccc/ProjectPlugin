package org.mjdev.plugins.projectplugin.modules

import com.intellij.openapi.project.Project
import io.github.classgraph.ClassGraph
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
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

    private val classLoader
        get() = ModulesManager::class.java.classLoader

    private val modulesDir
        get() = Paths.get("$basePath/$baseDir")

    val modules: List<HotModule>
        get() = (listModulesInProject() ?: listModulesInResources()).map {
            load(it)
        }.sortedBy {
            it.index
        }

    private val basePath
        get() = project.basePath ?: ""

    private val projectHasModules
        get() = Files.exists(modulesDir)

    private fun listModulesInProject(): List<String>? =
        if (!projectHasModules) null
        else Files.list(modulesDir)
            .filter { Files.isDirectory(it) }
            .map { it.name }
            .toList()

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

    private fun readResource(
        path: String
    ): String? = runCatching {
        val resourcePath = "$baseDir/$path"
        classLoader.getResourceAsStream(resourcePath)?.use { stream ->
            BufferedReader(
                InputStreamReader(stream, StandardCharsets.UTF_8)
            ).readText()
        }
    }.getOrNull()

    private fun readFile(path: String) = runCatching {
        Files.readString(modulesDir.resolve(path))
    }.getOrNull()

    private fun getFileData(path: String) =
        readFile(path) ?: readResource(path)

    fun load(moduleName: String): HotModule {
        val manifestData: String? = getFileData("$moduleName/$manifestPath")
        val layoutData: String? = getFileData("$moduleName/$layoutPath")
        val scriptData: String? = getFileData("$moduleName/$scriptPath")
        return HotModule(
            manifestData = manifestData,
            layoutData = layoutData,
            scriptData = scriptData
        )
    }

    companion object {
        fun isAvailable(project: Project): Boolean {
            val path: Path = Paths.get(project.basePath ?: "")
            return Files.exists(path)
        }
    }

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
}
