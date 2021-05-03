package com.intuit.ddb;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;


/*
This class is the core of the plugin. It calls Docker build & run commands.
The implementation is it in a separate class (and not DockDockBuildRunConfiguration.getState())
since we needed to call multiple commands.
It's getting compiled first in Gradle and then DockDockBuildRunConfiguration.getState() runs CmdProcessBuilder.class
 */
public class CmdProcessBuilder {

    enum CMD {
        BUILD,
        PULL,
        RUN
    }

    final private static String DEFAULT_TAG = "build";
    final private static String CODE_PATH_DOCKER = "/home/ddb";
    final private static String M2_PATH_DOCKER = "/root/.m2";

    public static void main(String[] args) throws IOException, InterruptedException {

        if(args.length != 1) {
            usage();
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
        Parameters p = Parameters.readParams(args[0]);

        // if using an image, the tag will be the the tag given in dockerImgUrl, else (for build) use default
        String imageTag = p.isImage == Boolean.TRUE ? p.dockerImgUrl : DEFAULT_TAG;
        String codeVol = p.codePath + ":" + CODE_PATH_DOCKER;
        String mavenVol = p.m2Path + ":" + M2_PATH_DOCKER;

        // if not working w/ prebuilt image, build from Dockerfile
        if (p.isImage == Boolean.FALSE) {
            dockerBuild(p.dockerExe, p.dockerfileDir, imageTag);
        } else {
            dockerPull(p.dockerExe, p.dockerImgUrl);
        }

        dockerRun(p.dockerExe, codeVol, mavenVol, imageTag, p.envScript, p.makefilePath, p.target,
                p.makefileFile, p.advancedDockerSettings);

        System.out.println("Program ended");
    }


    private static void usage() {
        System.err.println("CmdProcessBuilder usage:\n "
                + "java -cp \"<DockDockBuild jar>\" com.intuit.ddb.CmdProcessBuilder <parameters.json>\n\n"
                + "input args - JSON file path that contains the following details:\n"
                + "        {\"dockerExe\":\"<docker executable path>\",\n"
                + "        \"dockerfileDir\":\"<dockerfile directory>\",\n"
                + "        \"dockerImgUrl\":\"<OR URL:tag>\",\n"
                + "        \"isImage\":<true/ false>,\n"
                + "        \"makefilePath\":\".\",\n"
                + "        \"makefileFile\":\"Makefile\",\n"
                + "        \"target\":\"<make built target>\",\n"
                + "        \"codePath\":\"<code location>\",\n"
                + "        \"m2Path\":\"<maven cache path>\",\n"
                + "        \"envScript\":\"<optional environment script>\""
                + "        \"advancedDockerSettings\":\"<optional advanced settings for the docker run command>\"");
        System.exit(42);
    }


    private static void dockerBuild(String dockerPath, String dockerfileDir, String tag) throws IOException,
            InterruptedException {

        ProcessBuilder builder = new ProcessBuilder();

        // ******** DOCKER BUILD ********
        // docker build . --tag <tag>
        builder.command(dockerPath, "build", ".", "--tag", tag).
                directory(new File(dockerfileDir));

        executeCmd(CMD.BUILD, builder);
    }


    private static void dockerPull(String dockerPath, String tag) throws IOException, InterruptedException {

        ProcessBuilder builder = new ProcessBuilder();

        // ******** DOCKER PULL ********
        // docker pull <tag>
        builder.command(dockerPath, "pull", tag);

        executeCmd(CMD.PULL, builder);
    }


    private static void dockerRun(String dockerPath, String codeVol, String mavenVol, String tag, String envScript,
                                  String runDir, String target, String makefileFile, String advancedDockerSettings) throws IOException,
            InterruptedException {

        ProcessBuilder builder = new ProcessBuilder();

        // ******** DOCKER RUN ********
        // docker run --rm -t --volume <codeVol> --volume <mavenVol> <advancedDockerSettings?> <tag>
        List<String> cmd = new LinkedList<>(Arrays.asList(dockerPath, "run", "--rm", "-t", "--volume", codeVol,
                "--volume", mavenVol));
        if (!advancedDockerSettings.equals("")) {
            cmd.addAll(Arrays.asList(advancedDockerSettings.split("\\s+")));
        }
        cmd.addAll(Collections.singletonList(tag));

        // Docker command
        // full cmd: bash -c source <envScript> && cd <runDir> && make -r -f <makefileFile> <target>
        cmd.addAll((Arrays.asList("bash", "-c")));
        String innerCmd = "";
        // add source if environment script given
        if (envScript != null && !envScript.equals("")) {
            innerCmd = "source " + envScript + " && ";
        }
        // cd <runDir>
        if (!runDir.equals(".")) {
            innerCmd += "cd " + runDir + " && ";
        }
        //  make -r -f <makefileFile> <target>
        innerCmd += "make -r -f " + makefileFile + " " + target;
        cmd.addAll(Collections.singletonList(innerCmd));
        builder.command(cmd);

        executeCmd(CMD.RUN, builder);
    }


    private static void executeCmd(CMD cmd, ProcessBuilder builder) throws IOException,
            InterruptedException {

        String line;

        builder.inheritIO();

        System.out.println("\n" + "Docker " + cmd + ":");
        prettyPrint(builder.command());

        final Process process = builder.start();

        BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));

        while ((line = br.readLine()) != null) {
            System.out.println(line);
        }

        if (!process.waitFor(60, TimeUnit.MINUTES)) {
            System.err.println(cmd + " REACHED TIMOUT");
            System.exit(-1);
        }

        if (process.exitValue() != 0) {
            System.err.println(cmd + " FAILED. Stopping");
            System.exit(process.exitValue());
        }

        process.destroy();
    }


    // print array in a "runnable" form
    private static void prettyPrint(List<String> command) {
        for (String word : command) {
            System.out.print(word + " ");
        }

        System.out.println("\n");
    }

}
