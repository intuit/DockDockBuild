package name.kropp.intellij.makefile

import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.psi.PsiElement
import name.kropp.intellij.makefile.psi.MakefileTarget

class MakefileDocumentationProvider : AbstractDocumentationProvider() {
    override fun getQuickNavigateInfo(element: PsiElement, originalElement: PsiElement?) = (element as? MakefileTarget)?.docComment
    override fun generateDoc(element: PsiElement?, originalElement: PsiElement?) = (element as? MakefileTarget)?.docComment
}
