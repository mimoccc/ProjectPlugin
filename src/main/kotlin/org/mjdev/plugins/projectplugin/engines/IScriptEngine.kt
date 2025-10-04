package org.mjdev.plugins.projectplugin.engines

import kotlin.script.experimental.api.EvaluationResult
import kotlin.script.experimental.api.ResultWithDiagnostics

interface IScriptEngine {
    suspend fun eval(
        script: String
    ): ResultWithDiagnostics<EvaluationResult>?
}