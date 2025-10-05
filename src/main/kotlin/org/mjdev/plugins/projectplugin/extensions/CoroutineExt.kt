package org.mjdev.plugins.projectplugin.extensions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.rememberCoroutineScope as rememberCoroutineScopeEx
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

object CoroutineExt {

    val safeCoroutineContext =
        SupervisorJob() + Dispatchers.Default + CoroutineExceptionHandler { _, t ->
            t.printStackTrace()
        }

    fun launch(
        context: CoroutineContext = safeCoroutineContext,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> Unit
    ): Job = CoroutineScope(context).launch(context, start, block)

    fun <T> async(
        context: CoroutineContext = safeCoroutineContext,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> T
    ): Deferred<T> = CoroutineScope(context).async(context, start, block)

    @Composable
    inline fun rememberCoroutineScope(
        crossinline getContext: @DisallowComposableCalls () -> CoroutineContext = {
            safeCoroutineContext
        }
    ): CoroutineScope = rememberCoroutineScopeEx(getContext)

}