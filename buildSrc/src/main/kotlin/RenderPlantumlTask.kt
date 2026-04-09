import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import java.nio.file.Files

abstract class RenderPlantumlTask : DefaultTask() {

    init {
        group = "documentation"
        description = "Render all PlantUML files in docs/ to SVG using local PlantUML JAR"
    }

    @get:InputDirectory
    abstract val inputDir: DirectoryProperty

    @get:InputFile
    @get:Optional
    abstract val plantumlJar: RegularFileProperty

    @get:Input
    abstract val plantumlServer: Property<String>

    private val errorPatterns = listOf(
        "bad URL",
        "HUFFMAN",
        "DEFLATE",
        "The plugin you are using",
        "This URL does not look like"
    )

    @TaskAction
    fun render() {
        val inputDirPath = inputDir.get().asFile.toPath()
        val useLocal = plantumlJar.isPresent

        if (!Files.exists(inputDirPath)) {
            logger.warn("Input directory not found: $inputDirPath")
            return
        }

        val pumlFiles = Files.walk(inputDirPath)
            .filter { it.toString().endsWith(".puml") }
            .filter { pumlFile ->
                val svgPath = pumlFile.resolveSibling(
                    pumlFile.fileName.toString().replace(".puml", ".svg")
                )
                val needsRender = !Files.exists(svgPath) ||
                        Files.getLastModifiedTime(pumlFile) > Files.getLastModifiedTime(svgPath)
                if (!needsRender) {
                    logger.lifecycle("Skipping (up-to-date): $pumlFile")
                }
                needsRender
            }
            .sorted()
            .toList()

        if (pumlFiles.isEmpty()) {
            logger.lifecycle("No .puml files need rendering (all up-to-date)")
            return
        }

        logger.lifecycle("Found ${pumlFiles.size} PlantUML file(s) to render")

        var successCount = 0
        var failCount = 0

        for (pumlFile in pumlFiles) {
            val svgPath = pumlFile.resolveSibling(
                pumlFile.fileName.toString().replace(".puml", ".svg")
            )

            try {
                if (useLocal) {
                    renderLocal(pumlFile, svgPath)
                } else {
                    renderRemote(pumlFile, svgPath)
                }
                logger.lifecycle("  -> OK")
                successCount++
            } catch (e: Exception) {
                logger.error("  -> Failed: ${e.message}")
                failCount++
            }
        }

        logger.lifecycle("")
        logger.lifecycle("Rendered: $successCount success, $failCount failed")

        if (failCount > 0) {
            throw RuntimeException("$failCount PlantUML file(s) failed to render")
        }
    }

    private fun renderLocal(pumlFile: java.nio.file.Path, svgPath: java.nio.file.Path) {
        val jarPath = plantumlJar.get().asFile.absolutePath
        val outputDir = svgPath.parent.toString()

        logger.lifecycle("Rendering (local): $pumlFile")

        val process = ProcessBuilder(
            "java", "-jar", jarPath, "-tsvg", "-o", outputDir, pumlFile.toString()
        ).redirectErrorStream(true).start()

        val output = process.inputStream.bufferedReader().readText()
        val exitCode = process.waitFor()

        if (exitCode != 0) {
            throw RuntimeException("PlantUML exited with code $exitCode: $output")
        }

        // Validate output SVG
        if (!Files.exists(svgPath)) {
            throw RuntimeException("SVG file not created: $svgPath")
        }

        val svgContent = Files.readString(svgPath)
        if (!svgContent.contains("<svg")) {
            throw RuntimeException("Output is not valid SVG")
        }

        val svgErrors = errorPatterns.filter { svgContent.contains(it) }
        if (svgErrors.isNotEmpty()) {
            throw RuntimeException("SVG contains errors: ${svgErrors.joinToString(", ")}")
        }
    }

    private fun renderRemote(pumlFile: java.nio.file.Path, svgPath: java.nio.file.Path) {
        val server = plantumlServer.get()
        val content = Files.readString(pumlFile)
        val encoded = encodePlantuml(content)

        logger.lifecycle("Rendering (remote): $pumlFile")
        logger.lifecycle("  Encoded: $server/svg/~1${encoded.take(40)}...")

        val client = java.net.http.HttpClient.newBuilder()
            .followRedirects(java.net.http.HttpClient.Redirect.NORMAL)
            .connectTimeout(java.time.Duration.ofSeconds(30))
            .build()

        val request = java.net.http.HttpRequest.newBuilder()
            .uri(java.net.URI.create("$server/svg/~1$encoded"))
            .timeout(java.time.Duration.ofSeconds(60))
            .GET()
            .build()

        val response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofByteArray())

        if (response.statusCode() != 200 || response.body().isEmpty()) {
            throw RuntimeException("HTTP ${response.statusCode()}")
        }

        val body = String(response.body(), Charsets.UTF_8)
        val svgErrors = errorPatterns.filter { body.contains(it) }
        if (svgErrors.isNotEmpty()) {
            throw RuntimeException("SVG contains errors: ${svgErrors.joinToString(", ")}")
        }

        if (!body.contains("<svg")) {
            throw RuntimeException("Response is not valid SVG")
        }

        Files.write(svgPath, response.body())
    }

    private val alphabet = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz-_"

    private fun encodePlantuml(text: String): String {
        val rawBytes = text.toByteArray(Charsets.UTF_8)
        val compressed = java.io.ByteArrayOutputStream()

        val deflater = java.util.zip.Deflater(java.util.zip.Deflater.BEST_COMPRESSION, true)
        deflater.setInput(rawBytes)
        deflater.finish()

        val buf = ByteArray(1024)
        while (!deflater.finished()) {
            val len = deflater.deflate(buf)
            if (len > 0) compressed.write(buf, 0, len)
        }
        deflater.end()

        return encodePlantumlBytes(compressed.toByteArray())
    }

    private fun encodePlantumlBytes(data: ByteArray): String {
        val sb = StringBuilder()
        var i = 0
        while (i < data.size) {
            val b1 = data[i].toInt() and 0xFF
            val b2 = if (i + 1 < data.size) data[i + 1].toInt() and 0xFF else 0
            val b3 = if (i + 2 < data.size) data[i + 2].toInt() and 0xFF else 0

            val d = b1 or (b2 shl 8) or (b3 shl 16)
            val w1 = d and 0x3F
            val w2 = (d shr 6) and 0x3F
            val w3 = (d shr 12) and 0x3F
            val w4 = (d shr 18) and 0x3F

            sb.append(alphabet[w1])
            sb.append(alphabet[w2])
            sb.append(alphabet[w3])
            sb.append(alphabet[w4])

            i += 3
        }
        return sb.toString()
    }
}
