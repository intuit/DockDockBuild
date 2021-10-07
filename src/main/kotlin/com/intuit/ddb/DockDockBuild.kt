package com.intuit.ddb

import com.intellij.openapi.project.Project
import org.apache.commons.lang3.SystemUtils
import java.nio.file.Paths

const val PLUGIN_NAME = "DockDockBuild"
const val PROCESS_TO_RUN = "com.intuit.ddb.CmdProcessBuilder"

fun getDefaultCodePath(project: Project): String {
    return project.basePath.toString()
}

fun getDefaultDockerPath(): String {

    when {
        SystemUtils.IS_OS_WINDOWS -> return "docker"
        SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_UNIX -> return "/usr/local/bin/docker"
    }
    return ""
}

fun getDefaultM2Path(): String {
    return System.getProperty("user.home") + "/.m2"
}

fun getBasePath(project: Project): String {
    return getDefaultCodePath(project) + "/build"
}

fun getMakefileFilename(makefileFilename: String): String {
    val pathAbsolute = Paths.get(makefileFilename)
    return pathAbsolute.fileName.toString()
}

// get relative path for makefile to cd into in the Docker container
fun getMakefileDir(project: Project, makefileFilename: String): String {
    val pathAbsolute = Paths.get(makefileFilename)
    val pathBase = Paths.get(getBasePath(project))
    val pathRelative = pathBase.relativize(pathAbsolute)

    // if already in location, return . (dot - current dir) to avoid doing cd
    return pathRelative.parent?.toString() ?: "."
}

// Dockerfile is defaultly marked to be in the same folder as the makefile under
// <makefilePath>/docker in the host folder
fun getDefaultDockerfileDir(makefileFilePath: String): String {
    val makefilePathAbsolute = Paths.get(makefileFilePath)

    return makefilePathAbsolute.parent.toString() + "/docker"
}

// get Docker container set_env.sh path
fun getSetEnvRelPath(project: Project, path: String): String {
    val pathAbsolute = Paths.get(path)
    val pathBase = Paths.get(getBasePath(project))

    return pathBase.relativize(pathAbsolute).toString()
}

fun getParamsFile(project: Project): String {
    val paramsFile = "/dockDockBuildParams.json"
    return getDefaultCodePath(project) + paramsFile
}
