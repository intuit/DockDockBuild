package com.intuit.ddb.conf

import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.actions.LazyRunConfigurationProducer
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiElement
import com.intuit.ddb.getDefaultDockerfileDir
import name.kropp.intellij.makefile.MakefileFile
import name.kropp.intellij.makefile.psi.MakefileTarget
import java.io.File

class DockDockBuildRunConfigurationProducer : LazyRunConfigurationProducer<DockDockBuildRunConfiguration>() {

    override fun getConfigurationFactory(): ConfigurationFactory {
        return DockDockBuildRunConfigurationFactory(DockDockBuildRunConfigurationType)
    }

    // get IntelliJ context and create DockDockBuildRunConfiguration
    override fun setupConfigurationFromContext(
        configuration: DockDockBuildRunConfiguration,
        context: ConfigurationContext,
        sourceElement: Ref<PsiElement>
    ): Boolean {

        if (context.psiLocation?.containingFile !is MakefileFile) {
            return false
        }

        configuration.makefileFilePath = context.location?.virtualFile?.path ?: ""
        configuration.target = findTarget(context)?.name ?: ""
        configuration.dockerfileDir = getDefaultDockerfileDir(configuration.makefileFilePath)

        if (!configuration.target.isNullOrEmpty()) {
            configuration.name = configuration.target
        } else {
            configuration.name = File(configuration.dockerfileDir).name
        }

        return true
    }

    override fun isConfigurationFromContext(
        configuration: DockDockBuildRunConfiguration,
        context: ConfigurationContext
    ): Boolean {

        return configuration.makefileFilePath == context.location?.virtualFile?.path &&
            configuration.target == findTarget(context)?.name &&
            configuration.dockerfileDir == getDefaultDockerfileDir(configuration.makefileFilePath)
    }

    private fun findTarget(context: ConfigurationContext): MakefileTarget? {
        var element = context.psiLocation
        while (element != null && element !is MakefileTarget) {
            element = element.parent
        }
        val target = element as? MakefileTarget
        if (target?.isSpecialTarget == false) {
            return target
        }
        return null
    }
}
