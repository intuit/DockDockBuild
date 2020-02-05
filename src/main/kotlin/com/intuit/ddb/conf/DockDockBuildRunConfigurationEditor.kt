package com.intuit.ddb.conf

import com.intellij.execution.configuration.EnvironmentVariablesComponent
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiManager
import com.intellij.ui.DocumentAdapter
import com.intellij.ui.TextFieldWithAutoCompletion
import com.intellij.ui.components.fields.ExpandableTextField
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.UIUtil
import com.intuit.ddb.DockerfileFileChooserDescriptor
import name.kropp.intellij.makefile.MakefileFileChooserDescriptor
import name.kropp.intellij.makefile.MakefileTargetIcon
import name.kropp.intellij.makefile.findTargets
import java.awt.GridLayout
import javax.swing.*
import javax.swing.event.DocumentEvent


// This class builds the run conf UI
class DockDockBuildRunConfigurationEditor(private val project: Project) : SettingsEditor<DockDockBuildRunConfiguration>() {

    // create UI elements
    // Docker file\ image buttons
    private val dockerFilenameField = TextFieldWithBrowseButton()
    private val dockerImageField = JTextField("repository:version")
    private val isDockerImageBox = JRadioButton("Docker Image", false)
    private val isDockerfileBox = JRadioButton("Dockerfile", true)
    private val isImageGroup = ButtonGroup()
    private val dockerPanel = dockerPanel()

    private val makeFilenameField = TextFieldWithBrowseButton()

    private val targetCompletionProvider = TextFieldWithAutoCompletion.StringsCompletionProvider(
            emptyList(), MakefileTargetIcon)
    private val targetField = TextFieldWithAutoCompletion<String>(project, targetCompletionProvider,
            true, "")
    private val envScriptPathField = TextFieldWithBrowseButton()

    private val argumentsField = ExpandableTextField()
    private val environmentVarsComponent = EnvironmentVariablesComponent()

    // put UI elements in a panel
    private val panel: JPanel by lazy {
        FormBuilder.createFormBuilder()
                .setAlignLabelOnRight(false)
                .setHorizontalGap(UIUtil.DEFAULT_HGAP)
                .setVerticalGap(UIUtil.DEFAULT_VGAP)
                .addComponent(dockerPanel)
                .addSeparator()
                .addLabeledComponent("&Makefile", makeFilenameField)
                .addLabeledComponent("&Target", targetField)
                .addLabeledComponent("&Environment Script", envScriptPathField)
                .addSeparator()
                .addComponent(environmentVarsComponent)
                .panel
    }

    // add listeners to handle changes
    init {

        dockerFilenameField.addBrowseFolderListener("Dockerfile", "Dockerfile path", project,
                DockerfileFileChooserDescriptor())
        dockerFilenameField.textField.document.addDocumentListener(object : DocumentAdapter() {
            override fun textChanged(event: DocumentEvent) {
                updateTargetCompletion(dockerFilenameField.text)
            }
        })

        isDockerfileBox.addActionListener {
            dockerFilenameField.isEnabled = true
            dockerImageField.isEnabled = false
//            dockerImageField.text = "url:tag"
        }

        isDockerImageBox.addActionListener {
            dockerFilenameField.isEnabled = false
//            dockerFilenameField.text = "dockerfilePath"
            dockerImageField.isEnabled = true
        }

        makeFilenameField.addBrowseFolderListener("Makefile", "Makefile path", project,
                MakefileFileChooserDescriptor())
        makeFilenameField.textField.document.addDocumentListener(object : DocumentAdapter() {
            override fun textChanged(event: DocumentEvent) {
                updateTargetCompletion(makeFilenameField.text)
            }
        })

        envScriptPathField.addBrowseFolderListener("Environment script", "Environment script path",
                project, FileChooserDescriptorFactory.createSingleFileDescriptor("sh"))
        envScriptPathField.textField.document.addDocumentListener(object : DocumentAdapter() {
            override fun textChanged(event: DocumentEvent) {
                updateTargetCompletion(envScriptPathField.text)
            }
        })

    }

    fun updateTargetCompletion(filename: String) {
        val file = LocalFileSystem.getInstance().findFileByPath(filename)
        if (file != null) {
            val psiFile = PsiManager.getInstance(project).findFile(file)
            if (psiFile != null) {
                targetCompletionProvider.setItems(findTargets(psiFile).map { it.name })
                return
            }
        }
        targetCompletionProvider.setItems(emptyList())
    }

    override fun createEditor() = panel

    override fun applyEditorTo(configuration: DockDockBuildRunConfiguration) {
        configuration.dockerfileDir = dockerFilenameField.text
        configuration.dockerImageUrl = dockerImageField.text
        configuration.isDockerImage = isDockerImageBox.isSelected.toString()
        configuration.isDockerfile = isDockerfileBox.isSelected.toString()
        configuration.makefileFilePath = makeFilenameField.text
        configuration.target = targetField.text
        configuration.envScriptPath = envScriptPathField.text
        configuration.environmentVariables = environmentVarsComponent.envData
        configuration.arguments = argumentsField.text
    }

    // fills a form
    override fun resetEditorFrom(configuration: DockDockBuildRunConfiguration) {
        dockerFilenameField.text = configuration.dockerfileDir
        dockerImageField.text = configuration.dockerImageUrl
        isDockerImageBox.isSelected = configuration.isDockerImage == "true"
        isDockerfileBox.isSelected = configuration.isDockerfile == "true"
        dockerImageField.isEnabled = isDockerImageBox.isSelected
        dockerFilenameField.isEnabled = isDockerfileBox.isSelected

        makeFilenameField.text = configuration.makefileFilePath
        targetField.text = configuration.target

        envScriptPathField.text = configuration.envScriptPath
        environmentVarsComponent.envData = configuration.environmentVariables
        argumentsField.text = configuration.arguments

        updateTargetCompletion(configuration.dockerfileDir)
    }

    private fun dockerPanel(): JPanel {

        // put radio buttons in the same group
        isImageGroup.add(isDockerImageBox)
        isImageGroup.add(isDockerfileBox)

        // dockerfile is the default usage
        dockerImageField.isEnabled = isDockerImageBox.isSelected
        dockerFilenameField.isEnabled = isDockerfileBox.isSelected

        // Arrange buttons w/ text browser on a panel
        val radioPanel = JPanel()
        radioPanel.layout = GridLayout(2, 2)
        radioPanel.add(isDockerfileBox, 0)
        radioPanel.add(dockerFilenameField, 1)
        radioPanel.add(isDockerImageBox, 2)
        radioPanel.add(dockerImageField, 3)

        // Add a titled border to the button panel
        radioPanel.border = BorderFactory.createEmptyBorder()
        radioPanel.border = BorderFactory.createTitledBorder(radioPanel.border,
                "Select Docker file or image to run")

        return radioPanel

    }

}