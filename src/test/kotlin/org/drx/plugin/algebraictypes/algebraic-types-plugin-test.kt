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

import org.drx.plugin.algebraictypes.config.Config
import org.drx.plugin.algebraictypes.generate.license
import org.drx.plugin.algebraictypes.task.GenerateTypes
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.kotlin.dsl.withConvention
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.IOException


class KotlinAlgebraicTypesPluginTest {

    @Test
    fun pluginAddsTasksToProject() {
        val project: Project = ProjectBuilder.builder().build()
        project.pluginManager.apply (KotlinAlgebraicTypesPlugin::class.java)

        assert(project.tasks.getByName("generateAlgebraicTypes") is GenerateTypes)
    }


    @Test
    fun pluginAddsGeneratedSourceSetAndAddsOutputToClasspathOfMain() {
        val project: Project = ProjectBuilder.builder().build()
        project.pluginManager.apply(KotlinAlgebraicTypesPlugin::class.java)



        //val s = JavaPluginConvention::class.qualifiedName
        project.withConvention(JavaPluginConvention::class){
            //val s = sourceSets["generated"]

        }
/*
            assert(m.compileClasspath.files.map{it.absolutePath}.filter { it.endsWith("generated") }.size == 2)
            assert(m.runtimeClasspath.files.map{it.absolutePath}.filter { it.endsWith("generated") }.size == 2)
            */

    }

    @Test
    fun  testAlgebraicTypesExtension() {
        val project: Project = ProjectBuilder.builder().build()
        val projectDir = project.projectDir

        project.pluginManager.apply (KotlinAlgebraicTypesPlugin::class.java)

        project.algebraicTypes {
            products {
                dimension(5)
            }

            dataClasses {
                dataClass {
                    name = "Test"
                    packageName = "org.anisat.ion"
                    sourceFolder = "src/"
                    parameter {
                        name = "x"
                        type{
                            name = "Int"
                        }
                    }
                }
            }
        }


    }



    @Test
    fun testLicense(){
        println(license())
    }
}

public class FunctionalTest {
    @Rule @JvmField  val testProjectDir: TemporaryFolder = TemporaryFolder()
    private var settingsFile: File? = null
    private var buildFile: File? = null

    @Before
    @Throws(IOException::class)
    fun setup() {
        settingsFile = testProjectDir.newFile("settings.gradle.kts")
        buildFile = testProjectDir.newFile("build.gradle.kts")
    }

    @Test fun test() {

        settingsFile?.writeText("rootProject.name = \"test-project\"");
        val buildFileContent: String =
                TestConfig.BuildFile.initContentLocalRepo +
                "algebraicTypes {\n" +
                "    products {\n" +
                "       dimension(5)\n" +
                "    }\n" +
                "    dualities {\n" +
                "       dimension(5)\n" +
                "    }\n" +
                "}\n"

        buildFile?.writeText(buildFileContent);

        val result: BuildResult = GradleRunner . create ()
                .withProjectDir(testProjectDir.root)
                .withArguments(Config.Tasks.generateAlgebraicTypes)
                .build();

        assert(File(testProjectDir.root,"${Config.GeneratedSources.productsFolder}/product-5.kt").exists())
        assert(File(testProjectDir.root,"${Config.GeneratedSources.sumsFolder}/sum-5.kt").exists())
        assert(File(testProjectDir.root,"${Config.GeneratedSources.dualitiesFolder}/duality-5.kt").exists())
    }

    @Test fun idSetterTest() {
        fun <T> idSetter(): T.()->T = {this}
        assert(idSetter<Int>()(1) == 1)
    }

