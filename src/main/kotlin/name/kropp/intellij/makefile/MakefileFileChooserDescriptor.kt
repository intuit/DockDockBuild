package name.kropp.intellij.makefile

import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.fileChooser.FileElement
import com.intellij.openapi.vfs.VirtualFile

class MakefileFileChooserDescriptor : FileChooserDescriptor(true, false, false, false, false, false) {
    init {
        title = "Makefile"
    }

    override fun isFileVisible(file: VirtualFile, showHiddenFiles: Boolean) = when {
        !showHiddenFiles && FileElement.isFileHidden(file) -> false
        file.isDirectory -> true
        else -> file.name.endsWith(".mk") || file.name == "Makefile"
    }

    override fun isFileSelectable(file: VirtualFile?): Boolean {
        if (file != null) {
            return !file.isDirectory && isFileVisible(file, true)
        }
        return false
    }
}
