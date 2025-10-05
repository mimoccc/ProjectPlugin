package org.mjdev.plugins.projectplugin.modules

import kotlinx.coroutines.Job
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import org.mjdev.plugins.projectplugin.extensions.CoroutineExt.launch

class FilesWatcher(
    private val dirToWatch: String,
    private val onChanged: (Path) -> Unit
) : AutoCloseable {
    @Volatile
    private var watcher: WatchService? = null
    private val dir by lazy { Paths.get(dirToWatch) }
    private val keys = mutableMapOf<WatchKey, Path>()

    private var job: Job? = null

    fun start() {
        watcher = FileSystems.getDefault().newWatchService()
        registerAll(dir)
        job = launch {
            try {
                while (job?.isActive == true) {
                    val currentWatcher = watcher ?: break
                    val key = try {
                        currentWatcher.take()
                    } catch (e: ClosedWatchServiceException) {
                        break
                    } catch (e: InterruptedException) {
                        break
                    }
                    val watchedDir = keys[key] ?: continue
                    key.pollEvents().forEach { event ->
                        val relativePath = event.context() as? Path ?: return@forEach
                        val absolutePath = watchedDir.resolve(relativePath)
                        if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                            if (Files.exists(absolutePath) && Files.isDirectory(absolutePath)) {
                                try {
                                    registerAll(absolutePath)
                                } catch (e: ClosedWatchServiceException) {
                                    return@forEach
                                }
                            }
                        }
                        println("file changed: $absolutePath")
                        onChanged(absolutePath)
                    }
                    if (!key.reset()) {
                        keys.remove(key)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun registerAll(start: Path) {
        val currentWatcher = watcher ?: return
        Files.walkFileTree(start, object : SimpleFileVisitor<Path>() {
            override fun preVisitDirectory(
                dir: Path,
                attrs: BasicFileAttributes
            ) = FileVisitResult.CONTINUE.also {
                try {
                    keys[dir.register(
                        currentWatcher,
                        StandardWatchEventKinds.ENTRY_CREATE,
                        StandardWatchEventKinds.ENTRY_MODIFY,
                        StandardWatchEventKinds.ENTRY_DELETE
                    )] = dir
                } catch (e: ClosedWatchServiceException) {
                    // ignore
                }
            }
        })
    }

    fun stop() {
        job?.cancel()
        keys.keys.forEach { it.cancel() }
        keys.clear()
        watcher?.close()
        watcher = null
    }

    override fun close() = stop()
}
