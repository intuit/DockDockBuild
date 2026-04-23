package com.intuit.ddb

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.fixtures.CommonContainerFixture
import com.intellij.remoterobot.fixtures.ComponentFixture
import com.intellij.remoterobot.search.locators.byXpath
import com.intellij.remoterobot.utils.waitFor
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration

/**
 * UI tests for DockDockBuild plugin.
 *
 * Requires a running IDE instance with the robot server plugin loaded.
 * Start it first with: ./gradlew runIdeForUiTests
 * Then run: ./gradlew uiTest
 */
class DockDockBuildUiTest {
    private val robot = RemoteRobot("http://127.0.0.1:8082")

    /** Verifies the IDE starts up and is responsive. */
    @Test
    fun testIdeStartsUp() {
        waitFor(Duration.ofSeconds(30)) {
            robot.findAll<ComponentFixture>(byXpath("//div[@class='IdeFrameImpl']")).isNotEmpty()
        }
        val frame =
            robot.find<CommonContainerFixture>(
                byXpath("//div[@class='IdeFrameImpl']"),
                Duration.ofSeconds(10),
            )
        assertNotNull(frame)
    }

    /**
     * Verifies the DockDockBuild run configuration type appears
     * in the Run/Debug Configurations dialog.
     */
    @Test
    fun testRunConfigurationTypeRegistered() {
        waitForIdeFrame()

        val menuBar =
            robot.find<CommonContainerFixture>(
                byXpath("//div[@class='IdeFrameImpl']"),
                Duration.ofSeconds(10),
            )
        val runMenu =
            menuBar.find<ComponentFixture>(
                byXpath("//div[@text='Run']"),
                Duration.ofSeconds(5),
            )
        runMenu.click()

        waitFor(Duration.ofSeconds(5)) {
            robot.findAll<ComponentFixture>(byXpath("//div[@text='Edit Configurations...']")).isNotEmpty()
        }
        robot.find<ComponentFixture>(
            byXpath("//div[@text='Edit Configurations...']"),
            Duration.ofSeconds(5),
        ).click()

        waitFor(Duration.ofSeconds(10)) {
            robot.findAll<ComponentFixture>(byXpath("//div[@title='Run/Debug Configurations']")).isNotEmpty()
        }
        val dialog =
            robot.find<CommonContainerFixture>(
                byXpath("//div[@title='Run/Debug Configurations']"),
                Duration.ofSeconds(10),
            )

        dialog.find<ComponentFixture>(
            byXpath("//div[@tooltiptext='Add New Run Configuration' or @tooltiptext='Add New Configuration']"),
            Duration.ofSeconds(5),
        ).click()

        waitFor(Duration.ofSeconds(5)) {
            robot.findAll<ComponentFixture>(byXpath("//div[contains(@text, 'DockDockBuild')]")).isNotEmpty()
        }
        val configType =
            robot.find<ComponentFixture>(
                byXpath("//div[contains(@text, 'DockDockBuild')]"),
                Duration.ofSeconds(5),
            )
        assertNotNull(configType)

        robot.find<ComponentFixture>(
            byXpath("//div[@text='Cancel']"),
            Duration.ofSeconds(5),
        ).click()
    }

    /**
     * Verifies the DockDockBuild settings panel is accessible
     * under Build Tools in the project settings.
     */
    @Test
    fun testSettingsPanelAccessible() {
        waitForIdeFrame()

        robot.find<ComponentFixture>(
            byXpath("//div[@class='IdeFrameImpl']"),
        ).runJs("robot.openSettingsDialog()")

        waitFor(Duration.ofSeconds(10)) {
            robot.findAll<ComponentFixture>(
                byXpath("//div[@title='Settings' or @title='Preferences']"),
            ).isNotEmpty()
        }
        val settingsDialog =
            robot.find<CommonContainerFixture>(
                byXpath("//div[@title='Settings' or @title='Preferences']"),
                Duration.ofSeconds(10),
            )
        assertNotNull(settingsDialog)

        val searchField =
            settingsDialog.find<ComponentFixture>(
                byXpath("//div[@class='SearchTextField' or @class='MyTextField']"),
                Duration.ofSeconds(5),
            )
        searchField.click()
        searchField.runJs("component.setText('DockDockBuild')")

        waitFor(Duration.ofSeconds(5)) {
            settingsDialog.findAll<ComponentFixture>(
                byXpath("//div[contains(@text, 'DockDockBuild')]"),
            ).isNotEmpty()
        }
        val settingsEntry =
            settingsDialog.findAll<ComponentFixture>(
                byXpath("//div[contains(@text, 'DockDockBuild')]"),
            )
        assertTrue("DockDockBuild settings entry not found", settingsEntry.isNotEmpty())

        settingsDialog.find<ComponentFixture>(
            byXpath("//div[@text='Cancel']"),
            Duration.ofSeconds(5),
        ).click()
    }

    private fun waitForIdeFrame() {
        waitFor(Duration.ofSeconds(30)) {
            robot.findAll<ComponentFixture>(byXpath("//div[@class='IdeFrameImpl']")).isNotEmpty()
        }
    }
}
