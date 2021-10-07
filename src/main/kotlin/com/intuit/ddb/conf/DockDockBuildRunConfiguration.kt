package com.intuit.ddb.conf

import com.fasterxml.jackson.databind.ObjectMapper
import com.intellij.execution.Executor
import com.intellij.execution.configuration.EnvironmentVariablesData
import com.intellij.execution.configurations.* // ktlint-disable no-wildcard-imports
import com.intellij.execution.process.ColoredProcessHandler
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.ide.plugins.cl.PluginClassLoader
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
import com.intellij.util.getOrCreate
import com.intuit.ddb.* // ktlint-disable no-wildcard-imports
import org.jdom.Element
import java.io.File
import java.net.URLDecoder

// This class handles the *run* configurations of the plugin
open class DockDockBuildRunConfiguration(project: Project, factoryDocker: DockDockBuildRunConfigurationFactory, name: String) :
    LocatableConfigurationBase<RunProfileState>(project, factoryDocker, name) {

    var makefileFilePath = ""
    var dockerfileDir = ""
    var dockerImageUrl = ""
    var isDockerImage = ""
    var isDockerfile = ""
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
        // TODO:check for valid configuration
    }

    override fun getConfigurationEditor() = DockDockBuildRunConfigurationEditor(project)

    override fun writeExternal(element: Element) {
        super.writeExternal(element)
        val child = element.getOrCreate(DOCKER_MAKE)
        child.setAttribute(MAKEFILE_FILEPATH, makefileFilePath)
        child.setAttribute(DOCKER_FILENAME, dockerfileDir)
        child.setAttribute(DOCKER_IMAGE, dockerImageUrl)
        child.setAttribute(IS_DOCKER_IMAGE, isDockerImage)
        child.setAttribute(IS_DOCKER_FILE, isDockerfile)
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
            isDockerImage = child.getAttributeValue(IS_DOCKER_IMAGE) ?: ""
            isDockerfile = child.getAttributeValue(IS_DOCKER_FILE) ?: ""
            target = child.getAttributeValue(TARGET) ?: ""
            envScriptPath = child.getAttributeValue(ENV_SCRIPT) ?: ""
            arguments = child.getAttributeValue(ARGUMENTS) ?: ""
            environmentVariables = EnvironmentVariablesData.readExternal(child)
        }
    }

    override fun getState(executor: Executor, executionEnvironment: ExecutionEnvironment): RunProfileState? {

        handleParams()

        val decodedCP = getClassPath()
        val userDir = System.getProperty("user.dir")

        return object : CommandLineState(executionEnvironment) {
            override fun startProcess(): ProcessHandler {

                // java -cp <classPath> com.intuit.ddb.CmdProcessBuilder <parameters for Java class>
                val params = ParametersList()
                params.addAll("-cp", decodedCP, PROCESS_TO_RUN, getParamsFile(project))

                val cmd = GeneralCommandLine()
                    .withExePath("java")
                    .withWorkDirectory(userDir)
                    .withEnvironment(environmentVariables.envs)
                    .withParentEnvironmentType(
                        if (environmentVariables.isPassParentEnvs) GeneralCommandLine.ParentEnvironmentType.CONSOLE
                        else GeneralCommandLine.ParentEnvironmentType.NONE
                    )
                    .withParameters(params.list)

                val processHandler = ColoredProcessHandler(cmd)
                ProcessTerminatedListener.attach(processHandler)

                return processHandler
            }
        }
    }

    private fun handleParams() {

        // Plugin (project) configuration
        val dockerPath = ServiceManager.getService(project, DockDockBuildProjectSettings::class.java)
            .settings.dockerPath
        val codePath = ServiceManager.getService(project, DockDockBuildProjectSettings::class.java)
            .settings.codePath
        val m2Path = ServiceManager.getService(project, DockDockBuildProjectSettings::class.java)
            .settings.mavenCachePath
        val advancedDockerSettings = ServiceManager.getService(project, DockDockBuildProjectSettings::class.java)
            .settings.advancedDockerSettings

        // Runtime configurations
        // on host
        val dockerfileDir = if (dockerfileDir != "") dockerfileDir else getDefaultDockerfileDir(makefileFilePath)
        val makefileFileName = if (makefileFilePath != "") getMakefileFilename(makefileFilePath) else ""
        // on Docker
        val makefilePath = if (makefileFilePath != "") getMakefileDir(project, makefileFilePath) else "."
        val envScriptPath = if (envScriptPath != "") getSetEnvRelPath(project, envScriptPath) else ""

        // create Parameters obj and write to file to be used in CmdProcessBuilder
        val objectMapper = ObjectMapper()
        val cmdParams = Parameters(
            dockerPath, dockerfileDir, dockerImageUrl, isDockerImage.toBoolean(),
            makefilePath, makefileFileName, target, codePath, m2Path, envScriptPath, advancedDockerSettings
        )
        objectMapper.writeValue(File(getParamsFile(project)), cmdParams)
    }

    // iterate over IntelliJ's PluginClassLoader and find DockDockBuild.jar classpath to call CmdProcessBuilder
    private fun getClassPath(): String {
        val jarRegex = Regex("DockDockBuild.jar")
        var classpath = ""

        for (cp in (CmdProcessBuilder::class.java.classLoader as PluginClassLoader).urls) {
            if (jarRegex.containsMatchIn(cp.file)) {
                classpath = cp.path
                break
            }
        }
        return URLDecoder.decode(classpath, "UTF-8")
    }
}