    @Test fun testDataClassGeneration() {

        settingsFile?.writeText("rootProject.name = \"test-project\"")
        val sourceFolder = "src/module/main/kotlin"
        val packageName = "org.drx.test"
        val packageFolder = "org/drx/test"
        val buildFileContent: String =
                """${TestConfig.BuildFile.initContentLocalRepo}
                    |
                    |algebraicTypes{
                    |   dataClasses{
                    |       dataClass{
                    |           name = "TestData"
                    |           packageName = "$packageName"
                    |           sourceFolder = "$sourceFolder"
                    |           parameter{
                    |               name = "x"
                    |               type {
                    |                   name = "Int"
                    |               }
                    |           }  
                    |           parameter{
                    |               name = "y"
                    |               defaultValue = "arrayListOf()"
                    |               type {
                    |                   name = "ArrayList<Y>"
                    |                   isGeneric = true
                    |                   genericIn = "Y"
                    |               }
                    |           } 
                    |            parameter{
                    |               name = "z"
                    |               type {
                    |                   name = "String"
                    |               }
                    |           }  
                    |       }   
                    |   }
                    |}
                """.trimMargin()

        buildFile?.writeText(buildFileContent);

        val result: BuildResult = GradleRunner . create ()
                .withProjectDir(testProjectDir.root)
                .withArguments(Config.Tasks.generateAlgebraicTypes)
                .build();

        assert(File(testProjectDir.root,"${Config.GeneratedSources.DataClasses.folderName}/functions.kt").exists())
        println(File(testProjectDir.root,"${Config.GeneratedSources.DataClasses.folderName}/functions.kt").readText())

        assert(File(testProjectDir.root,"${Config.GeneratedSources.productsFolder}/product-2.kt").exists())
        assert(File(testProjectDir.root,"${Config.GeneratedSources.productsFolder}/product-3.kt").exists())


        assert(File(testProjectDir.root,"$sourceFolder/$packageFolder/TestData.kt").exists())
        println(File(testProjectDir.root,"$sourceFolder/$packageFolder/TestData.kt").readText())
    }


    @Test fun sealedClassGeneration() {
        settingsFile?.writeText("rootProject.name = \"test-project\"")
        val sourceFolder = "src/module/main/kotlin"
        val packageName = "org.drx.test"
        val packageFolder = "org/drx/test"
        val buildFileContent: String =
                """${TestConfig.BuildFile.initContentLocalRepo}
                    |
                    |algebraicTypes{
                    |   dataClasses{
                    |      sealedClass {
                    |         name = "TestData"
                    |         packageName = "$packageName"
                    |         sourceFolder = "$sourceFolder" 
                    |         dataRepresentative {
                    |               name = "One"
                    |               parameter {
                    |                   name = "x"
                    |                   type {
                    |                       name = "Int"
                    |                   }
                    |               }
                    |         }
                    |         dataRepresentative {
                    |               name = "Two"
                    |               parameter {
                    |                   name = "x"
                    |                   type {
                    |                       name = "Int"
                    |                   }
                    |               }
                    |               parameter {
                    |                   name = "y"
                    |                   type {
                    |                       name = "String"
                    |                   }
                    |               }
                    |               parameter {
                    |                   name = "z"
                    |                   type {
                    |                       name = "Boolean"
                    |                   }
                    |               }
                    |         }
                    |         dataRepresentative {
                    |               name = "Three"
                    |               parameter {
                    |                   name = "z"
                    |                   type {
                    |                       name = "Boolean"
                    |                   }
                    |               }
                    |         }
                    |      }   
                    |   }
                    |}
                """.trimMargin()

        buildFile?.writeText(buildFileContent);

        val result: BuildResult = GradleRunner . create ()
                .withProjectDir(testProjectDir.root)
                .withArguments(Config.Tasks.generateAlgebraicTypes)
                .build();

        // We expect files:
        // sum-3
        // product-1,product-2,product-3
        // file containing the generated prism

        assert(File(testProjectDir.root,"${Config.GeneratedSources.Sums.folderName}/sum-3.kt").exists())

        assert(File(testProjectDir.root,"${Config.GeneratedSources.productsFolder}/product-1.kt").exists())
        assert(File(testProjectDir.root,"${Config.GeneratedSources.productsFolder}/product-2.kt").exists())
        assert(File(testProjectDir.root,"${Config.GeneratedSources.productsFolder}/product-3.kt").exists())

        assert(File(testProjectDir.root,"$sourceFolder/$packageFolder/TestData.kt").exists())
        println(File(testProjectDir.root,"$sourceFolder/$packageFolder/TestData.kt").readText())
    }
}