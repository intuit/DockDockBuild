package com.intuit.ddb

import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.uiDesigner.core.Spacer
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.UIUtil
import com.intuit.ddb.conf.DockDockBuildProjectSettings
import javax.swing.JComponent
import javax.swing.JTextField

// Docker Make project config
class DockDockBuildConfigurable(project: Project?) : Configurable {

    private val settings = DockDockBuildProjectSettings()
    private val dockerPathField = TextFieldWithBrowseButton()
    private val codePathField = TextFieldWithBrowseButton()
    private val mavenCachePathField = TextFieldWithBrowseButton()
    private val advancedDockerSettingsField = JTextField()
    private val defaultCodePath = getDefaultCodePath(project!!)

    init {
        dockerPathField.addBrowseFolderListener(PLUGIN_NAME, "Path to Docker executable", project,
                FileChooserDescriptor(true, false, false, false, false, false))
        codePathField.addBrowseFolderListener(PLUGIN_NAME, "Path to code root", project,
                FileChooserDescriptor(false, true, false, false, false, false))
        mavenCachePathField.addBrowseFolderListener(PLUGIN_NAME, "Path to Maven cache", project,
                FileChooserDescriptor(false, true, false, false, false, false))
    }

    override fun isModified(): Boolean {
        return (settings.settings?.dockerPath != dockerPathField.text) ||
                (settings.settings?.codePath != codePathField.text) ||
                (settings.settings?.mavenCachePath != mavenCachePathField.text) ||
                (settings.settings?.advancedDockerSettings != advancedDockerSettingsField.text)
    }

    override fun getDisplayName() = PLUGIN_NAME

    override fun apply() {
        settings.settings?.dockerPath = dockerPathField.text
        settings.settings?.codePath = codePathField.text
        settings.settings?.mavenCachePath = mavenCachePathField.text
        settings.settings?.advancedDockerSettings = advancedDockerSettingsField.text
    }

    override fun createComponent(): JComponent {
        return FormBuilder.createFormBuilder()
                .setAlignLabelOnRight(false)
                .setHorizontalGap(UIUtil.DEFAULT_HGAP)
                .setVerticalGap(UIUtil.DEFAULT_VGAP)
                .addLabeledComponent("Path to &Docker executable", dockerPathField)
                .setHorizontalGap(UIUtil.DEFAULT_HGAP)
                .addLabeledComponent("Path to code root", codePathField)
                .setHorizontalGap(UIUtil.DEFAULT_HGAP)
                .addLabeledComponent("Path to Maven cache", mavenCachePathField)
                .setHorizontalGap(UIUtil.DEFAULT_HGAP)
                .addLabeledComponent("Advanced docker settings", advancedDockerSettingsField)
                .addTooltip("Use with caution!")
                .addComponentFillVertically(Spacer(), 0)
                .panel
    }

    override fun reset() {
        dockerPathField.text = settings.settings?.dockerPath ?: getDefaultDockerPath()
        codePathField.text = settings.settings?.codePath ?: defaultCodePath
        mavenCachePathField.text = settings.settings?.mavenCachePath ?: getDefaultM2Path()
        advancedDockerSettingsField.text = settings.settings?.advancedDockerSettings ?: ""
    }

    override fun getHelpTopic() = null
}