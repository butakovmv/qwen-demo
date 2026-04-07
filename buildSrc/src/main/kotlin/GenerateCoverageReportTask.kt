import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.xml.sax.InputSource
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory

data class CoverageFile(
    val filePath: String,
    val linesTotal: Int,
    val linesCovered: Int,
    val branchesTotal: Int,
    val branchesCovered: Int
)

data class CoverageModule(
    val name: String,
    val files: List<CoverageFile>
)

abstract class GenerateCoverageReportTask : DefaultTask() {
    @get:InputFiles
    abstract val jacocoXmlFiles: ConfigurableFileCollection

    @get:InputFiles
    abstract val lcovFiles: ConfigurableFileCollection

    @get:OutputFile
    abstract val outputHtml: RegularFileProperty

    @TaskAction
    fun execute() {
        val allModules = mutableListOf<CoverageModule>()

        // --- Backend: парсим JaCoCo XML ---
        jacocoXmlFiles.forEach { xmlFile ->
            if (xmlFile.exists()) {
                val moduleName = extractModuleName(xmlFile)
                val module = parseJacocoXml(xmlFile, moduleName)
                allModules.add(module)
            }
        }

        // --- Frontend: парсим LCOV ---
        lcovFiles.forEach { lcovFile ->
            if (lcovFile.exists()) {
                val module = parseLcov(lcovFile)
                allModules.add(module)
            }
        }

        val allFiles = allModules.flatMap { it.files }
        val totalLines = allFiles.sumOf { it.linesTotal }
        val coveredLines = allFiles.sumOf { it.linesCovered }
        val totalBranches = allFiles.sumOf { it.branchesTotal }
        val coveredBranches = allFiles.sumOf { it.branchesCovered }
        val lineRate = if (totalLines > 0) String.format(java.util.Locale.US, "%.1f", coveredLines.toDouble() / totalLines * 100) else "0.0"
        val branchRate = if (totalBranches > 0) String.format(java.util.Locale.US, "%.1f", coveredBranches.toDouble() / totalBranches * 100) else "N/A"

        val moduleStats = allModules.map { mod ->
            val mFiles = mod.files
            val mLines = mFiles.sumOf { it.linesTotal }
            val mCovered = mFiles.sumOf { it.linesCovered }
            val mBranches = mFiles.sumOf { it.branchesTotal }
            val mCoveredBranches = mFiles.sumOf { it.branchesCovered }
            val mRate = if (mLines > 0) String.format(java.util.Locale.US, "%.1f", mCovered.toDouble() / mLines * 100) else "0.0"
            val mBranchRate = if (mBranches > 0) String.format(java.util.Locale.US, "%.1f", mCoveredBranches.toDouble() / mBranches * 100) else "N/A"
            val rateNum = mRate.toDoubleOrNull() ?: 0.0
            val rateClass = if (rateNum == 100.0) "high" else if (rateNum >= 50) "mid" else "low"
            ModuleCoverageStat(mod.name, mFiles.size, mLines, mCovered, mBranches, mCoveredBranches, mRate, mBranchRate, rateClass)
        }.sortedByDescending { it.coveredLines }

        val cards = listOf(
            "Суммарно" to ModuleCoverageStat(
                "Суммарно",
                allFiles.size,
                totalLines,
                coveredLines,
                totalBranches,
                coveredBranches,
                lineRate,
                branchRate,
                if ((lineRate.toDoubleOrNull() ?: 0.0) == 100.0) "high" else if ((lineRate.toDoubleOrNull() ?: 0.0) >= 50) "mid" else "low"
            )
        ) + moduleStats.map { it.name to it }

        val cardsHtml = cards.joinToString("\n") { (name, stat) ->
            """<div class="module-card ${stat.rateClass}">
                <div class="module-name">$name</div>
                <div class="module-details">
                    <div class="detail">📄 <span>${stat.fileCount}</span> files</div>
                    <div class="detail">📏 <span>${stat.coveredLines}/${stat.totalLines}</span> lines</div>
                    <div class="detail">🔀 <span>${stat.coveredBranches}/${stat.totalBranches}</span> branches</div>
                </div>
                <div class="pass-rate">Line: ${stat.lineRate}% | Branch: ${stat.branchRate}%</div>
            </div>"""
        }

        val fileRows = allModules.flatMap { mod ->
            mod.files.map { f ->
                val fRate = if (f.linesTotal > 0) String.format(java.util.Locale.US, "%.1f", f.linesCovered.toDouble() / f.linesTotal * 100) else "0.0"
                val rateNum = fRate.toDoubleOrNull() ?: 0.0
                val sc = if (rateNum >= 50) "passed" else "failed"
                """<tr class="$sc"><td>${mod.name}</td><td>${f.filePath}</td><td class="num">${f.linesCovered}/${f.linesTotal}</td><td class="status $sc">$fRate%</td></tr>"""
            }
        }.joinToString("\n")

        val outputFile = outputHtml.get().asFile
        outputFile.parentFile.mkdirs()
        outputFile.writeText(buildHtml(cardsHtml, fileRows, lineRate, branchRate, totalLines, coveredLines, totalBranches, coveredBranches, allFiles.size))
        println("Coverage report: ${outputFile.absolutePath} ($totalLines lines, $coveredLines covered, ${lineRate}%)")
    }

