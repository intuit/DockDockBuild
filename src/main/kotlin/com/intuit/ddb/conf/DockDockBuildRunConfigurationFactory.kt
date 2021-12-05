package com.intuit.ddb.conf

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.RunConfigurationSingletonPolicy
import com.intellij.openapi.project.Project
import com.intuit.ddb.PLUGIN_NAME

class DockDockBuildRunConfigurationFactory(runConfigurationType: DockDockBuildRunConfigurationType) :
    ConfigurationFactory(runConfigurationType) {

    override fun createTemplateConfiguration(project: Project) = DockDockBuildRunConfiguration(
        project, this, PLUGIN_NAME
    )

    override fun getSingletonPolicy() = RunConfigurationSingletonPolicy.SINGLE_INSTANCE_ONLY

    override fun getId() = "DockDockBuildRunConfiguration"
}
