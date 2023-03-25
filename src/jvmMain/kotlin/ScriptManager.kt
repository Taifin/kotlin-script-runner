import java.io.File
import java.nio.file.Files
import java.nio.file.Path

object ScriptManager {

    private val scriptsDir = Path.of("scripts")
    private var size = 0
    private var allFiles: MutableList<Path> = mutableListOf()

    init {
        if (!Files.exists(scriptsDir)) Files.createDirectory(scriptsDir)
        for (file in Files.list(scriptsDir)) allFiles.add(file)
        size = allFiles.size
    }

    fun getAllFiles() = allFiles.toList()

    fun addScriptFromText(content: String, filename: String): Boolean {
        // todo: validate path
        val new = File("$scriptsDir/$filename")
        new.writeText(content)
        size++
        allFiles.add(Path.of(new.path))
        return true
    }

    fun addScriptFromFile(filename: File): Boolean {
        return if (Files.exists(Path.of(filename.absolutePath))) {
            Files.copy(Path.of(filename.absolutePath), Path.of("$scriptsDir/${filename.name}"))
            size++
            allFiles.add(Path.of("$scriptsDir/${filename.name}"))
            true
        } else {
            false
        }
    }

    fun uploadScriptFromFile(filename: String): String {
        return Files.readString(Path.of("$scriptsDir/${filename}"))
    }

    fun removeFileWithScript(filename: String): Boolean {
        val pathToScript = Path.of("${scriptsDir}/${filename}")
        return allFiles.removeIf { file -> file == pathToScript && Files.deleteIfExists(pathToScript) }
    }
}