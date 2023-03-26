import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

/**
 * Runs script saved in [fileName] using 'kotlinc -script' command.
 *
 * @param [onOutput]: callback function that uses output stream of the process
 * @param [onError]: callback function that uses error stream of the process
 * @param [onFinish]: callback function that uses exit code of the process. If process was terminated due to timeout, 'null' is returned as exit code
 * @param [fileName]: name of the file with script saved
 * @param [executionTimeThresholdInMs]: amount of milliseconds to wait for the process if it does not produce any output
 */
class Runner(
    val onOutput: (String) -> Unit,
    val onError: (String) -> Unit,
    val onFinish: (Int?) -> Unit,
    private val fileName: String = "scripts/tmp.kts",
    private val executionTimeThresholdInMs: Long = 600000 // TODO: change threshold in settings in gui
) : Runnable {

    override fun run() {
        val process = ProcessBuilder("kotlinc", "-script", fileName).start()

        var line: String?

        val reader = BufferedReader(InputStreamReader(process.inputStream))
        while (reader.readLine().also { line = it } != null) {
            onOutput(line!!)
        }

        val errorReader = BufferedReader(InputStreamReader(process.errorStream))
        while (errorReader.readLine().also { line = it } != null) {
            onError(line!!)
        }

        val finished = process.waitFor(executionTimeThresholdInMs, TimeUnit.MILLISECONDS)
        if (finished) onFinish(process.waitFor())
        else onFinish(null)
    }

}