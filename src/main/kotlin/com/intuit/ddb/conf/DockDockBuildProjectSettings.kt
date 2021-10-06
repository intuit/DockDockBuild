package com.intuit.ddb.conf

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.StoragePathMacros
import com.intuit.ddb.DockDockBuildSettings

@State(name = "DockDockBuild.Settings", storages = [Storage(StoragePathMacros.WORKSPACE_FILE)])
class DockDockBuildProjectSettings : PersistentStateComponent<DockDockBuildSettings> {
    var settings: DockDockBuildSettings = DockDockBuildSettings()

    override fun getState() = settings

    override fun loadState(state: DockDockBuildSettings) {
        settings = state
    }
}
