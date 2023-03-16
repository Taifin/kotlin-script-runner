import java.io.BufferedReader
import java.io.InputStreamReader

class Runner(val onOutput: (String) -> Unit, val onError: (String) -> Unit, val onFinish: (Int) -> Unit) : Runnable {
    private val fileName = "scripts/tmp.kts"

    override fun run() {
        val process = ProcessBuilder("kotlinc", "-script", fileName).start()

        var line: String?

        val reader = BufferedReader(InputStreamReader(process.inputStream))
        while (reader.readLine().also { line = it } != null) {
            onOutput(line!!)
        }

        val errorReader = BufferedReader(InputStreamReader(process.errorStream))
        while (errorReader.readLine().also{ line = it} != null) {
            onError(line!!)
        }

        val exitCode = process.waitFor()
        onFinish(exitCode)
    }

}