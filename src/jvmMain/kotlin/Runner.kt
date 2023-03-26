import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Runs script saved in [fileName] using 'kotlinc -script' command.
 *
 * @property onOutput callback function that uses output stream of the process
 * @property onError callback function that uses error stream of the process
 * @property onFinish callback function that uses exit code of the process. If process was terminated due to timeout, 'null' is returned as exit code
 * @property fileName name of the file with script saved
 * @property executionTimeThresholdInMs amount of milliseconds to wait for the process if it does not produce any output
 */
class Runner(
    val onOutput: (String) -> Unit,
    val onError: (String) -> Unit,
    val onFinish: (Int?) -> Unit,
    private val fileName: String = "scripts/tmp.kts",
    private val executionTimeThresholdInMs: Long = 60000 // TODO: change threshold in settings in gui
) {

    suspend fun run() {
        val osName = System.getProperty("os.name").lowercase()
        val process = withContext(Dispatchers.IO) {
            if (osName.contains("win")) {
                // windows needs different command to run kotlinc from Process
                ProcessBuilder("cmd", "/c", "kotlinc", "-script", fileName).start()
            } else {
                ProcessBuilder("kotlinc", "-script", fileName).start()
            }
        }

        withContext(Dispatchers.IO) {
            process.inputStream.bufferedReader().useLines { lines ->
                lines.forEach { line ->
                    withContext(Dispatchers.Default) { onOutput(line) }
                }
            }

            process.errorStream.bufferedReader().useLines { lines ->
                lines.forEach { line ->
                    withContext(Dispatchers.Default) { onError(line) }
                }
            }
        }

        val finished = withTimeoutOrNull(executionTimeThresholdInMs) {
            withContext(Dispatchers.IO) {
                process.waitFor()
            }
        } != null

        withContext(Dispatchers.Default) {
            if (finished) onFinish(process.exitValue())
            else onFinish(null)
        }
    }

}