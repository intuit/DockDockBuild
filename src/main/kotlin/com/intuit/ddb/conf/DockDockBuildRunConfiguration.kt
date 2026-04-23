@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.intuit.ddb.conf

import com.fasterxml.jackson.databind.ObjectMapper
import com.intellij.execution.Executor
import com.intellij.execution.configuration.EnvironmentVariablesData
import com.intellij.execution.configurations.*
import com.intellij.execution.process.ColoredProcessHandler
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.project.Project
import com.intuit.ddb.*
import org.jdom.Element
import java.io.File

// This class handles the *run* configurations of the plugin
open class DockDockBuildRunConfiguration(project: Project, factoryDocker: DockDockBuildRunConfigurationFactory, name: String) :
    LocatableConfigurationBase<RunProfileState>(project, factoryDocker, name) {
    var makefileFilePath = ""
    var dockerfileDir = ""
    var dockerImageUrl = ""
    var isDockerImage = false
    var isDockerfile = true
    var target = ""
    var envScriptPath = ""
    var arguments = ""
    var environmentVariables: EnvironmentVariablesData = EnvironmentVariablesData.DEFAULT

    private companion object {
        const val DOCKER_MAKE = "dockDockBuild"
        const val DOCKER_FILENAME = "dockerfileDir"
        const val DOCKER_IMAGE = "dockerImageUrl"
        const val IS_DOCKER_IMAGE = "isDockerImage"
        const val IS_DOCKER_FILE = "isDockerfile"
        const val MAKEFILE_FILEPATH = "makefileFilePath"
        const val TARGET = "target"
        const val ENV_SCRIPT = "envScriptPath"
        const val ARGUMENTS = "arguments"
    }

    override fun checkConfiguration() {
        if (makefileFilePath.isBlank()) throw RuntimeConfigurationError("Makefile path must not be empty")
        if (target.isBlank()) throw RuntimeConfigurationError("Target must not be empty")
        if (isDockerfile && dockerfileDir.isBlank()) throw RuntimeConfigurationError("Dockerfile path must not be empty")
        if (isDockerImage && dockerImageUrl.isBlank()) throw RuntimeConfigurationError("Docker image URL must not be empty")
    }

    override fun getConfigurationEditor() = DockDockBuildRunConfigurationEditor(project)

    override fun writeExternal(element: Element) {
        super.writeExternal(element)
        val child = element.getOrCreateChild(DOCKER_MAKE)
        child.setAttribute(MAKEFILE_FILEPATH, makefileFilePath)
        child.setAttribute(DOCKER_FILENAME, dockerfileDir)
        child.setAttribute(DOCKER_IMAGE, dockerImageUrl)
        child.setAttribute(IS_DOCKER_IMAGE, isDockerImage.toString())
        child.setAttribute(IS_DOCKER_FILE, isDockerfile.toString())
        child.setAttribute(TARGET, target)
        child.setAttribute(ENV_SCRIPT, envScriptPath)
        child.setAttribute(ARGUMENTS, arguments)
        environmentVariables.writeExternal(child)
    }

    override fun readExternal(element: Element) {
        super.readExternal(element)
        val child = element.getChild(DOCKER_MAKE)
        if (child != null) {
            makefileFilePath = child.getAttributeValue(MAKEFILE_FILEPATH) ?: ""
            dockerfileDir = child.getAttributeValue(DOCKER_FILENAME) ?: ""
            dockerImageUrl = child.getAttributeValue(DOCKER_IMAGE) ?: ""
            isDockerImage = child.getAttributeValue(IS_DOCKER_IMAGE)?.toBoolean() ?: false
            isDockerfile = child.getAttributeValue(IS_DOCKER_FILE)?.toBoolean() ?: true
            target = child.getAttributeValue(TARGET) ?: ""
            envScriptPath = child.getAttributeValue(ENV_SCRIPT) ?: ""
            arguments = child.getAttributeValue(ARGUMENTS) ?: ""
            environmentVariables = EnvironmentVariablesData.readExternal(child)
        }
    }

    override fun getState(
        executor: Executor,
        executionEnvironment: ExecutionEnvironment,
    ): RunProfileState? {
        val paramsFile = handleParams()

        val decodedCP = getClassPath()
        val userDir = System.getProperty("user.dir")

        return object : CommandLineState(executionEnvironment) {
            override fun startProcess(): ProcessHandler {
                // java -cp <classPath> com.intuit.ddb.CmdProcessBuilder <parameters for Java class>
                val params = ParametersList()
                params.addAll("-cp", decodedCP, PROCESS_TO_RUN, paramsFile)

                val cmd =
                    GeneralCommandLine()
                        .withExePath("java")
                        .withWorkDirectory(userDir)
                        .withEnvironment(environmentVariables.envs)
                        .withParentEnvironmentType(
                            if (environmentVariables.isPassParentEnvs) {
                                GeneralCommandLine.ParentEnvironmentType.CONSOLE
                            } else {
                                GeneralCommandLine.ParentEnvironmentType.NONE
                            },
                        )
                        .withParameters(params.list)

                val processHandler = ColoredProcessHandler(cmd)
                ProcessTerminatedListener.attach(processHandler)

                return processHandler
            }
        }
    }

    private fun handleParams(): String {
        // Plugin (project) configuration
        val dockerPath =
            project.getService(DockDockBuildProjectSettings::class.java)
                .settings.dockerPath
        val codePath =
            project.getService(DockDockBuildProjectSettings::class.java)
                .settings.codePath
        val m2Path =
            project.getService(DockDockBuildProjectSettings::class.java)
                .settings.mavenCachePath
        val advancedDockerSettings =
            project.getService(DockDockBuildProjectSettings::class.java)
                .settings.advancedDockerSettings

        // Runtime configurations
        // on host
        val dockerfileDir = if (dockerfileDir != "") dockerfileDir else getDefaultDockerfileDir(makefileFilePath)
        val makefileFileName = if (makefileFilePath != "") getMakefileFilename(makefileFilePath) else ""
        // on Docker
        val makefilePath = if (makefileFilePath != "") getMakefileDir(project, makefileFilePath) else "."
        val envScriptPath = if (envScriptPath != "") getSetEnvRelPath(project, envScriptPath) else ""

        // Write params to a unique temp file so concurrent runs don't clobber each other.
        val objectMapper = ObjectMapper()
        val cmdParams =
            Parameters(
                dockerPath, dockerfileDir, dockerImageUrl, isDockerImage,
                makefilePath, makefileFileName, target, codePath, m2Path, envScriptPath, advancedDockerSettings,
            )
        val paramsFile = File.createTempFile("dockDockBuildParams", ".json")
        paramsFile.deleteOnExit()
        objectMapper.writeValue(paramsFile, cmdParams)
        return paramsFile.absolutePath
    }

    private fun getClassPath(): String {
        // Ask IntelliJ's plugin manager for the plugin's install path, then find the jar inside it.
        // This works regardless of classloader implementation (PluginClassLoader, URLClassLoader, etc.)
        val pluginId = PluginId.getId(PLUGIN_ID)
        val descriptor =
            PluginManagerCore.getPlugin(pluginId)
                ?: throw RuntimeException("Plugin descriptor not found for id: $pluginId")

        val pluginPath = descriptor.pluginPath
        val jar = pluginPath.resolve("lib/DockDockBuild.jar").toFile()
        if (jar.exists()) {
            return jar.absolutePath
        }

        // Fallback: search lib/ for any DockDockBuild jar
        val libDir = pluginPath.resolve("lib").toFile()
        if (libDir.isDirectory) {
            val found =
                libDir.listFiles { f -> f.name.startsWith("DockDockBuild") && f.name.endsWith(".jar") }
                    ?.firstOrNull()
            if (found != null) return found.absolutePath
        }

        throw RuntimeException("DockDockBuild.jar not found under plugin path: $pluginPath")
    }
}
