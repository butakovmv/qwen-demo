import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

data class TestCase(
    val module: String,
    val className: String,
    val name: String,
    val time: String,
    val status: String = "passed"
)

private data class ModuleStat(
    val total: Int, val passed: Int, val failed: Int, val skipped: Int,
    val time: Double, val rate: String, val rateClass: String
)

abstract class GenerateTestReportTask : DefaultTask() {
    @get:InputFiles
    abstract val backendTestResultsDirs: ConfigurableFileCollection

    @get:Optional
    @get:InputDirectory
    abstract val frontTestResultsDir: DirectoryProperty

    @get:OutputFile
    abstract val outputHtml: RegularFileProperty

    @TaskAction
    fun execute() {
        val allTestCases = mutableListOf<TestCase>()

        // Backend: ищем XML в каждой директории
        backendTestResultsDirs.forEach { dir ->
            if (dir.isDirectory) {
                dir.walkTopDown()
                    .filter { it.extension == "xml" }
                    .forEach { xmlFile -> parseXml(xmlFile, allTestCases) }
            }
        }

        // Frontend
        if (frontTestResultsDir.isPresent) {
            val frontDir = frontTestResultsDir.get().asFile
            if (frontDir.exists()) {
                frontDir.walkTopDown()
                    .filter { it.extension == "xml" }
                    .forEach { xmlFile -> parseXml(xmlFile, allTestCases, forceModule = "front") }
            }
        }

        val total = allTestCases.size
        val passed = allTestCases.count { it.status == "passed" }
        val failed = allTestCases.count { it.status == "failed" || it.status == "error" }
        val skipped = allTestCases.count { it.status == "skipped" }
        val totalTime = allTestCases.sumOf { it.time.toDoubleOrNull() ?: 0.0 }
        val passRate = if (total > 0) String.format("%.1f", passed.toDouble() / total * 100) else "0.0"
        val totalRateClass = if (failed == 0) "high" else if (passed.toDouble() / (total + 0.0001) * 100 >= 50) "mid" else "low"

        val modules = allTestCases.groupBy { it.module }.toSortedMap()
        val moduleStats = modules.mapValues { (_, tests) ->
            val mTotal = tests.size
            val mPassed = tests.count { it.status == "passed" }
            val mFailed = tests.count { it.status == "failed" || it.status == "error" }
            val mSkipped = tests.count { it.status == "skipped" }
            val mTime = tests.sumOf { it.time.toDoubleOrNull() ?: 0.0 }
            val mRate = if (mTotal > 0) String.format("%.1f", mPassed.toDouble() / mTotal * 100) else "0.0"
            val mRateClass = if (mFailed == 0) "high" else if (mPassed.toDouble() / (mTotal + 0.0001) * 100 >= 50) "mid" else "low"
            ModuleStat(mTotal, mPassed, mFailed, mSkipped, mTime, mRate, mRateClass)
        }

        val rows = allTestCases.joinToString("\n") { tc ->
            val sc = when (tc.status) { "passed" -> "passed"; "failed", "error" -> "failed"; else -> "skipped" }
            val st = when (tc.status) { "passed" -> "✅ PASSED"; "failed" -> "❌ FAILED"; "error" -> "❌ ERROR"; "skipped" -> "⏭ SKIPPED"; else -> tc.status }
            """<tr class="$sc"><td>${tc.module}</td><td>${tc.className}</td><td>${tc.name}</td><td class="status $sc">$st</td></tr>"""
        }

        val moduleCards = listOf("Суммарно" to ModuleStat(total, passed, failed, skipped, totalTime, passRate, totalRateClass)) +
            moduleStats.entries.map { it.key to it.value }
        val cardsHtml = moduleCards.joinToString("\n") { (name, stat) ->
            val timeStr = String.format("%.3f", stat.time)
            """<div class="module-card ${stat.rateClass}">
                <div class="module-name">$name</div>
                <div class="module-details">
                    <div class="detail">📋 <span>${stat.total}</span></div>
                    <div class="detail">✅ <span>${stat.passed}</span></div>
                    <div class="detail">❌ <span>${stat.failed}</span></div>
                    <div class="detail">⏭️ <span>${stat.skipped}</span></div>
                    <div class="detail">⏱️ <span>${timeStr}s</span></div>
                </div>
                <div class="pass-rate">${stat.rate}% pass rate</div>
            </div>"""
        }

        val outputFile = outputHtml.get().asFile
        outputFile.parentFile.mkdirs()
        outputFile.writeText(buildHtml(cardsHtml, rows))
        println("Aggregated test report: ${outputFile.absolutePath} ($total tests, $passed passed, $failed failed, $skipped skipped)")
    }

