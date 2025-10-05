package org.mjdev.plugins.projectplugin.modules

import org.json.JSONObject
import org.mjdev.plugins.projectplugin.engines.ScriptEngineHolder
import org.mjdev.plugins.projectplugin.modules.FileData.Companion.readFile
import org.mjdev.plugins.projectplugin.modules.FileData.Companion.readResource
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.absolutePathString
import kotlin.script.experimental.api.ResultWithDiagnostics

data class HotModule(
    val moduleDirPath: String,
    val manifestData: FileData,
    val layoutData: FileData,
    val scriptData: FileData,
) {
    val manifest: JSONObject = JSONObject(manifestData.toString())
    val layout: JSONObject = JSONObject(layoutData.toString())

    val name: String = manifest.optString("name")?.takeIf { it.isNotEmpty() } ?: "-"
    val version: String = manifest.optString("version")?.takeIf { it.isNotEmpty() } ?: "-"
    val index: Int = manifest.optString("index")?.takeIf { it.isNotEmpty() }?.toInt() ?: -1

    private val scriptEngine = ScriptEngineHolder(scriptData.toString())

    val fileModulePath = manifestData.fileBaseDir
    val resourcePath = manifestData.resourceBaseDir

    suspend fun init() {
        runCatching {
            scriptEngine.eval(scriptData.toString())
        }.onSuccess { evalRes ->
            when (evalRes) {
                is ResultWithDiagnostics.Success -> {
                    println("Script ok.")
                }

                is ResultWithDiagnostics.Failure -> {
                    println("Script eval failed:")
                    evalRes.reports.forEach { report ->
                        println(" - ${report.message}")
                        report.exception?.printStackTrace()
                    }
                }

                null -> null
            }
        }.onFailure { e ->
            println("Error evaluating script: ${e.message}")
            e.printStackTrace()
        }
    }

    suspend fun invoke(
        functionName: String,
        state: Map<String, Any?> = emptyMap()
    ): Any? = runCatching {
        scriptEngine.invoke(functionName, state)
    }.getOrNull()

    fun getFileData(
        fileName: String
    ): String? {
        val file = Paths.get(fileModulePath).resolve(fileName)
        val resource = "$resourcePath/$fileName"
        return if (Files.exists(file)) {
            readFile(file.absolutePathString())
        } else {
            readResource(resource)
        }
    }
}
