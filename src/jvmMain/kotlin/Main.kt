import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.runtime.*

class GUI {
    companion object Window {
        @Composable
        @Preview
        fun app() {
            var inputText by remember { mutableStateOf("") }
            var outputText by remember { mutableStateOf("") }
            var errorText by remember { mutableStateOf("") }
            var exitCode by remember { mutableStateOf("Not executed yet") }
            var blocking by remember { mutableStateOf(false) }

            Column(modifier = Modifier.fillMaxSize()) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    label = { Text("Kotlin Code") },
                    modifier = Modifier.fillMaxWidth().weight(2f).padding(16.dp)
                )

                OutlinedTextField(
                    value = outputText + errorText,
                    onValueChange = { },
                    label = { Text("Output") },
                    modifier = Modifier.fillMaxWidth().weight(2f).padding(16.dp),
                )

                Row {
                    OutlinedTextField(
                        value = exitCode,
                        onValueChange = { },
                        label = { Text("Return code") },
                        modifier = Modifier.weight(2f).padding(16.dp),
                    )

                    Button(
                        onClick = {
                            val runner = Runner({ str: String -> outputText = "$str\n" },
                                { str: String -> errorText += "Error occurred during the execution:\n$str\n" },
                                { c: Int ->
                                    run {
                                        exitCode = c.toString()
                                        blocking = false
                                    }
                                })
                            Thread(runner).start()
                            blocking = true
                        },
                        modifier = Modifier.padding(16.dp),
                        enabled = !blocking
                    ) {
                        Text("Run")
                    }
                }


            }
        }
    }
}
// TODO: option to upload script from file

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        GUI.app()
    }
}
