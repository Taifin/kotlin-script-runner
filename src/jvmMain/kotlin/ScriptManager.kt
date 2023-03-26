import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.notExists

/**
 * Manages scripts as files, providing access to existing scripts and ability to add new scripts from text and other files.
 *
 * Please note that this manager does not provide any safety at all, it only performs basic checks on presence of some files.
 * It will definitely break if something go wrong, for example, if a file will be manually deleted during the execution of the program.
 */
object ScriptManager {

    private val scriptsDir = Path.of("scripts")
    private var size = 0
    private var allFiles: MutableList<Path> = mutableListOf()

    init {
        if (!Files.exists(scriptsDir)) Files.createDirectory(scriptsDir)
        for (file in Files.list(scriptsDir)) allFiles.add(file)
        size = allFiles.size
    }

    /**
     * @return list of all files presented in [scriptsDir]
     */
    fun getAllFiles() = allFiles.toList()

    /**
     * Adds script with content of given string and saves it into given file.
     * @param content source code of the script
     * @param filename name of the file to be saved
     * @return true if file was saved, false otherwise
     */
    fun addScriptFromText(content: String, filename: String): Boolean {
        val new = File("${scriptsDir}/${filename}")
        return if (new.exists()) {
            new.writeText(content)
            size++
            allFiles.add(Path.of(new.path))
            true
        } else {
            false
        }
    }

    /**
     * Copies given file into [scriptsDir]
     * @param filename file to be copied
     * @return true if file was copied (if file to be copied existed), false otherwise
     */
    fun addScriptFromFile(filename: File): Boolean {
        return if (Files.exists(Path.of(filename.absolutePath))) {
            Files.copy(Path.of(filename.absolutePath), Path.of("${scriptsDir}/${filename.name}"))
            size++
            allFiles.add(Path.of("${scriptsDir}/${filename.name}"))
            true
        } else {
            false
        }
    }

    /**
     * Returns contents of given script
     * @param filename name of script to read
     * @return contents of the script
     */
    fun uploadScriptFromFile(filename: String): String {
        val pathToScript = Path.of("${scriptsDir}/${filename}")
        if (pathToScript.notExists()) return ""
        return Files.readString(pathToScript)
    }

    /**
     * Removes script from both [scriptsDir] and [allFiles]
     * @param filename name of script to remove
     * @return true if successfully removed, false otherwise
     */
    fun removeFileWithScript(filename: String): Boolean {
        val pathToScript = Path.of("${scriptsDir}/${filename}")
        return allFiles.removeIf { file -> file == pathToScript && Files.deleteIfExists(pathToScript) }
    }
}