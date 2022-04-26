package com.intuit.ddb

import com.intellij.execution.Executor
import com.intellij.execution.Location
import com.intellij.execution.PsiLocation
import com.intellij.execution.RunManagerEx
import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.runners.ExecutionUtil
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intuit.ddb.conf.DockDockBuildRunConfigurationProducer
import name.kropp.intellij.makefile.MakefileTargetIcon
import name.kropp.intellij.makefile.psi.MakefileTarget

// This class creates the context and runs the plugin when an event was received
class DockDockBuildRunTargetAction(private val target: MakefileTarget) : AnAction(
    PLUGIN_NAME + " ${target.name}",
    PLUGIN_NAME + " ${target.name}", MakefileTargetIcon
) {

    override fun actionPerformed(event: AnActionEvent) {

        val dataContext = SimpleDataContext.getSimpleContext(Location.DATA_KEY, PsiLocation(target), event.dataContext)

        val context = ConfigurationContext.getFromContext(dataContext)

        val producer = DockDockBuildRunConfigurationProducer()
        val configuration = producer.findOrCreateConfigurationFromContext(context)?.configurationSettings ?: return

        (context.runManager as RunManagerEx).setTemporaryConfiguration(configuration)
        ExecutionUtil.runConfiguration(configuration, Executor.EXECUTOR_EXTENSION_NAME.extensionList.first())
    }
}
