package com.intuit.ddb.conf

import com.intellij.execution.configurations.ConfigurationType
import name.kropp.intellij.makefile.MakefileIcon

object DockDockBuildRunConfigurationType : ConfigurationType {
    override fun getDisplayName() = "DockDockBuild"
    override fun getIcon() = MakefileIcon
    override fun getConfigurationTypeDescription() = "DockDockBuild"

    override fun getId() = "DockDockBuildRunConfiguration"

    override fun getConfigurationFactories() = arrayOf(DockDockBuildRunConfigurationFactory(DockDockBuildRunConfigurationType))
}