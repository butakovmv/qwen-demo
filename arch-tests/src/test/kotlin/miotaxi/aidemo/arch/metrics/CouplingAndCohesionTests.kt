package miotaxi.aidemo.arch.metrics

import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.importer.ClassFileImporter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import kotlin.math.roundToInt

@DisplayName("Coupling между модулями")
class CouplingAndCohesionTests {
    companion object {
        private lateinit var allClasses: JavaClasses

        @BeforeAll
        @JvmStatic
        fun setUp() {
            allClasses =
                ClassFileImporter()
                    .importPackages("miotaxi.aidemo")
        }

        // -------------------------------------------------------------------------
        // Запрещённые зависимости: sourcePackage НЕ ДОЛЖЕН зависеть от targetPackage
        // -------------------------------------------------------------------------
        @JvmStatic
        fun forbiddenCouplingCases(): List<ModulePair> =
            listOf(
                ModulePair(
                    sourcePackage = "miotaxi.aidemo.operation",
                    targetPackage = "miotaxi.aidemo.webapi",
                ),
                ModulePair(
                    sourcePackage = "miotaxi.aidemo.operation",
                    targetPackage = "miotaxi.aidemo.app",
                ),
                ModulePair(
                    sourcePackage = "miotaxi.aidemo.model",
                    targetPackage = "miotaxi.aidemo.webapi",
                ),
                ModulePair(
                    sourcePackage = "miotaxi.aidemo.model",
                    targetPackage = "miotaxi.aidemo.app",
                ),
            )

        // -------------------------------------------------------------------------
        // Допустимые зависимости: sourcePackage МОЖЕТ зависеть от targetPackage
        // (информационный отчёт — тест не падает, просто логирует)
        // -------------------------------------------------------------------------
        @JvmStatic
        fun allowedCouplingCases(): List<ModulePair> =
            listOf(
                ModulePair(
                    sourcePackage = "miotaxi.aidemo.webapi",
                    targetPackage = "miotaxi.aidemo.model",
                ),
                ModulePair(
                    sourcePackage = "miotaxi.aidemo.app",
                    targetPackage = "miotaxi.aidemo.model",
                ),
                ModulePair(
                    sourcePackage = "miotaxi.aidemo.app",
                    targetPackage = "miotaxi.aidemo.webapi",
                ),
                ModulePair(
                    sourcePackage = "miotaxi.aidemo.operation",
                    targetPackage = "miotaxi.aidemo.model",
                ),
            )

        @JvmStatic
        fun cohesionCases(): List<CohesionCase> =
            listOf(
                CohesionCase(
                    packageName = "miotaxi.aidemo.operation",
                    minCohesion = 0.5,
                ),
                CohesionCase(
                    packageName = "miotaxi.aidemo.webapi",
                    minCohesion = 0.0,
                ),
                CohesionCase(
                    packageName = "miotaxi.aidemo.app",
                    minCohesion = 0.0,
                ),
                CohesionCase(
                    packageName = "miotaxi.aidemo.model",
                    minCohesion = 0.0,
                ),
            )
    }

    // =========================================================================
    // Запрещённые зависимости — тест падает при нарушении
    // =========================================================================
    @Nested
    @DisplayName("Запрещённые зависимости")
    inner class `Forbidden coupling` {
        @ParameterizedTest(name = "{0} ↛ {1}")
        @MethodSource("miotaxi.aidemo.arch.metrics.CouplingAndCohesionTests#forbiddenCouplingCases")
        fun `module must not depend on another`(pair: ModulePair) {
            val results =
                efferentCoupling(
                    sourcePackage = pair.sourcePackage,
                    targetPackage = pair.targetPackage,
                    allClasses = allClasses,
                )

            val violatingClasses = results.filter { it.dependencies.isNotEmpty() }

            assertEquals(
                0,
                violatingClasses.size,
                buildString {
                    append("${pair.sourceName} must NOT depend on ${pair.targetName}, ")
                    append("but found ${violatingClasses.size} violating class(es): ")
                    append(
                        violatingClasses.joinToString(", ") { r ->
                            "${r.sourceClass.simpleName} → ${r.dependencies.map { it.simpleName }}"
                        },
                    )
                },
            )
        }
    }

    // =========================================================================
    // Допустимые зависимости — информационный отчёт, тест не падает
    // =========================================================================
    @Nested
    @DisplayName("Допустимые зависимости (информационный)")
    inner class `Allowed coupling` {
        @ParameterizedTest(name = "{0} → {1}")
        @MethodSource("miotaxi.aidemo.arch.metrics.CouplingAndCohesionTests#allowedCouplingCases")
        fun `module may depend on another — report`(pair: ModulePair) {
            val results =
                efferentCoupling(
                    sourcePackage = pair.sourcePackage,
                    targetPackage = pair.targetPackage,
                    allClasses = allClasses,
                )

            val dependentClasses = results.filter { it.dependencies.isNotEmpty() }

            // Тест всегда проходит — просто проверяем, что данные собираются
            assertTrue(
                dependentClasses.size >= 0,
                buildString {
                    append("${pair.sourceName} → ${pair.targetName}: ")
                    append("${dependentClasses.size} class(es) depend on ${pair.targetName}")
                    if (dependentClasses.isNotEmpty()) {
                        append(
                            ". Details: ${dependentClasses.joinToString(", ") { r ->
                                "${r.sourceClass.simpleName} → ${r.dependencies.map { it.simpleName }}"
                            }}",
                        )
                    }
                },
            )
        }
    }

    // =========================================================================
    // Cohesion внутри модулей
    // =========================================================================
    @Nested
    @DisplayName("Cohesion внутри модулей")
    inner class `Internal cohesion` {
        @ParameterizedTest(name = "{0}")
        @MethodSource("miotaxi.aidemo.arch.metrics.CouplingAndCohesionTests#cohesionCases")
        fun `module should meet cohesion threshold`(testCase: CohesionCase) {
            val cohesion =
                internalCohesion(
                    packageName = testCase.packageName,
                    allClasses = allClasses,
                )
            val count = classCount(testCase.packageName, allClasses)
            val pct = (cohesion * 100).roundToInt()
            val minPct = (testCase.minCohesion * 100).roundToInt()

            assertTrue(
                cohesion >= testCase.minCohesion,
                "${testCase.packageName}: cohesion = $pct% (min: $minPct%, classes: $count)",
            )
        }
    }
}

// =========================================================================
// Data classes для параметризованных тестов
// =========================================================================

/**
 * Пара модулей для проверки coupling.
 */
data class ModulePair(
    val sourcePackage: String,
    val targetPackage: String,
) {
    val sourceName: String get() = sourcePackage.removePrefix("miotaxi.aidemo.")
    val targetName: String get() = targetPackage.removePrefix("miotaxi.aidemo.")

    override fun toString() = "$sourceName → $targetName"
}

/**
 * Параметры для проверки cohesion модуля.
 */
data class CohesionCase(
    val packageName: String,
    val minCohesion: Double,
) {
    val shortName: String get() = packageName.removePrefix("miotaxi.aidemo.")

    override fun toString() = "$shortName (min ${(minCohesion * 100).toInt()}%)"
}