    private fun parseJacocoXml(xmlFile: java.io.File, moduleName: String): CoverageModule {
        val factory = DocumentBuilderFactory.newInstance().apply {
            isNamespaceAware = false
            setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
        }
        val builder = factory.newDocumentBuilder()
        val doc = builder.parse(xmlFile)

        val files = mutableListOf<CoverageFile>()
        val packageNodes = doc.getElementsByTagName("package")
        for (i in 0 until packageNodes.length) {
            val pkg = packageNodes.item(i) as org.w3c.dom.Element
            val pkgName = pkg.getAttribute("name")
            val sourceNodes = pkg.getElementsByTagName("sourcefile")
            for (j in 0 until sourceNodes.length) {
                val src = sourceNodes.item(j) as org.w3c.dom.Element
                val srcName = src.getAttribute("name")
                var linesTotal = 0
                var linesCovered = 0
                var branchesTotal = 0
                var branchesCovered = 0

                val counters = src.getElementsByTagName("counter")
                for (k in 0 until counters.length) {
                    val counter = counters.item(k) as org.w3c.dom.Element
                    val type = counter.getAttribute("type")
                    val missed = counter.getAttribute("missed").toIntOrNull() ?: 0
                    val covered = counter.getAttribute("covered").toIntOrNull() ?: 0
                    when (type) {
                        "LINE" -> {
                            linesTotal = missed + covered
                            linesCovered = covered
                        }
                        "BRANCH" -> {
                            branchesTotal = missed + covered
                            branchesCovered = covered
                        }
                    }
                }
                if (linesTotal > 0) {
                    val filePath = if (pkgName == ".") srcName else "$pkgName/$srcName"
                    files.add(CoverageFile(filePath, linesTotal, linesCovered, branchesTotal, branchesCovered))
                }
            }
        }
        return CoverageModule(moduleName, files)
    }

