package miotaxi.aidemo.arch.metrics

import com.tngtech.archunit.core.domain.JavaClass
import com.tngtech.archunit.core.domain.JavaClasses

/**
 * Efferent Coupling (Ce) — количество внешних классов,
 * от которых зависят классы данного пакета.
 *
 * Возвращает список пар: (класс, множество классов из других модулей, от которых он зависит).
 */
fun efferentCoupling(
    sourcePackage: String,
    targetPackage: String,
    allClasses: JavaClasses,
): List<EfferentCouplingResult> {
    val sourceClasses = allClasses.filter { it.belongsToModule(sourcePackage) }
    val targetClasses = allClasses.filter { it.belongsToModule(targetPackage) }.toSet()

    return sourceClasses.map { javaClass ->
        val deps =
            javaClass.directDependenciesFromSelf
                .mapNotNull { it.targetClass }
                .filter { it in targetClasses }
        EfferentCouplingResult(javaClass, deps)
    }
}

data class EfferentCouplingResult(
    val sourceClass: JavaClass,
    val dependencies: List<JavaClass>,
)

/**
 * Afferent Coupling (Ca) — количество классов из других модулей,
 * которые зависят от классов данного пакета.
 */
fun afferentCoupling(
    targetPackage: String,
    sourcePackage: String,
    allClasses: JavaClasses,
): List<AfferentCouplingResult> {
    val targetClasses = allClasses.filter { it.belongsToModule(targetPackage) }.toSet()
    val sourceClasses = allClasses.filter { it.belongsToModule(sourcePackage) }

    return sourceClasses.mapNotNull { sourceClass ->
        val depsOnTarget =
            sourceClass.directDependenciesFromSelf
                .mapNotNull { it.targetClass }
                .filter { it in targetClasses }

        if (depsOnTarget.isNotEmpty()) {
            AfferentCouplingResult(sourceClass, depsOnTarget)
        } else {
            null
        }
    }
}

data class AfferentCouplingResult(
    val dependentClass: JavaClass,
    val dependencies: List<JavaClass>,
)

/**
 * Cohesion — доля классов в пакете, которые имеют зависимости
 * на другие классы того же пакета.
 *
 * Значение от 0.0 (полная разрозненность) до 1.0 (полная связность).
 */
fun internalCohesion(
    packageName: String,
    allClasses: JavaClasses,
): Double {
    val classes = allClasses.filter { it.belongsToModule(packageName) }
    if (classes.size <= 1) return 1.0

    val classesWithInternalDeps =
        classes.count { javaClass ->
            javaClass.directDependenciesFromSelf.any { dep ->
                dep.targetClass != null && dep.targetClass in classes
            }
        }
    return classesWithInternalDeps.toDouble() / classes.size
}

/**
 * Количество классов в пакете.
 */
fun classCount(
    packageName: String,
    allClasses: JavaClasses,
): Int = allClasses.count { it.belongsToModule(packageName) }

private fun JavaClass.belongsToModule(packageName: String): Boolean =
    this.packageName.startsWith(packageName) || this.packageName == packageName
