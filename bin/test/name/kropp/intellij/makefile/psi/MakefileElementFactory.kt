package name.kropp.intellij.makefile.psi

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiWhiteSpace
import name.kropp.intellij.makefile.MakefileFile
import name.kropp.intellij.makefile.MakefileFileType

object MakefileElementFactory {
    fun createFile(project: Project, text: String) =
        PsiFileFactory.getInstance(project).createFileFromText("Makefile", MakefileFileType.INSTANCE, text) as MakefileFile

    fun createRule(project: Project, target: String) =
        createFile(project, "$target:\n").firstChild as MakefileRule

    fun createTarget(project: Project, name: String) =
        createRule(project, name).firstChild.firstChild.firstChild as MakefileTarget

    fun createPrerequisite(project: Project, name: String) =
        (createFile(project, "a: $name").firstChild as MakefileRule).targetLine.prerequisites!!.normalPrerequisites.firstChild as MakefilePrerequisite

    fun createWhiteSpace(project: Project, whitespace: String) =
        createFile(project, whitespace).firstChild as PsiWhiteSpace
}
