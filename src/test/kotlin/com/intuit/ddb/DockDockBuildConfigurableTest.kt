package com.intuit.ddb

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intuit.ddb.conf.DockDockBuildProjectSettings

class DockDockBuildConfigurableTest : BasePlatformTestCase() {
    private fun configurable() = DockDockBuildConfigurable(project)

    private fun settings() = project.getService(DockDockBuildProjectSettings::class.java).settings

    fun testCreateComponentReturnsNonNullPanel() {
        assertNotNull(configurable().createComponent())
    }

    fun testGetDisplayNameReturnsPluginName() {
        assertEquals(PLUGIN_NAME, configurable().getDisplayName())
    }

    fun testIsModifiedFalseAfterReset() {
        val configurable = configurable()
        configurable.createComponent()
        configurable.reset()
        assertFalse(configurable.isModified)
    }

    fun testResetPopulatesFieldsFromSettings() {
        settings().dockerPath = "/custom/docker"
        settings().codePath = "/custom/code"
        settings().mavenCachePath = "/custom/.m2"
        settings().advancedDockerSettings = "--memory 4g"

        val configurable = configurable()
        configurable.createComponent()
        configurable.reset()

        assertFalse(configurable.isModified)
    }

    fun testApplyRoundTripPreservesValues() {
        settings().dockerPath = "/usr/bin/docker"
        settings().codePath = "/home/user/project"
        settings().mavenCachePath = "/home/user/.m2"
        settings().advancedDockerSettings = ""

        val configurable = configurable()
        configurable.createComponent()
        configurable.reset()
        configurable.apply()

        assertEquals("/usr/bin/docker", settings().dockerPath)
        assertEquals("/home/user/project", settings().codePath)
        assertEquals("/home/user/.m2", settings().mavenCachePath)
        assertEquals("", settings().advancedDockerSettings)
    }
}
