import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

class Runner(
    val onOutput: (String) -> Unit,
    val onError: (String) -> Unit,
    val onFinish: (Int?) -> Unit,
    private val fileName: String = "scripts/tmp.kts",
    private val executionTimeThresholdInMs: Long = 600000 // TODO: change threshold in settings in gui
) : Runnable {

    override fun run() {
        val process = ProcessBuilder("cmd", "/c", "kotlinc", "-script", fileName).start()

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