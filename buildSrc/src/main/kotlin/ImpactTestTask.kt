package impact

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import java.io.ByteArrayOutputStream

/**
 * Gradle-задача для анализа изменённых файлов (git diff) и определения
 * минимально необходимого набора тестов для проверки этих изменений.
 *
 * Конфигурация в config/impact-rules.json — формат:
 *   {
 *     ":test": {
 *       "description": "All tests",
 *       "patterns": ["^buildSrc/", "/build\\.gradle\\.kts$"]
 *     },
 *     ":operation:test": {
 *       "description": "Operation unit tests",
 *       "patterns": ["^operation/"]
 *     }
 *   }
 *
 * Примеры использования:
 *   ./gradlew impactTest                          — сравнивает с HEAD (незакоммиченные изменения)
 *   ./gradlew impactTest --base=origin/main       — сравнивает с main
 *   ./gradlew impactTest --base=HEAD~3            — сравнивает с 3 коммита назад
 */
abstract class ImpactTestTask : DefaultTask() {

    private var baseRef: String? = null
    private var stagedOnly = false

    init {
        group = "verification"
        description = "Run only tests affected by changed files (git diff-based impact analysis)"
    }

    @get:InputFile
    abstract val rulesFile: org.gradle.api.file.RegularFileProperty

    @Option(option = "base", description = "Git ref to diff against (default: auto-detect origin/main or HEAD)")
    fun setBaseRef(ref: String) {
        this.baseRef = ref
    }

    @Option(option = "staged", description = "Only analyze staged (cached) files for pre-commit hooks")
    fun setStagedOnly(value: Boolean) {
        this.stagedOnly = value
    }

    @TaskAction
    fun execute() {
        val rules = loadRules()
        val base = baseRef ?: detectBaseRef()

        logger.lifecycle("=== Impact Analysis ===")
        logger.lifecycle("Comparing against: $base")
        logger.lifecycle("Rules file: ${rulesFile.get().asFile.absolutePath}")
        logger.lifecycle("")

        val changedFiles = getChangedFiles(base)

        if (changedFiles.isEmpty()) {
            logger.lifecycle("No changed files detected.")
            return
        }

        logger.lifecycle("Changed files (${changedFiles.size}):")
        changedFiles.forEach { logger.lifecycle("  $it") }
        logger.lifecycle("")

        val affectedTests = mutableMapOf<String, MutableSet<String>>()

        for ((testPath, testRule) in rules) {
            for (file in changedFiles) {
                for (pattern in testRule.patterns) {
                    if (pattern.containsMatchIn(file)) {
                        affectedTests.getOrPut(testPath) { mutableSetOf() }.add(file)
                        break // один файл уже триггернул этот тест — дальше не проверяем этот файл для этого теста
                    }
                }
            }
        }

        logger.lifecycle("=== Impact Summary ===")
        for ((testPath, triggeringFiles) in affectedTests.entries.sortedBy { it.key }) {
            val description = rules[testPath]!!.description
            logger.lifecycle("  $testPath ($description)")
            for (file in triggeringFiles.sorted()) {
                logger.lifecycle("    ← $file")
            }
        }
        logger.lifecycle("")

        if (affectedTests.isEmpty()) {
            logger.lifecycle("No tests to run based on changed files.")
            return
        }

        logger.lifecycle("Tests to run (${affectedTests.size}):")
        affectedTests.keys.sorted().forEach { logger.lifecycle("  $it") }
        logger.lifecycle("")
        logger.lifecycle("To run the affected tests:")
        affectedTests.keys.sorted().forEach { logger.lifecycle("  ./gradlew $it") }
    }

    private fun loadRules(): Map<String, TestRule> {
        val file = rulesFile.get().asFile
        if (!file.exists()) {
            throw RuntimeException("Impact rules file not found: ${file.absolutePath}")
        }

        val json = groovy.json.JsonSlurper().parseText(file.readText()) as Map<*, *>

        return json.mapValues { (_, value) ->
            val rule = value as Map<*, *>
            val patternStrings = (rule["patterns"] as List<*>).map { it as String }
            TestRule(
                description = rule["description"] as String,
                patterns = patternStrings.map { Regex(it, RegexOption.IGNORE_CASE) }
            )
        }.mapKeys { (key, _) -> key as String }
    }

    private fun detectBaseRef(): String {
        return try {
            val result = runGit("rev-parse", "--verify", "origin/main")
            if (result.exitCode == 0) {
                "origin/main"
            } else {
                "HEAD"
            }
        } catch (_: Exception) {
            "HEAD"
        }
    }

    private fun getChangedFiles(base: String): List<String> {
        return try {
            if (stagedOnly) {
                // Только staged-файлы (для pre-commit hook)
                val result = runGit("diff", "--cached", "--name-only", "--diff-filter=ACMR")
                if (result.exitCode == 0 && result.output.isNotBlank()) {
                    result.output.lines().filter { it.isNotBlank() }
                } else {
                    emptyList()
                }
            } else {
                val result = runGit("diff", "--name-only", "--diff-filter=ACMR", base)
                if (result.exitCode == 0 && result.output.isNotBlank()) {
                    result.output.lines().filter { it.isNotBlank() }
                } else {
                    val unstaged = runGit("diff", "--name-only", "--diff-filter=ACMR")
                    val staged = runGit("diff", "--name-only", "--diff-filter=ACMR", "--cached")
                    (unstaged.output.lines() + staged.output.lines())
                        .filter { it.isNotBlank() }
                        .distinct()
                }
            }
        } catch (e: Exception) {
            logger.warn("Git diff failed: ${e.message}")
            emptyList()
        }
    }

    private fun runGit(vararg args: String): GitResult {
        val process = ProcessBuilder(buildList {
            add("git")
            addAll(args)
        }).redirectErrorStream(true).start()

        val baos = ByteArrayOutputStream()
        process.inputStream.copyTo(baos)
        val exitCode = process.waitFor()

        return GitResult(
            output = baos.toString("UTF-8").trim(),
            exitCode = exitCode
        )
    }

    private data class GitResult(val output: String, val exitCode: Int)

    private data class TestRule(
        val description: String,
        val patterns: List<Regex>
    )
}
