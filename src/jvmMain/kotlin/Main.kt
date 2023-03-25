import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.awt.FileDialog
import java.awt.Frame

object GUI {

    private var first = true
    private val highlighter = SyntaxHighlighter()
    private const val progressBarUpdateSpeed = 50L
    private const val deleteTmpPoll = 50L
    private const val supportedExtension = ".kts"

    private fun ScriptManager.addTmpScript(text: String) {
        addScriptFromText(text, "tmp$supportedExtension")
    }

    private fun ScriptManager.removeTmpScript() {
        removeFileWithScript("tmp$supportedExtension")
    }

    private fun executeWithTmpScript(text: String, conditionCallback: () -> Boolean, action: () -> Unit) {
        ScriptManager.addTmpScript(text)
        action()
        CoroutineScope(Dispatchers.Default).launch {
            while (conditionCallback()) delay(deleteTmpPoll)
            ScriptManager.removeTmpScript()
        }
    }

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    @Preview
    fun app() {
        var inputText by remember { mutableStateOf(TextFieldValue()) }
        var outputText by remember { mutableStateOf("") }
        var errorText by remember { mutableStateOf("") }
        var exitCode by remember { mutableStateOf("Not executed yet") }
        var isRunning by remember { mutableStateOf(false) }
        val availableFiles = remember { mutableStateListOf<String>() }
        var popupShown by remember { mutableStateOf(false) }
        var scriptSaveName by remember { mutableStateOf("") }
        var progress by remember { mutableStateOf(0.0) }
        var estimatedTimeLeft by remember { mutableStateOf<Int?>(null) }

        if (first) { // TODO: ???
            for (file in ScriptManager.getAllFiles()) {
                availableFiles.add(file.fileName.toString())
            }
            first = false
        }

        fun onOutput(str: String) {
            outputText += "$str\n"
        }

        fun onError(str: String) {
            if (errorText.isEmpty()) errorText += "Error occurred during the execution:\n"
            errorText += "$str\n"
        }

        fun onFinish(c: Int?) {
            exitCode = c?.toString() ?: "Timeout"
            isRunning = false
        }

        fun updateEstimatedTimeLeft(newTime: Int?) {
            estimatedTimeLeft = newTime
        }

        fun updateProgressBar(newValue: Double) {
            progress = newValue
        }

        fun updateEstimationsOnFinish() {
            estimatedTimeLeft = 0
            progress = 1.0
        }

        fun checkIfStillRunning() = isRunning

        fun onDoubleClick(file: String) {
            TimeEstimator.clearRuns()
            inputText =
                TextFieldValue(highlighter.highlight(ScriptManager.uploadScriptFromFile(file)), inputText.selection)
        }

        fun prepareNewRun() {
            outputText = ""
            errorText = ""
        }

        fun runScript() {
            prepareNewRun()

            executeWithTmpScript(inputText.text, ::checkIfStillRunning) {
                val runner = Runner(::onOutput, ::onError, ::onFinish)
                Thread(runner).start()
                isRunning = true
            }

            CoroutineScope(Dispatchers.Default).launch {
                TimeEstimator.updateProgressDuringRun(
                    progressBarUpdateSpeed,
                    ::checkIfStillRunning,
                    ::updateEstimatedTimeLeft,
                    ::updateProgressBar,
                    ::updateEstimationsOnFinish
                )
            }
        }

        @Composable
        if (popupShown) {
            AlertDialog(
                title = { Text("Please enter filename to save script") },
                onDismissRequest = { popupShown = false },
                text = {
                    TextField(
                        value = scriptSaveName,
                        onValueChange = { scriptSaveName = it }
                    )
                },
                buttons = {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            Button(
                                onClick = {
                                    popupShown = false
                                    val realScriptName = "$scriptSaveName.$supportedExtension"
                                    if (ScriptManager.addScriptFromText(inputText.text, filename = realScriptName)) {
                                        availableFiles.add(realScriptName)
                                    } else {
                                        // todo: error msg
                                    }
                                }
                            ) {
                                Text("Save")
                            }
                        }

                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            Button(
                                onClick = {
                                    popupShown = false
                                }
                            ) {
                                Text("Dismiss")
                            }
                        }
                    }
                }
            )
        }

        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().weight(2f).height(100.dp)
            ) {

                TextField(
                    value = inputText,
                    onValueChange = { value ->
                        val selection = value.selection
                        inputText = TextFieldValue(highlighter.highlight(value.text), selection)
                    },
                    label = { Text("Kotlin Code") },
                    modifier = Modifier.fillMaxHeight().weight(2f).padding(start = 16.dp)
                )

                Column(
                    modifier = Modifier.padding(start = 16.dp).weight(1f)
                )
                {
                    LazyColumn(
                        // todo add option to remove files
                        modifier = Modifier.fillMaxWidth()
                            .padding(end = 16.dp, bottom = 16.dp)
                            .background(Color.LightGray).weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        contentPadding = PaddingValues(bottom = 32.dp),
                    ) {
                        items(availableFiles) { file ->
                            Text(
                                file,
                                modifier = Modifier.clickable {
                                    onDoubleClick(file)
                                }
                            )
                        }
                    }

                    Row {
                        Button(onClick = { popupShown = true }, modifier = Modifier.padding(end = 16.dp)) {
                            Text("Save current")
                        }
                        Button(onClick = {
                            val dialog = FileDialog(null as Frame?, "Choose a file")
                            dialog.isVisible = true
                            if (dialog.files != null) {
                                for (file in dialog.files) {
                                    if (file.extension == "kts") {
                                        if (ScriptManager.addScriptFromFile(file)) {
                                            availableFiles.add(file.name)
                                        } else {
                                            // TODO: error message
                                        }
                                    }
                                }
                            }
                        }, modifier = Modifier.padding(end = 16.dp)) {
                            Text("Upload new")
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {

                OutlinedTextField(
                    value = TextFieldValue(
                        AnnotatedString(outputText, SpanStyle(color = Color.Black)) + AnnotatedString(
                            errorText,
                            SpanStyle(color = Color.Red)
                        )
                    ),
                    onValueChange = { },
                    label = { Text("Output") },
                    modifier = Modifier.fillMaxHeight().padding(start = 16.dp, bottom = 16.dp).weight(2f)
                )

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        "Estimated running time: ${if (estimatedTimeLeft == null) "N/A" else "$estimatedTimeLeft s"}",
                        modifier = Modifier.padding(start = 16.dp, top = 16.dp)
                    )
                    LinearProgressIndicator(
                        progress = progress.toFloat(),
                        modifier = Modifier.padding(start = 16.dp).height(20.dp)
                    )

                    Row {
                        OutlinedTextField(
                            value = exitCode,
                            onValueChange = { },
                            label = { Text("Return code") },
                            modifier = Modifier.padding(16.dp).weight(1f),
                        )

                        Button(
                            onClick = { runScript() },
                            modifier = Modifier.padding(16.dp).weight(1f),
                            enabled = !isRunning
                        ) {
                            Text("Run")
                        }
                    }
                }
            }
        }
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        GUI.app()
    }
}
