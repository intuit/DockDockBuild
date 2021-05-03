package name.kropp.intellij.makefile

import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.psi.PsiElement
import com.intuit.ddb.DockDockBuildRunTargetAction
import name.kropp.intellij.makefile.psi.MakefileTarget
import name.kropp.intellij.makefile.psi.MakefileTargets
import name.kropp.intellij.makefile.psi.MakefileTypes

class MakefileTargetRunLineMarkerContributor : RunLineMarkerContributor() {
  override fun getInfo(element: PsiElement): Info? {
    if (element.node.elementType == MakefileTypes.IDENTIFIER) {
      val target = element.parent
      if (target is MakefileTarget) {
        val targets = target.parent as MakefileTargets
        val targetList = targets.targetList
        if (targetList.firstOrNull() == target &&
            targetList.any { !it.isSpecialTarget }) {
          // IGNORE ERR
          return Info(MakefileTargetIcon, { "" }, *targetList.map(::DockDockBuildRunTargetAction).toTypedArray())
        }
      }
    }
    return null
  }
}
