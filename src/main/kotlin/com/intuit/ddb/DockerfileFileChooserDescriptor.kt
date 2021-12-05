package com.intuit.ddb

import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.fileChooser.FileElement
import com.intellij.openapi.vfs.VirtualFile

class DockerfileFileChooserDescriptor : FileChooserDescriptor(
    true, false,
    false, false, false, false
) {
    init {
        title = "Dockerfile"
    }

    override fun isFileVisible(file: VirtualFile, showHiddenFiles: Boolean) = when {
        !showHiddenFiles && FileElement.isFileHidden(file) -> false
        file.isDirectory -> true
        else -> file.name.endsWith("") && file.name == "Dockerfile"
    }

    override fun isFileSelectable(file: VirtualFile?): Boolean {
        if (file != null) {
            return !file.isDirectory && isFileVisible(file, true)
        }
        return false
    }
}
