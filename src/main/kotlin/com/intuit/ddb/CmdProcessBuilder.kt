package com.intuit.ddb

import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

/*
This class is the core of the plugin. It calls Docker build & run commands.
The implementation is it in a separate class (and not DockDockBuildRunConfiguration.getState())
since we needed to call multiple commands.
It's getting compiled first in Gradle and then DockDockBuildRunConfiguration.getState() runs CmdProcessBuilder.class
 */
object CmdProcessBuilder {
    enum class CMD {
        BUILD,
        PULL,
        RUN,
    }

    private const val DEFAULT_TAG = "build"
    private const val CODE_PATH_DOCKER = "/home/ddb"
    private const val M2_PATH_DOCKER = "/root/.m2"

    @JvmStatic
    @Throws(IOException::class, InterruptedException::class)
    fun main(args: Array<String>) {
        if (args.size != 1) {
            usage()
        }

        /* input args - JSON file path that contains the following details:
        {"dockerExe":"<docker executable path>",
        "dockerfileDir":"<dockerfile directory>",
        "dockerImgUrl":"<OR URL:tag>",
        "isImage":<true/ false>,
        "makefilePath":".",
        "makefileFile":"Makefile",
        "target":"<make built target>",
        "codePath":"<code location>",
        "m2Path":"<maven cache path>",
        "envScript":"<optional environment script>"}
         */
        val p = Parameters.readParams(args[0])

        // if using an image, the tag will be the tag given in dockerImgUrl, else (for build) use default
        val imageTag = if (p.isImage == true) p.dockerImgUrl else DEFAULT_TAG
        val codeVol = "${p.codePath}:$CODE_PATH_DOCKER"
        val mavenVol = "${p.m2Path}:$M2_PATH_DOCKER"

        // if not working w/ prebuilt image, build from Dockerfile
        if (p.isImage == false) {
            dockerBuild(p.dockerExe, p.dockerfileDir, imageTag)
        } else {
            dockerPull(p.dockerExe, p.dockerImgUrl)
        }

        dockerRun(
            p.dockerExe,
            codeVol,
            mavenVol,
            imageTag,
            p.envScript,
            p.makefilePath,
            p.target,
            p.makefileFile,
            p.advancedDockerSettings,
        )

        println("Program ended")
    }

    private fun usage() {
        System.err.println(
            "CmdProcessBuilder usage:\n " +
                "java -cp \"<DockDockBuild jar>\" com.intuit.ddb.CmdProcessBuilder <parameters.json>\n\n" +
                "input args - JSON file path that contains the following details:\n" +
                "        {\"dockerExe\":\"<docker executable path>\",\n" +
                "        \"dockerfileDir\":\"<dockerfile directory>\",\n" +
                "        \"dockerImgUrl\":\"<OR URL:tag>\",\n" +
                "        \"isImage\":<true/ false>,\n" +
                "        \"makefilePath\":\".\",\n" +
                "        \"makefileFile\":\"Makefile\",\n" +
                "        \"target\":\"<make built target>\",\n" +
                "        \"codePath\":\"<code location>\",\n" +
                "        \"m2Path\":\"<maven cache path>\",\n" +
                "        \"envScript\":\"<optional environment script>\"\n" +
                "        \"advancedDockerSettings\":\"<optional advanced settings for the docker run command>\"",
        )
        exitProcess(42)
    }

    @Throws(IOException::class, InterruptedException::class)
    private fun dockerBuild(
        dockerPath: String?,
        dockerfileDir: String?,
        tag: String?,
    ) {
        val builder = ProcessBuilder()
        builder.command(buildDockerBuildCmd(dockerPath, tag))
        if (dockerfileDir != null) builder.directory(File(dockerfileDir))
        executeCmd(CMD.BUILD, builder)
    }

    @Throws(IOException::class, InterruptedException::class)
    private fun dockerPull(
        dockerPath: String?,
        tag: String?,
    ) {
        val builder = ProcessBuilder()
        builder.command(buildDockerPullCmd(dockerPath, tag))
        executeCmd(CMD.PULL, builder)
    }

    @Throws(IOException::class, InterruptedException::class)
    private fun dockerRun(
        dockerPath: String?,
        codeVol: String?,
        mavenVol: String?,
        tag: String?,
        envScript: String?,
        runDir: String?,
        target: String?,
        makefileFile: String?,
        advancedDockerSettings: String?,
    ) {
        val builder = ProcessBuilder()
        builder.command(
            buildDockerRunCmd(dockerPath, codeVol, mavenVol, tag, envScript, runDir, target, makefileFile, advancedDockerSettings),
        )
        executeCmd(CMD.RUN, builder)
    }

    @Throws(IOException::class, InterruptedException::class)
    private fun executeCmd(
        cmd: CMD,
        builder: ProcessBuilder,
    ) {
        builder.inheritIO()

        println("\nDocker $cmd:")
        prettyPrint(builder.command())

        val process = builder.start()

        if (!process.waitFor(60, TimeUnit.MINUTES)) {
            System.err.println("$cmd REACHED TIMEOUT")
            process.destroyForcibly()
            exitProcess(-1)
        }

        if (process.exitValue() != 0) {
            System.err.println("$cmd FAILED. Stopping")
            exitProcess(process.exitValue())
        }

        process.destroy()
    }

    // print array in a "runnable" form
    private fun prettyPrint(command: List<String>) {
        for (word in command) {
            print("$word ")
        }
        println("\n")
    }
}

// docker build . --tag <tag>
internal fun buildDockerBuildCmd(
    dockerPath: String?,
    tag: String?,
): List<String> = listOf(dockerPath, "build", ".", "--tag", tag).filterNotNull()

// docker pull <tag>
internal fun buildDockerPullCmd(
    dockerPath: String?,
    tag: String?,
): List<String> = listOf(dockerPath, "pull", tag).filterNotNull()

// docker run --rm -t --volume <codeVol> --volume <mavenVol> [advancedSettings] <tag> bash -c <innerCmd>
internal fun buildDockerRunCmd(
    dockerPath: String?,
    codeVol: String?,
    mavenVol: String?,
    tag: String?,
    envScript: String?,
    runDir: String?,
    target: String?,
    makefileFile: String?,
    advancedDockerSettings: String?,
): List<String> {
    val cmd = mutableListOf(dockerPath, "run", "--rm", "-t", "--volume", codeVol, "--volume", mavenVol)

    if (!advancedDockerSettings.isNullOrEmpty()) {
        cmd.addAll(advancedDockerSettings.split("\\s+".toRegex()).filter { it.isNotEmpty() })
    }
    cmd.add(tag)
    cmd.addAll(listOf("bash", "-c"))

    // Each variable is single-quoted to prevent shell word-splitting and injection.
    // Single quotes are the only characters that cannot appear inside a single-quoted string,
    // so we reject them at the call site (validated in checkConfiguration).
    var innerCmd = ""
    if (!envScript.isNullOrEmpty()) {
        innerCmd = "source '${envScript.replace("'", "")}' && "
    }
    if (runDir != ".") {
        innerCmd += "cd '${runDir?.replace("'", "")}' && "
    }
    innerCmd += "make -r -f '${makefileFile?.replace("'", "")}' '${target?.replace("'", "")}'"
    cmd.add(innerCmd)

    return cmd.filterNotNull()
}
