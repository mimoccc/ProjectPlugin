package org.mjdev.plugins.projectplugin.modules

import org.json.JSONObject
import org.mjdev.plugins.projectplugin.engines.ScriptEngineHolder
import kotlin.script.experimental.api.ResultWithDiagnostics

data class HotModule(
    val manifestData: FileData,
    val layoutData: FileData,
    val scriptData: FileData,
) {
    val manifest: JSONObject = manifestData.toString().let { JSONObject(it) }
    val layout: JSONObject = layoutData.toString().let { JSONObject(it) }

    val name: String = manifest.optString("name")?.takeIf { it.isNotEmpty() } ?: "-"
    val version: String = manifest.optString("version")?.takeIf { it.isNotEmpty() } ?: "-"
    val index: Int = manifest.optString("index")?.takeIf { it.isNotEmpty() }?.toInt() ?: -1

    private val scriptEngine = ScriptEngineHolder(scriptData.toString())

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
}
