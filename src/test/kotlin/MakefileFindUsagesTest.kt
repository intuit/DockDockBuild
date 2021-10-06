import com.intellij.codeInsight.TargetElementUtil
import com.intellij.find.FindManager
import com.intellij.find.impl.FindManagerImpl
import org.hamcrest.collection.IsCollectionWithSize.hasSize
import org.hamcrest.core.IsNull.nullValue
import org.junit.Assert.assertThat

class MakefileFindUsagesTest : DockDockTestCase() {
    fun testSimple() {
        val usages = myFixture.testFindUsages("$basePath/${getTestName(true)}.mk")

        assertThat(usages, hasSize(2))
    }

    fun testPhony() = notSearchableForUsages()
    fun testForce() = notSearchableForUsages()

    fun notSearchableForUsages() {
        myFixture.configureByFiles("$basePath/${getTestName(true)}.mk")
        val targetElement = TargetElementUtil.findTargetElement(myFixture.editor, TargetElementUtil.ELEMENT_NAME_ACCEPTED or TargetElementUtil.REFERENCED_ELEMENT_ACCEPTED)
        val handler = (FindManager.getInstance(project) as FindManagerImpl).findUsagesManager.getFindUsagesHandler(targetElement!!, false)

        assertThat(handler, nullValue())
    }

    override fun getBasePath() = "findUsages"
}
