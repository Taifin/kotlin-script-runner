import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import java.io.File

/**
 * Holds list of keywords to highlight given text
 */
class SyntaxHighlighter {
    private var keywords: Set<String>

    constructor() {
        keywords = setOf(
            "val", "var", "while", "for",
            "class", "fun", "public", "private",
            "object", "true", "false", "if"
        )
    }

    constructor(kws: Set<String>) {
        keywords = kws
    }

    /**
      *  Constructs SyntaxHighlighter based on file with keywords
      *
      *  @param fileWithKws: file with keywords, each must be on a separate line
     */
    constructor(fileWithKws: String) {
        val file = File(fileWithKws)
        val set = mutableSetOf<String>()
        for (line in file.readLines()) {
            set.add(line)
        }
        keywords = set.toSet()
    }

    /**
     * Highlights given text, making all keywords that contained in [keywords] of [keywordsColor] color. Other words will be colored into [textColor].
     *
     * @param inputText: the text to be highlighted
     * @param keywordsColor: color to apply to the keywords in the text, red by default
     * @param textColor: color to apply to the rest of the text, black by default
     */
    fun highlight(inputText: String, keywordsColor: Color = Color.Red, textColor: Color = Color.Black): AnnotatedString {
        val annotatedText = AnnotatedString.Builder().apply {
            var startIndex = 0
            var endIndex: Int

            while (startIndex < inputText.length) {
                endIndex = inputText.indexOfAny(charArrayOf(' ', '\n'), startIndex)
                if (endIndex == -1) {
                    endIndex = inputText.length
                }

                val word = inputText.substring(startIndex, endIndex)
                val style = if (keywords.contains(word)) {
                    SpanStyle(color = keywordsColor)
                } else {
                    SpanStyle(color = textColor)
                }

                append(AnnotatedString(inputText.substring(startIndex, endIndex), style))

                if (endIndex < inputText.length && inputText[endIndex] == '\n') {
                    append("\n")
                    startIndex = endIndex + 1
                } else {
                    startIndex = endIndex + 1
                    append(" ")
                }
            }
        }.toAnnotatedString()
        return annotatedText
    }
}