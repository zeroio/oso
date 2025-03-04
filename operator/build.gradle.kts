import io.fabric8.crd.generator.collector.CustomResourceCollector
import io.fabric8.crdv2.generator.CRDGenerationInfo
import io.fabric8.crdv2.generator.CRDGenerator
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import java.nio.file.Files

plugins {
    alias(libs.plugins.kotlin.jvm)
    idea
}

idea {
    module {
        isDownloadSources = true
        isDownloadJavadoc = true
    }
}

buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        classpath(libs.fabric8.k8s.crdgen.api)
        classpath(libs.fabric8.k8s.crdgen.collector)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform(libs.operator.sdk.bom))
    implementation(platform(libs.http4k.bom))
    implementation(libs.fabric8.k8s.client)
    implementation(libs.operator.sdk.framework)
    implementation(libs.logback.classic)
    implementation(libs.http4k.platform.k8s)
    implementation(libs.http4k.format.moshi.yaml)
    implementation(libs.bc.fips)
    implementation(libs.bcutil.fips)
    implementation(libs.bctls.fips)
    implementation(libs.bcpkix.fips)
    implementation(libs.jte)
    implementation(libs.password4j)
    //
    testImplementation(libs.kotest.assertion.core.jvm)
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            // Use JUnit Jupiter test framework
            useJUnitJupiter(libs.versions.junit)
        }
    }
}

val javaVersion = JavaLanguageVersion.of(libs.versions.jdk.get())
java {
    toolchain {
        languageVersion = javaVersion
    }
}

kotlin {
    jvmToolchain {
        languageVersion.set(javaVersion)
    }

    compilerOptions {
        val version = KotlinVersion.fromVersion(libs.versions.kt.get())
        apiVersion.set(version)
        languageVersion.set(version)
    }
}

tasks {
    val classes = named("classes")
    val genCrds = register("genCrds") {
        description = "Generate CRDs from compiled custom resource classes"
        group = "build"
        dependsOn(classes)

        val sourceSet = project.sourceSets["main"]

        val compileClasspathElements = sourceSet.compileClasspath.map { e -> e.absolutePath }

        val outputClassesDirs = sourceSet.output.classesDirs.filter { it.exists() }
        val outputClasspathElements = outputClassesDirs.map { d -> d.absolutePath }

        val classpathElements = listOf(outputClasspathElements, compileClasspathElements).flatten()
        val filesToScan = listOf(outputClassesDirs).flatten()
        val outputDir = sourceSet.output.resourcesDir

        doLast {
            Files.createDirectories(outputDir!!.toPath())

            val collector = CustomResourceCollector()
                .withParentClassLoader(Thread.currentThread().contextClassLoader)
                .withClasspathElements(classpathElements)
                .withFilesToScan(filesToScan)

            val crdGenerator = CRDGenerator()
                .customResourceClasses(collector.findCustomResourceClasses())
                .inOutputDir(outputDir)

            val crdGenerationInfo: CRDGenerationInfo = crdGenerator.detailedGenerate()

            crdGenerationInfo.crdDetailsPerNameAndVersion.forEach { (crdName, versionToInfo) ->
                println("Generated CRD $crdName:")
                versionToInfo.forEach { (version, info) -> println(" $version -> ${info.filePath}") }
            }
        }
    }

    classes.configure {
        finalizedBy(genCrds)
    }
}
