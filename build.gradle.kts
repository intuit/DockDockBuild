import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

fun properties(key: String) = providers.gradleProperty(key)

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.intellij.platform")
    id("org.jetbrains.changelog")
    id("jacoco")
    id("org.jlleitschuh.gradle.ktlint")
}

group = properties("pluginGroup").get()
version = properties("pluginVersion").get()

kotlin {
    sourceSets {
        main {
            kotlin.srcDir("src/main/kotlin")
        }
    }
    jvmToolchain(properties("javaVersion").get().toInt())
}

java {
    sourceSets {
        main {
            java.srcDirs("src/main/java", "gen")
            resources.srcDirs("src/main/resources")
        }
        create("uiTest") {
            kotlin.srcDirs("src/uiTest/kotlin")
            resources.srcDirs("src/uiTest/resources")
            compileClasspath += sourceSets.main.get().output
            runtimeClasspath += sourceSets.main.get().output
        }
    }
}

val uiTestImplementation: Configuration by configurations.getting {
    extendsFrom(configurations.testImplementation.get())
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-core:2.18.3")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.3")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.18.3")
    testImplementation("org.hamcrest:hamcrest:2.2")
    testImplementation("junit:junit:4.13.2")

    uiTestImplementation("com.intellij.remoterobot:remote-robot:0.11.23")
    uiTestImplementation("com.intellij.remoterobot:remote-fixtures:0.11.23")
    uiTestImplementation("com.squareup.okhttp3:okhttp:4.12.0")
    uiTestImplementation("junit:junit:4.13.2")

    intellijPlatform {
        intellijIdeaCommunity(properties("platformVersion").get())
        testFramework(TestFrameworkType.Platform)
    }
}

intellijPlatform {
    pluginConfiguration {
        name = properties("pluginName")
        version = properties("pluginVersion")

        description =
            providers.fileContents(layout.projectDirectory.file("README.md")).asText.map {
                val start = "<!-- Plugin description -->"
                val end = "<!-- Plugin description end -->"

                with(it.lines()) {
                    if (!containsAll(listOf(start, end))) {
                        throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
                    }
                    subList(indexOf(start) + 1, indexOf(end)).joinToString("\n").let(::markdownToHTML)
                }
            }

        val changelog = project.changelog
        changeNotes =
            properties("pluginVersion").map { pluginVersion ->
                with(changelog) {
                    renderItem(
                        (getOrNull(pluginVersion) ?: getUnreleased())
                            .withHeader(false)
                            .withEmptySections(false),
                        Changelog.OutputType.HTML,
                    )
                }
            }

        ideaVersion {
            sinceBuild = properties("pluginSinceBuild")
        }
    }

    signing {
        certificateChain = providers.environmentVariable("CERTIFICATE_CHAIN")
        privateKey = providers.environmentVariable("PRIVATE_KEY")
        password = providers.environmentVariable("PRIVATE_KEY_PASSWORD")
    }

    publishing {
        token = providers.environmentVariable("PUBLISH_TOKEN")
        channels =
            properties("pluginVersion").map {
                listOf(it.split('-').getOrElse(1) { "default" }.split('.').first())
            }
    }
}

intellijPlatformTesting {
    runIde {
        register("runIdeForUiTests") {
            task {
                jvmArgumentProviders +=
                    CommandLineArgumentProvider {
                        listOf(
                            "-Drobot-server.port=8082",
                            "-Dide.mac.message.dialogs.as.sheets=false",
                            "-Djb.privacy.policy.text=<!--999.999-->",
                            "-Djb.consents.confirmation.enabled=false",
                        )
                    }
            }
            plugins {
                robotServerPlugin()
            }
        }
    }
}

val uiTest =
    task<Test>("uiTest") {
        description = "Runs UI tests against a running IDE instance (start with runIdeForUiTests first)."
        group = "verification"
        testClassesDirs = sourceSets["uiTest"].output.classesDirs
        classpath = sourceSets["uiTest"].runtimeClasspath
    }

changelog {
    groups.empty()
    versionPrefix = ""
}

tasks {
    named<ProcessResources>("processResources") {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }

    wrapper {
        gradleVersion = properties("gradleVersion").get()
    }

    withType<KotlinCompile> {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    jar {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        dependsOn(ktlintCheck)
        archiveFileName.set("DockDockBuild.jar")

        manifest {
            attributes("Main-Class" to "com.intuit.ddb.CmdProcessBuilder")
        }

        from(sourceSets.main.get().output)
        dependsOn(configurations.runtimeClasspath)
        from({
            configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
        },)
    }

    test {
        finalizedBy(jacocoTestReport)
    }

    jacocoTestReport {
        dependsOn(test)
    }

    publishPlugin {
        dependsOn(patchChangelog)
    }
}
