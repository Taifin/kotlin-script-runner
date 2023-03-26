import kotlinx.coroutines.delay
import java.util.*
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Uses information of the previous runs of the script to estimate time of a new run.
 * @property maxRuns determines how many runs will be considered for estimation
 * @property higherWeightPercentage determines how many of the most recent runs will have more weight than others
 * @property weight weight of the most recent runs
 */
object TimeEstimator {
    private const val maxRuns = 10
    private const val higherWeightPercentage = 10
    private const val weight = 0.9

    private val runs = LinkedList<Double>()
    private var weights = List(10) { 1.0 }

    private fun addRun(run: Double) {
        if (runs.size >= maxRuns) runs.pop()
        runs.push(run)
        if (runs.size in 2 until maxRuns) weights =
            (0 until maxRuns).map { index -> (if (index < maxRuns / higherWeightPercentage) weight else (1 - weight) / (runs.size - 1)) }
    }

    private fun estimate(): Double {
        var time = 0.0
        val n = runs.size.coerceAtMost(maxRuns)
        if (n == 0) return time
        for (i in 0 until n) time += runs[i] * weights[i]
        return time
    }

    suspend fun updateProgressDuringRun(
        progressBarUpdateSpeed: Long,
        isRunning: () -> Boolean,
        estimatedTimeLeftCallback: (Int?) -> Unit,
        progressBarCallback: (Double) -> Unit,
        onRunFinishCallback: () -> Unit,
    ) {
        val estimatedTimeTotal = estimate()
        val startTime = System.currentTimeMillis().toDouble()
        // if it is the first run and no estimations can be done
        if (estimatedTimeTotal == 0.0) {
            while (isRunning()) delay(progressBarUpdateSpeed)
        } else {
            while (isRunning()) {
                val elapsedTime = (System.currentTimeMillis() - startTime)
                estimatedTimeLeftCallback(Integer.max(0, ((estimatedTimeTotal - elapsedTime) / 1000).roundToInt()))
                progressBarCallback(min(1.0, elapsedTime / estimatedTimeTotal))
                delay(progressBarUpdateSpeed)
            }
        }
        onRunFinishCallback()
        addRun(System.currentTimeMillis() - startTime)
    }

    fun clearRuns() {
        runs.clear()
        weights = List(10) { 1.0 }
    }
}