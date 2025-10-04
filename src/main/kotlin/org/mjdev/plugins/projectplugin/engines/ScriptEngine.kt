package org.mjdev.plugins.projectplugin.engines

import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.functions
import kotlin.script.experimental.api.EvaluationResult
import kotlin.script.experimental.api.ResultValue
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.valueOrNull

class ScriptEngineHolder(
    private val script: String?,
    private val engineDelegate: IScriptEngine = KotlinScriptEngine()
) {
    suspend fun eval(
        script: String?
    ): ResultWithDiagnostics<EvaluationResult>? = engineDelegate.eval(script ?: "")

    suspend fun invoke(
        functionName: String,
        state: Map<String, Any?> = emptyMap()
    ): Any? = runCatching {
        val scriptObject = engineDelegate.eval(script ?: "")
        val instance = scriptObject?.valueOrNull()?.returnValue?.let {
            if (it is ResultValue.Value) it.value else null
        } ?: throw IllegalStateException("No script instance available. Call eval() first.")
        val func = instance::class.functions.find { it.name == functionName }
            ?: throw NoSuchMethodException("Function $functionName not found")
        return if (func.isSuspend) {
            func.callSuspend(instance, state)
        } else {
            func.call(instance, state)
        }
    }.onFailure { e ->
        e.printStackTrace()
    }
}
