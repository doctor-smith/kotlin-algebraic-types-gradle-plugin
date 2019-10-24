package org.drx.plugin.algebraictypes

import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet
import org.gradle.kotlin.dsl.container
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.withConvention
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

class KotlinAlgebraicTypesPluginTest {

    @Test
    fun pluginAddsTasksToProject() {
        val project: Project = ProjectBuilder.builder().build()
        project.pluginManager.apply ("org.drx.algebraic-types-plugin")

        assert(project.tasks.getByName("generateSumType") is GenerateSumType)
        assert(project.tasks.getByName("generateSumTypes") is GenerateSumTypes)
        assert(project.tasks.getByName("generateProductType") is GenerateProductType)
        assert(project.tasks.getByName("generateProductTypes") is GenerateProductTypes)

    }

    @Test
    fun pluginAddsGeneratedSourceSetAndAddsOutputToClasspathOfMain() {
        val project: Project = ProjectBuilder.builder().build()
        project.pluginManager.apply ("org.drx.algebraic-types-plugin")

        //val s = JavaPluginConvention::class.qualifiedName
        project.withConvention(JavaPluginConvention::class){
            //val s = sourceSets["generated"]
            val m = sourceSets["main"]
            assert(m.compileClasspath.files.map{it.absolutePath}.filter { it.endsWith("generated") }.size == 2)
            assert(m.runtimeClasspath.files.map{it.absolutePath}.filter { it.endsWith("generated") }.size == 2)

        }
    }

    @Test
    fun testLicense(){
        println(license())
    }
}