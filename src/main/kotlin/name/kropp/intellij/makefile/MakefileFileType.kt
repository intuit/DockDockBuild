package name.kropp.intellij.makefile

import com.intellij.icons.AllIcons
import com.intellij.openapi.fileTypes.*
import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

val MakefileIcon = IconLoader.getIcon("/ddb/icon/DockDockBuild15pxl.png")
val MakefileTargetIcon = AllIcons.RunConfigurations.TestState.Run!!

class MakefileFileType : LanguageFileType(MakefileLanguage) {

  override fun getName(): String {
    return "Makefile"
  }

  override fun getDescription(): String {
    return "GNU Makefile"
  }

  override fun getDefaultExtension(): String {
    return "mk"
  }

  override fun getIcon(): Icon? {
    return MakefileIcon
  }

  companion object Factory {
    val INSTANCE = MakefileFileType()
  }
}

