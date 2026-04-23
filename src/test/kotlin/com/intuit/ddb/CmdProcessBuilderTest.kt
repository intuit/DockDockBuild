package com.intuit.ddb

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CmdProcessBuilderTest {
    // ---- buildDockerBuildCmd ----

    @Test
    fun `buildDockerBuildCmd produces correct command`() {
        val cmd = buildDockerBuildCmd("/usr/bin/docker", "my-image")
        assertEquals(listOf("/usr/bin/docker", "build", ".", "--tag", "my-image"), cmd)
    }

    @Test
    fun `buildDockerBuildCmd uses default tag`() {
        val cmd = buildDockerBuildCmd("/usr/bin/docker", "build")
        assertEquals("build", cmd.last())
    }

    // ---- buildDockerPullCmd ----

    @Test
    fun `buildDockerPullCmd produces correct command`() {
        val cmd = buildDockerPullCmd("/usr/bin/docker", "registry/image:v1")
        assertEquals(listOf("/usr/bin/docker", "pull", "registry/image:v1"), cmd)
    }

    // ---- buildDockerRunCmd ----

    @Test
    fun `buildDockerRunCmd basic command without env script and root makefile dir`() {
        val cmd =
            buildDockerRunCmd(
                dockerPath = "/usr/bin/docker",
                codeVol = "/code:/home/ddb",
                mavenVol = "/root/.m2:/root/.m2",
                tag = "build",
                envScript = null,
                runDir = ".",
                target = "all",
                makefileFile = "Makefile",
                advancedDockerSettings = null,
            )

        assertEquals("/usr/bin/docker", cmd[0])
        assertEquals("run", cmd[1])
        assertTrue(cmd.contains("--rm"))
        assertTrue(cmd.contains("-t"))
        assertTrue(cmd.contains("--volume"))
        assertTrue(cmd.contains("/code:/home/ddb"))
        assertTrue(cmd.contains("/root/.m2:/root/.m2"))
        assertEquals("build", cmd[cmd.indexOf("bash") - 1])
        assertEquals("bash", cmd[cmd.size - 3])
        assertEquals("-c", cmd[cmd.size - 2])
        assertEquals("make -r -f 'Makefile' 'all'", cmd.last())
    }

    @Test
    fun `buildDockerRunCmd includes cd when runDir is not dot`() {
        val cmd =
            buildDockerRunCmd(
                dockerPath = "/usr/bin/docker",
                codeVol = "/code:/home/ddb",
                mavenVol = "/root/.m2:/root/.m2",
                tag = "build",
                envScript = null,
                runDir = "subdir",
                target = "test",
                makefileFile = "Makefile",
                advancedDockerSettings = null,
            )

        val innerCmd = cmd.last()
        assertTrue("Expected cd in inner command", innerCmd.contains("cd 'subdir' &&"))
        assertTrue(innerCmd.endsWith("make -r -f 'Makefile' 'test'"))
    }

    @Test
    fun `buildDockerRunCmd skips cd when runDir is dot`() {
        val cmd =
            buildDockerRunCmd(
                dockerPath = "/usr/bin/docker",
                codeVol = "/code:/home/ddb",
                mavenVol = "/root/.m2:/root/.m2",
                tag = "build",
                envScript = null,
                runDir = ".",
                target = "test",
                makefileFile = "Makefile",
                advancedDockerSettings = null,
            )

        assertTrue("cd should not appear when runDir is '.'", !cmd.last().contains("cd"))
    }

    @Test
    fun `buildDockerRunCmd includes source when envScript provided`() {
        val cmd =
            buildDockerRunCmd(
                dockerPath = "/usr/bin/docker",
                codeVol = "/code:/home/ddb",
                mavenVol = "/root/.m2:/root/.m2",
                tag = "build",
                envScript = "set_env.sh",
                runDir = ".",
                target = "build",
                makefileFile = "Makefile",
                advancedDockerSettings = null,
            )

        assertTrue(cmd.last().startsWith("source 'set_env.sh' &&"))
    }

    @Test
    fun `buildDockerRunCmd splits advancedDockerSettings into separate args`() {
        val cmd =
            buildDockerRunCmd(
                dockerPath = "/usr/bin/docker",
                codeVol = "/code:/home/ddb",
                mavenVol = "/root/.m2:/root/.m2",
                tag = "build",
                envScript = null,
                runDir = ".",
                target = "build",
                makefileFile = "Makefile",
                advancedDockerSettings = "--memory 2g --cpus 4",
            )

        assertTrue(cmd.contains("--memory"))
        assertTrue(cmd.contains("2g"))
        assertTrue(cmd.contains("--cpus"))
        assertTrue(cmd.contains("4"))
        // tag comes after advanced settings, before bash
        val tagIndex = cmd.indexOf("build")
        assertTrue(cmd.indexOf("2g") < tagIndex)
    }

    @Test
    fun `buildDockerRunCmd full command with all options`() {
        val cmd =
            buildDockerRunCmd(
                dockerPath = "/usr/bin/docker",
                codeVol = "/code:/home/ddb",
                mavenVol = "/home/.m2:/root/.m2",
                tag = "registry/img:v2",
                envScript = "scripts/env.sh",
                runDir = "services/api",
                target = "package",
                makefileFile = "Makefile",
                advancedDockerSettings = "--network host",
            )

        val innerCmd = cmd.last()
        assertTrue(innerCmd.startsWith("source 'scripts/env.sh' &&"))
        assertTrue(innerCmd.contains("cd 'services/api' &&"))
        assertTrue(innerCmd.endsWith("make -r -f 'Makefile' 'package'"))
    }
}