    private fun parseLcov(lcovFile: java.io.File): CoverageModule {
        val files = mutableListOf<CoverageFile>()
        var currentFile: String? = null
        var linesTotal = 0
        var linesCovered = 0
        var branchesTotal = 0
        var branchesCovered = 0

        lcovFile.readLines().forEach { line ->
            when {
                line.startsWith("SF:") -> {
                    currentFile = line.substringAfter("SF:")
                    linesTotal = 0
                    linesCovered = 0
                    branchesTotal = 0
                    branchesCovered = 0
                }
                line.startsWith("DA:") -> {
                    val parts = line.substringAfter("DA:").split(",")
                    linesTotal++
                    if (parts.getOrNull(1)?.toIntOrNull() ?: 0 > 0) linesCovered++
                }
                line.startsWith("BRDA:") -> {
                    branchesTotal++
                    val hit = line.substringAfter("BRDA:").split(",").getOrNull(3)?.toIntOrNull() ?: 0
                    if (hit > 0) branchesCovered++
                }
                line.startsWith("LF:") -> {
                    linesTotal = line.substringAfter("LF:").toIntOrNull() ?: linesTotal
                }
                line.startsWith("LH:") -> {
                    linesCovered = line.substringAfter("LH:").toIntOrNull() ?: linesCovered
                }
                line.startsWith("BRF:") -> {
                    branchesTotal = line.substringAfter("BRF:").toIntOrNull() ?: branchesTotal
                }
                line.startsWith("BRH:") -> {
                    branchesCovered = line.substringAfter("BRH:").toIntOrNull() ?: branchesCovered
                }
                line == "end_of_record" && currentFile != null -> {
                    files.add(CoverageFile(currentFile!!, linesTotal, linesCovered, branchesTotal, branchesCovered))
                    currentFile = null
                }
            }
        }
        return CoverageModule("front", files)
    }

    private fun extractModuleName(xmlFile: java.io.File): String {
        val parts = xmlFile.absolutePath.split("/")
        val buildIdx = parts.indexOfLast { it == "build" }
        return if (buildIdx > 0) parts[buildIdx - 1] else "unknown"
    }

    private fun buildHtml(
        cardsHtml: String, fileRows: String,
        lineRate: String, branchRate: String,
        totalLines: Int, coveredLines: Int, totalBranches: Int, coveredBranches: Int,
        fileCount: Int
    ): String = """
<!DOCTYPE html>
<html lang="ru">
<head>
<meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Coverage Report — Aggregated</title>
<style>
*{box-sizing:border-box;margin:0;padding:0}
body{font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif;background:#f0f2f5;color:#333;padding:24px}
.container{max-width:1200px;margin:0 auto;background:#fff;border-radius:12px;box-shadow:0 2px 12px rgba(0,0,0,.08);overflow:hidden}
.header{padding:24px 32px;border-bottom:1px solid #e8e8e8}
.header h1{font-size:1.5rem;margin-bottom:0}
.module-stats{padding:16px 32px;border-bottom:1px solid #e8e8e8}
.module-grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(260px,1fr));gap:12px}
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
tr:hover td{background:#f5f8ff}
td:first-child{font-weight:500;color:#555}
td.num{white-space:nowrap}
.status{font-weight:600;font-size:.8rem;white-space:nowrap}
.status.passed{color:#2e7d32}
.status.failed{color:#c62828}
</style>
</head>
<body>
<div class="container">
<div class="header"><h1>Coverage Report — Aggregated</h1></div>
<div class="module-stats"><div class="module-grid">
$cardsHtml
</div></div>
<div class="filter-bar">
<button class="active" onclick="filter('all',this)">All</button>
<button onclick="filter('passed',this)">≥50%</button>
<button onclick="filter('failed',this)"><50%</button>
<input type="text" id="search" placeholder="Search file..." oninput="searchFiles(this.value)">
</div>
<table>
<thead><tr><th>Module</th><th>File</th><th>Lines</th><th>Coverage</th></tr></thead>
<tbody id="fileBody">
$fileRows
</tbody>
</table>
</div>
<script>
function filter(s,b){document.querySelectorAll('.filter-bar button').forEach(x=>x.classList.remove('active'));b.classList.add('active');document.querySelectorAll('#fileBody tr').forEach(r=>{r.style.display=(s==='all'||r.classList.contains(s))?'':'none'})}
function searchFiles(q){q=q.toLowerCase();document.querySelectorAll('#fileBody tr').forEach(r=>{r.style.display=r.textContent.toLowerCase().includes(q)?'':'none'})}
</script>
</body>
</html>""".trimIndent()

    private data class ModuleCoverageStat(
        val name: String,
        val fileCount: Int,
        val totalLines: Int,
        val coveredLines: Int,
        val totalBranches: Int,
        val coveredBranches: Int,
        val lineRate: String,
        val branchRate: String,
        val rateClass: String
    )
}