    private fun parseXml(xmlFile: java.io.File, cases: MutableList<TestCase>, forceModule: String? = null) {
        val moduleName = forceModule ?: extractModuleName(xmlFile)
        val text = xmlFile.readText()

        Regex("""<testcase\s+([^>]*)/>""")
            .findAll(text)
            .forEach { match ->
                val attrs = match.groupValues[1]
                cases.add(TestCase(moduleName, attr(attrs, "classname"), attr(attrs, "name"), attr(attrs, "time")))
            }

        Regex("""<testcase\s+([^>]*)>(.*?)</testcase>""", RegexOption.DOT_MATCHES_ALL)
            .findAll(text)
            .forEach { match ->
                val attrs = match.groupValues[1]
                val body = match.groupValues[2]
                val status = when {
                    body.contains("<failure") -> "failed"
                    body.contains("<skipped") -> "skipped"
                    body.contains("<error") -> "error"
                    else -> "passed"
                }
                cases.add(TestCase(moduleName, attr(attrs, "classname"), attr(attrs, "name"), attr(attrs, "time"), status))
            }
    }

    private fun attr(attrs: String, name: String): String =
        Regex("""$name="([^"]+)""").find(attrs)?.groupValues?.get(1) ?: if (name == "time") "0" else "?"

    private fun buildHtml(cardsHtml: String, rows: String): String = """
<!DOCTYPE html>
<html lang="ru">
<head>
<meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Test Report — Aggregated</title>
<style>
*{box-sizing:border-box;margin:0;padding:0}
body{font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif;background:#f0f2f5;color:#333;padding:24px}
.container{max-width:1200px;margin:0 auto;background:#fff;border-radius:12px;box-shadow:0 2px 12px rgba(0,0,0,.08);overflow:hidden}
.header{padding:24px 32px;border-bottom:1px solid #e8e8e8}
.header h1{font-size:1.5rem;margin-bottom:0}
.module-stats{padding:16px 32px;border-bottom:1px solid #e8e8e8}
.module-grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(220px,1fr));gap:12px}
.module-card{padding:14px 18px;border-radius:8px;background:#f8f9fa;border:1px solid #e8e8e8}
.module-card .module-name{font-size:.95rem;font-weight:600;color:#333;margin-bottom:8px}
.module-card .module-details{display:flex;gap:12px;flex-wrap:wrap}
.module-card .detail{font-size:.8rem;color:#555}
.module-card .detail span{font-weight:600}
.module-card .pass-rate{margin-top:6px;font-size:.85rem;font-weight:600;color:#333}
.module-card.high{background:#e8f5e9;border-color:#a5d6a7}
.module-card.mid{background:#fff3e0;border-color:#ffcc80}
.module-card.low{background:#ffebee;border-color:#ef9a9a}
.filter-bar{padding:16px 32px;background:#fafafa;border-bottom:1px solid #e8e8e8;display:flex;gap:12px;flex-wrap:wrap;align-items:center}
.filter-bar button{padding:6px 16px;border:1px solid #ddd;border-radius:6px;background:#fff;cursor:pointer;font-size:.85rem;transition:all .15s}
.filter-bar button:hover{background:#e8e8e8}
.filter-bar button.active{background:#1565c0;color:#fff;border-color:#1565c0}
.filter-bar input{padding:6px 12px;border:1px solid #ddd;border-radius:6px;font-size:.85rem;width:250px}
table{width:100%;border-collapse:collapse}
th{background:#f5f5f5;padding:10px 16px;text-align:left;font-size:.8rem;text-transform:uppercase;color:#666;position:sticky;top:0}
td{padding:10px 16px;border-top:1px solid #f0f0f0;font-size:.9rem}
tr.passed td{background:#fff}
tr.failed td{background:#fff5f5}
tr.skipped td{background:#fffbf0}
tr:hover td{background:#f5f8ff}
td:first-child{font-weight:500;color:#555}
.status{font-weight:600;font-size:.8rem;white-space:nowrap}
.status.passed{color:#2e7d32}
.status.failed{color:#c62828}
.status.skipped{color:#e65100}
</style>
</head>
<body>
<div class="container">
<div class="header"><h1>Test Report — Aggregated</h1></div>
<div class="module-stats"><div class="module-grid">
$cardsHtml
</div></div>
<div class="filter-bar">
<button class="active" onclick="filter('all',this)">All</button>
<button onclick="filter('passed',this)">Passed</button>
<button onclick="filter('failed',this)">Failed</button>
<button onclick="filter('skipped',this)">Skipped</button>
<input type="text" id="search" placeholder="Search test name..." oninput="searchTests(this.value)">
</div>
<table>
<thead><tr><th>Module</th><th>Class</th><th>Test</th><th>Status</th></tr></thead>
<tbody id="testBody">
$rows
</tbody>
</table>
</div>
<script>
function filter(s,b){document.querySelectorAll('.filter-bar button').forEach(x=>x.classList.remove('active'));b.classList.add('active');document.querySelectorAll('#testBody tr').forEach(r=>{r.style.display=(s==='all'||r.classList.contains(s))?'':'none'})}
function searchTests(q){q=q.toLowerCase();document.querySelectorAll('#testBody tr').forEach(r=>{r.style.display=r.textContent.toLowerCase().includes(q)?'':'none'})}
</script>
</body>
</html>""".trimIndent()

    private fun extractModuleName(xmlFile: java.io.File): String {
        val parts = xmlFile.absolutePath.split("/")
        val buildIdx = parts.indexOfLast { it == "build" }
        return if (buildIdx > 0) parts[buildIdx - 1] else "unknown"
    }
}
