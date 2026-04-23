package com.intuit.ddb

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.File
import java.io.IOException

@JsonAutoDetect(fieldVisibility = Visibility.ANY)
@JsonIgnoreProperties(ignoreUnknown = true)
data class Parameters(
    // Docker executable path
    val dockerExe: String?,
    // Dockerfile directory
    val dockerfileDir: String?,
    // OR the docker image URL (url:tag)
    val dockerImgUrl: String?,
    // is using docker image
    val isImage: Boolean?,
    // makefile path
    val makefilePath: String?,
    // makefile filename
    val makefileFile: String?,
    // makefile target to invoke
    val target: String?,
    // code path to mount onto Docker container
    val codePath: String?,
    // Maven cache path to mount onto Docker container
    val m2Path: String?,
    // environment script to run at Docker container rise
    val envScript: String?,
    // optional advanced settings for the docker cmd
    val advancedDockerSettings: String?,
) {
    companion object {
        // deserialize from Json in file to Parameters obj
        @Throws(IOException::class)
        fun readParams(file: String): Parameters {
            val objectMapper = ObjectMapper().registerKotlinModule()
            return objectMapper.readValue(File(file), Parameters::class.java)
        }
    }
}
