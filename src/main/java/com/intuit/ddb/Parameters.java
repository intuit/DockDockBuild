package com.intuit.ddb;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;

@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public class Parameters {

    // Docker executable path
    String dockerExe;
    // Dockerfile directory
    String dockerfileDir;
    // OR the docker image URL (url:tag)
    String dockerImgUrl;
    // is using docker image
    Boolean isImage;
    // makefile path
    String makefilePath;
    // makefile filename
    String makefileFile;
    // makefile target to invoke
    String target;
    // code path to mount onto Docker container
    String codePath;
    // Maven cache path to mount onto Docker container
    String m2Path;
    // environment script to run at Docker container rise
    String envScript;
    // optional advanced settings for the docker cmd
    String advancedDockerSettings;


    public Parameters(String dockerExe, String dockerfileDir, String dockerImgUrl, Boolean isImage,
            String makefilePath, String makefileFile, String target, String codePath, String m2Path,
            String envScript, String advancedDockerSettings) {

        this.dockerExe = dockerExe;
        this.dockerfileDir = dockerfileDir;
        this.dockerImgUrl = dockerImgUrl;
        this.isImage = isImage;
        this.makefilePath = makefilePath;
        this.makefileFile = makefileFile;
        this.target = target;
        this.codePath = codePath;
        this.m2Path = m2Path;
        this.envScript = envScript;
        this.advancedDockerSettings = advancedDockerSettings;

    }

    // empty constructor needed for jackson
    Parameters() {

    }

    // deserialize from Json in file to Parameters obj
    static Parameters readParams(String file) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(new File(file), Parameters.class);
    }

}
