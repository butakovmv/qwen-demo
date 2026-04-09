package miotaxi.aidemo.arch

import com.tngtech.archunit.core.domain.JavaClass
import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.importer.ClassFileImporter
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Repository
import org.springframework.web.bind.annotation.RestController
import kotlin.reflect.KVisibility

class VisibilityTests {
    companion object {
        private lateinit var allClasses: JavaClasses

        @BeforeAll
        @JvmStatic
        fun setUp() {
            allClasses =
                ClassFileImporter()
                    .importPackages("miotaxi.aidemo")
        }

        private fun JavaClass.kotlinVisibility(): KVisibility? =
            runCatching {
                val kClass = Class.forName(name).kotlin
                kClass.visibility
            }.getOrNull()

        private fun JavaClass.hasAnnotation(annotationClass: Class<out Annotation>): Boolean =
            runCatching {
                Class.forName(name).getAnnotation(annotationClass) != null
            }.getOrElse { false }

        private fun JavaClass.isRestController(): Boolean = hasAnnotation(RestController::class.java)

        private fun JavaClass.isConfiguration(): Boolean = hasAnnotation(Configuration::class.java)

        private fun JavaClass.isRepository(): Boolean = hasAnnotation(Repository::class.java)

        private fun JavaClass.isUseCaseImpl(): Boolean =
            simpleName.endsWith("UseCaseImpl") ||
                (simpleName.endsWith("Impl") && packageName.contains(".impl"))

        private fun JavaClass.isModelClass(): Boolean = packageName.contains(".model") && !isInterface

        private fun JavaClass.isUseCaseInterface(): Boolean = isInterface && simpleName.endsWith("UseCase")
    }

    @Test
    fun `usecase implementations should not be public`() {
        val violations =
            allClasses
                .filter { it.isUseCaseImpl() }
                .filter { it.kotlinVisibility() == KVisibility.PUBLIC }
                .map { it.simpleName }

        assertTrue(
            violations.isEmpty(),
            "UseCase implementations should be internal, but are public: $violations",
        )
    }

    @Test
    fun `controllers should not be public`() {
        val violations =
            allClasses
                .filter { it.isRestController() }
                .filter { it.kotlinVisibility() == KVisibility.PUBLIC }
                .map { it.simpleName }

        assertTrue(
            violations.isEmpty(),
            "Controllers should be internal, but are public: $violations",
        )
    }

    @Test
    fun `configuration classes should not be public`() {
        val violations =
            allClasses
                .filter { it.isConfiguration() }
                .filter { it.kotlinVisibility() == KVisibility.PUBLIC }
                .map { it.simpleName }

        assertTrue(
            violations.isEmpty(),
            "Configuration classes should be internal, but are public: $violations",
        )
    }

    @Test
    fun `usecase interfaces should be public`() {
        val violations =
            allClasses
                .filter { it.isUseCaseInterface() }
                .filter { it.kotlinVisibility() != KVisibility.PUBLIC }
                .map { it.simpleName }

        assertTrue(
            violations.isEmpty(),
            "UseCase interfaces should be public, but are not: $violations",
        )
    }

    @Test
    fun `model classes should be public`() {
        val violations =
            allClasses
                .filter { it.isModelClass() }
                .filter { it.kotlinVisibility() != KVisibility.PUBLIC }
                .map { it.simpleName }

        assertTrue(
            violations.isEmpty(),
            "Model classes should be public, but are not: $violations",
        )
    }

    @Test
    fun `usecase module should not depend on spring-web`() {
        val usecaseClasses =
            allClasses.filter {
                it.isUseCaseInterface() ||
                    it.isUseCaseImpl() ||
                    it.simpleName.endsWith("Repository") && it.isInterface
            }

        val springWebDeps = mutableSetOf<String>()
        usecaseClasses.forEach { javaClass ->
            javaClass.directDependenciesFromSelf.forEach { dep ->
                val targetClass = dep.targetClass
                if (targetClass != null && targetClass.name.startsWith("org.springframework.web")) {
                    springWebDeps.add(targetClass.name)
                }
            }
        }

        assertTrue(
            springWebDeps.isEmpty(),
            "usecase module should not depend on spring-web, found: $springWebDeps",
        )
    }

    @Test
    fun `repositories should only exist in data adapter modules`() {
        val violations =
            allClasses
                .filter { it.isRepository() }
                .filter {
                    it.packageName.contains(".usecase") ||
                        it.packageName.contains(".web-api") ||
                        it.packageName.contains(".app")
                }
                .map { it.simpleName }

        assertTrue(
            violations.isEmpty(),
            "Repository annotations should not be in usecase/app/web-api modules: $violations",
        )
    }
}
