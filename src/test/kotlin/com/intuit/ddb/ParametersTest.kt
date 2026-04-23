package com.intuit.ddb

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ParametersTest {
    @get:Rule
    val tmp = TemporaryFolder()

    private val mapper = ObjectMapper()

    private fun writeParamsFile(json: String): String {
        val file = tmp.newFile("params.json")
        file.writeText(json)
        return file.absolutePath
    }

    @Test
    fun `readParams deserializes all fields`() {
        val path =
            writeParamsFile(
                """
                {
                  "dockerExe": "/usr/bin/docker",
                  "dockerfileDir": "/project/docker",
                  "dockerImgUrl": "my-registry/image:latest",
                  "isImage": true,
                  "makefilePath": "subdir",
                  "makefileFile": "Makefile",
                  "target": "build",
                  "codePath": "/home/user/code",
                  "m2Path": "/home/user/.m2",
                  "envScript": "set_env.sh",
                  "advancedDockerSettings": "--memory 2g"
                }
                """.trimIndent(),
            )

        val p = Parameters.readParams(path)

        assertEquals("/usr/bin/docker", p.dockerExe)
        assertEquals("/project/docker", p.dockerfileDir)
        assertEquals("my-registry/image:latest", p.dockerImgUrl)
        assertEquals(true, p.isImage)
        assertEquals("subdir", p.makefilePath)
        assertEquals("Makefile", p.makefileFile)
        assertEquals("build", p.target)
        assertEquals("/home/user/code", p.codePath)
        assertEquals("/home/user/.m2", p.m2Path)
        assertEquals("set_env.sh", p.envScript)
        assertEquals("--memory 2g", p.advancedDockerSettings)
    }

    @Test
    fun `readParams handles missing optional fields as null`() {
        val path =
            writeParamsFile(
                """
                {
                  "dockerExe": "/usr/bin/docker",
                  "isImage": false
                }
                """.trimIndent(),
            )

        val p = Parameters.readParams(path)

        assertEquals("/usr/bin/docker", p.dockerExe)
        assertEquals(false, p.isImage)
        assertNull(p.envScript)
        assertNull(p.advancedDockerSettings)
        assertNull(p.dockerfileDir)
    }

    @Test
    fun `Parameters round-trips through JSON serialization`() {
        val original =
            Parameters(
                dockerExe = "/usr/bin/docker",
                dockerfileDir = "/project/docker",
                dockerImgUrl = null,
                isImage = false,
                makefilePath = ".",
                makefileFile = "Makefile",
                target = "test",
                codePath = "/code",
                m2Path = "/root/.m2",
                envScript = null,
                advancedDockerSettings = null,
            )

        val file = tmp.newFile("roundtrip.json")
        mapper.writeValue(file, original)
        val restored = Parameters.readParams(file.absolutePath)

        assertEquals(original, restored)
    }
}
