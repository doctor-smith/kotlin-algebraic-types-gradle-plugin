/**
 * Copyright (c) 2019 Dr. Florian Schmidt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.drx.plugin.algebraictypes

import org.drx.plugin.algebraictypes.generate.license
import org.drx.plugin.algebraictypes.task.GenerateTypes
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.withConvention
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

class KotlinAlgebraicTypesPluginTest {

    @Test
    fun pluginAddsTasksToProject() {
        val project: Project = ProjectBuilder.builder().build()
        project.pluginManager.apply (KotlinAlgebraicTypesPlugin::class.java)

        assert(project.tasks.getByName("generateAlgebraicTypes") is GenerateTypes)

        /*assert(project.tasks.getByName("generateSumTypes") is GenerateSumTypes)
        assert(project.tasks.getByName("generateProductType") is GenerateProductType)
        assert(project.tasks.getByName("generateProductTypes") is GenerateProductTypes)

        assert(project.tasks.getByName("generateDuality") is GenerateDuality)
        assert(project.tasks.getByName("generateDualities") is GenerateDualities)
        assert(project.tasks.getByName("generateProductTypeArithmetic") is GenerateProductTypeArithmetic)
        assert(project.tasks.getByName("generateProductTypeArithmetics") is GenerateProductTypeArithmetics)
        assert(project.tasks.getByName("generateEvoleqProduct") is GenerateEvoleqProduct)
        assert(project.tasks.getByName("generateEvoleqProducts") is GenerateEvoleqProducts)
        assert(project.tasks.getByName("generateEvoleqSum") is GenerateEvoleqSum)
        assert(project.tasks.getByName("generateEvoleqSums") is GenerateEvoleqSums)


         */
    }


    @Test
    fun pluginAddsGeneratedSourceSetAndAddsOutputToClasspathOfMain() {
        val project: Project = ProjectBuilder.builder().build()
        project.pluginManager.apply(KotlinAlgebraicTypesPlugin::class.java) //("org.drx.kotlin-algebraic-types-plugin")

        //val s = JavaPluginConvention::class.qualifiedName
        project.withConvention(JavaPluginConvention::class){
            //val s = sourceSets["generated"]
            val m = sourceSets["main"]
            assert(m.compileClasspath.files.map{it.absolutePath}.filter { it.endsWith("generated") }.size == 2)
            assert(m.runtimeClasspath.files.map{it.absolutePath}.filter { it.endsWith("generated") }.size == 2)

        }


    }

    //@Test
    fun  testAlgebraicTypesExtension() {
        val project: Project = ProjectBuilder.builder().build()

        project.pluginManager.apply (KotlinAlgebraicTypesPlugin::class.java)



    }

    @Test
    fun testLicense(){
        println(license())
    }
}