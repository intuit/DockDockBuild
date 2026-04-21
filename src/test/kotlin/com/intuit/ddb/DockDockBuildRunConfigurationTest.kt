package com.intuit.ddb

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intuit.ddb.conf.DockDockBuildRunConfiguration
import com.intuit.ddb.conf.DockDockBuildRunConfigurationEditor
import com.intuit.ddb.conf.DockDockBuildRunConfigurationFactory
import com.intuit.ddb.conf.DockDockBuildRunConfigurationType
import org.jdom.Element

class DockDockBuildRunConfigurationTest : BasePlatformTestCase() {
    private fun factory() = DockDockBuildRunConfigurationFactory(DockDockBuildRunConfigurationType)

    private fun config(name: String = "test") = DockDockBuildRunConfiguration(project, factory(), name)

    // ---- basic properties ----

    fun testDefaultFieldValuesAreEmptyStrings() {
        val config = config()
        assertEquals("", config.makefileFilePath)
        assertEquals("", config.dockerfileDir)
        assertEquals("", config.dockerImageUrl)
        assertFalse(config.isDockerImage)
        assertEquals("", config.target)
        assertEquals("", config.envScriptPath)
    }

    fun testGetConfigurationEditorReturnsEditorInstance() {
        assertInstanceOf(config().getConfigurationEditor(), DockDockBuildRunConfigurationEditor::class.java)
    }

    // ---- writeExternal / readExternal round-trip ----

    fun testWriteAndReadExternalRoundTripPreservesAllFields() {
        val original = config("my-run")
        original.makefileFilePath = "/project/Makefile"
        original.dockerfileDir = "/project/docker"
        original.dockerImageUrl = "registry/image:v1"
        original.isDockerImage = false
        original.isDockerfile = true
        original.target = "build"
        original.envScriptPath = "/project/set_env.sh"

        val element = Element("configuration")
        original.writeExternal(element)

        val restored = config("my-run")
        restored.readExternal(element)

        assertEquals(original.makefileFilePath, restored.makefileFilePath)
        assertEquals(original.dockerfileDir, restored.dockerfileDir)
        assertEquals(original.dockerImageUrl, restored.dockerImageUrl)
        assertEquals(original.isDockerImage, restored.isDockerImage)
        assertEquals(original.isDockerfile, restored.isDockerfile)
        assertEquals(original.target, restored.target)
        assertEquals(original.envScriptPath, restored.envScriptPath)
    }

    fun testReadExternalOnEmptyElementLeavesDefaults() {
        val config = config()
        config.readExternal(Element("configuration"))
        assertEquals("", config.makefileFilePath)
        assertEquals("", config.target)
    }

    // ---- editor applyEditorTo / resetEditorFrom round-trip ----

    fun testEditorResetThenApplyPreservesValues() {
        val config = config()
        config.makefileFilePath = "/project/Makefile"
        config.dockerfileDir = "/project/docker"
        config.dockerImageUrl = ""
        config.isDockerImage = false
        config.isDockerfile = true
        config.target = "package"
        config.envScriptPath = ""

        val editor = DockDockBuildRunConfigurationEditor(project)
        editor.resetFrom(config)

        val output = config("output")
        editor.applyTo(output)

        assertEquals("/project/Makefile", output.makefileFilePath)
        assertEquals("/project/docker", output.dockerfileDir)
        assertEquals("package", output.target)
        assertFalse(output.isDockerImage)
        assertTrue(output.isDockerfile)
    }

    fun testEditorDockerImageModePreservedThroughRoundTrip() {
        val config = config()
        config.dockerImageUrl = "my-registry/image:latest"
        config.isDockerImage = true
        config.isDockerfile = false
        config.makefileFilePath = "/project/Makefile"
        config.target = "run"

        val editor = DockDockBuildRunConfigurationEditor(project)
        editor.resetFrom(config)

        val output = config("output")
        editor.applyTo(output)

        assertEquals("my-registry/image:latest", output.dockerImageUrl)
        assertTrue(output.isDockerImage)
        assertFalse(output.isDockerfile)
    }
}
