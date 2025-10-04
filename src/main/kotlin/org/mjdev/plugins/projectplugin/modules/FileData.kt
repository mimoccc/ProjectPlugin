package org.mjdev.plugins.projectplugin.modules

import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class FileData(
    val filesDir: Path,
    val baseDir: String,
    val moduleName: String,
    val fileName: String,
) {
    val resourcePath = "$baseDir/$moduleName/$fileName"
    val filePath = "$filesDir/$baseDir/$moduleName/$fileName"

    val dataRes: String? = readResource(resourcePath)
    val dataFile: String? = readFile(filePath)

    val isFile: Boolean
        get() = dataFile != null

    val isRes: Boolean
        get() = dataRes != null

    override fun toString(): String {
        return dataFile ?: dataRes ?: ""
    }

    companion object {
        fun readFile(
            filePath: String,
        ) = runCatching {
            Files.readString(Paths.get(filePath))
        }.getOrNull()

        private fun readResource(
            resourcePath: String,
        ): String? = runCatching {
            val classLoader = ModulesManager::class.java.classLoader
            classLoader.getResourceAsStream(resourcePath)?.use { stream ->
                BufferedReader(
                    InputStreamReader(stream, StandardCharsets.UTF_8)
                ).readText()
            }
        }.getOrNull()
    }
}