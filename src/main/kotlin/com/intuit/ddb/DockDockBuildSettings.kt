package com.intuit.ddb

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class DockDockBuildSettings {
    var dockerPath = getDefaultDockerPath()
    var codePath = ""
    var mavenCachePath = getDefaultM2Path()
    var advancedDockerSettings = ""
}
