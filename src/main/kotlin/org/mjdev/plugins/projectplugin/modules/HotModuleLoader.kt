package org.mjdev.plugins.projectplugin.modules

import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

object HotModuleLoader {
    private val baseDirName = "hot-modules"
    private val classLoader = HotModuleLoader::class.java.classLoader
    private val overrideDir: Path by lazy {
        Path.of(System.getProperty("user.dir"))
            .resolve(baseDirName)
    }
    private val manifestPath = "manifest.json"
    private val layoutPath = "layout.json"
    private val scriptPath = "script.kts"

    private fun readResource(
        path: String // module-name/file-name
    ): String? = runCatching {
        val resourcePath = "$baseDirName/$path"
        classLoader.getResourceAsStream(resourcePath).use { stream ->
            @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
            BufferedReader(
                InputStreamReader(
                    stream,
                    StandardCharsets.UTF_8
                )
            ).readText()
        }
    }.getOrNull()

    private fun readFile(
        path: String // module-name/file-name
    ) = runCatching {
        Files.readString(overrideDir.resolve(path))
    }.getOrNull()

    private fun getFileData(
        path: String // module-name/file-name
    ) = readFile(path) ?: readResource(path)

    fun load(
        moduleName: String
    ): HotModule {
        val manifestStr: String? = getFileData("$moduleName/$manifestPath")
        val layout: String? = getFileData("$moduleName/$layoutPath")
        val script: String? = getFileData("$moduleName/$scriptPath")
        val manifestJson : JSONObject = manifestStr?.let { JSONObject(it) } ?: JSONObject()
        val name: String = manifestJson.optString("name")?.takeIf { it.isNotEmpty() } ?: "-"
        val version: String? = manifestJson.optString("version")?.takeIf { it.isNotEmpty() }
        return HotModule(name, version, layout, script)
    }
}
