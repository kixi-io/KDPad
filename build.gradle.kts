import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "io.kixi"
version = "1.0.0-beta-2"
description = "KDPad"

plugins {
    java
    kotlin("jvm") version "1.4.10"
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

repositories { mavenCentral() }

dependencies {
    implementation(kotlin("stdlib"))
    implementation(files("lib/Ki.KD-ktAll-1.0.0-beta-2.jar"))
    testImplementation(platform("org.junit:junit-bom:5.7.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

sourceSets {
    main {
        resources {
            srcDirs("src/main/res")
        }
    }
}

/**
 * Builds an executable Jar file
 */
tasks.register("jar-javaAll", Jar::class) {
    this.dependsOn("jar")

    group = "build"
    manifest { attributes["Main-Class"] = "io.kixi.app.kdpad.KDPadKt" }
    archiveBaseName.set("KDPad_SA")

    val deps = configurations.compileClasspath.get().map {
        if(it.name.contains("kotlin-stdlib-common")) {
            null
        } else {
            if (it.isDirectory) it else zipTree(it)
        }
    }

    val newDeps = mutableListOf<Any?>()
    newDeps.addAll(deps)
    newDeps.add("build/classes/kotlin/main")
    newDeps.add("build/classes/java/main")
    newDeps.add("build/resources/main")

    for(dep in newDeps) println(dep?.javaClass?.simpleName + " $dep")

    from(newDeps)
    exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")
}

tasks.test {
    useJUnitPlatform()

    val failedTests = mutableListOf<TestDescriptor>()
    val skippedTests = mutableListOf<TestDescriptor>()

    addTestListener(object : TestListener {
        override fun beforeSuite(suite: TestDescriptor) {}
        override fun beforeTest(testDescriptor: TestDescriptor) {}
        override fun afterTest(testDescriptor: TestDescriptor, result: TestResult) {
            when (result.resultType) {
                TestResult.ResultType.FAILURE -> failedTests.add(testDescriptor)
                TestResult.ResultType.SKIPPED -> skippedTests.add(testDescriptor)
                else -> Unit
            }
        }
        override fun afterSuite(suite: TestDescriptor, result: TestResult) {
            if (suite.parent == null) { // root suite
                logger.lifecycle("----")
                logger.lifecycle("Test result: ${result.resultType}")
                logger.lifecycle(
                    "Test summary: ${result.testCount} tests, " +
                            "${result.successfulTestCount} succeeded, " +
                            "${result.failedTestCount} failed, " +
                            "${result.skippedTestCount} skipped")
            }
        }
    })
}