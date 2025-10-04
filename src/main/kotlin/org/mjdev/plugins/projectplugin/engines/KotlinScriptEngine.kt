package org.mjdev.plugins.projectplugin.engines

import com.intellij.openapi.application.PathManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlin.script.experimental.api.ScriptEvaluationConfiguration
import kotlin.script.experimental.host.StringScriptSource
import kotlin.script.experimental.jvm.dependenciesFromClassContext
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.script.experimental.jvmhost.createJvmCompilationConfigurationFromTemplate

class KotlinScriptEngine : IScriptEngine {
    private val scriptingHost by lazy { BasicJvmScriptingHost() }
    private val compilationConfiguration = createJvmCompilationConfigurationFromTemplate<Any> {
        jvm {
            dependenciesFromClassContext(
                javaClass.kotlin,
                wholeClasspath = true
            )
        }
    }
    private val evaluationConfiguration by lazy {
        ScriptEvaluationConfiguration {}
    }

    init {
        System.setProperty("idea.home.path", PathManager.getHomePath())
        System.setProperty("idea.config.path", PathManager.getConfigPath())
    }

    override suspend fun eval(
        script: String
    ) = CoroutineScope(
        Dispatchers.Default
    ).async {
        scriptingHost.eval(
            script.toScriptSource(),
            compilationConfiguration,
            evaluationConfiguration
        )
    }.await()

    private fun String.toScriptSource() = StringScriptSource(this)
}