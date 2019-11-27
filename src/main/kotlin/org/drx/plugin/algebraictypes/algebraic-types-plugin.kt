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

import org.drx.plugin.algebraictypes.task.GenerateTypes
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.kotlin.dsl.*
import java.io.File

val basePath= "/src/generated/kotlin/org/drx/generated"



class KotlinAlgebraicTypesPlugin: Plugin<Project> {
    override fun apply(target: Project) {
        target.apply<JavaPlugin>()
        target.apply<JavaBasePlugin>()

        val dir = File("${target.projectDir}$basePath")
        if(!dir.exists()) {
            dir.mkdirs()
        }



        val extension = target.extensions.create<AlgebraicTypesExtension>("algebraicTypes")
        (extension as ExtensionAware).extensions.create<DimensionSelectionExtension>("dimensionSelection")
        (extension as ExtensionAware).extensions.create<OutputExtension>("outputSelection")
        val keysExtension = (extension as ExtensionAware).extensions.create<KeysExtension>("keys")
        (keysExtension as ExtensionAware).extensions.create<OutputExtension>("addKeys")

        val generateTypes = target.tasks.create("generateAlgebraicTypes", GenerateTypes::class.java)
        generateTypes.group = "algebraic types"
        //generateTypes.generate()

        target.afterEvaluate {
            configure<JavaPluginConvention> {
                if (sourceSets.findByName("generated") == null) {
                    sourceSets.create("generated") {
                        java.srcDirs("src/generated/java", "src/generated/kotlin")
                    }
                }
                extension.outputs.compile.forEach {
                    if (sourceSets.findByName(it) == null) {
                        sourceSets.create(it) {
                            java.srcDirs("src/$it/java", "src/$it/kotlin")
                        }
                    }
                    sourceSets.getByName(it) {
                        java {
                            compileClasspath += sourceSets["generated"].output
                        }
                    }
                }
                extension.outputs.runtime.forEach {
                    if (sourceSets.findByName(it) == null) {
                        sourceSets.create(it) {
                            java.srcDirs("src/$it/java", "src/$it/kotlin")
                        }
                    }
                    sourceSets.getByName(it) {
                        java {
                            runtimeClasspath += sourceSets["generated"].output
                        }

                    }
                }
                extension.outputs.test.forEach {
                    if (sourceSets.findByName(it) == null) {
                        sourceSets.create(it) {
                            java.srcDirs("src/$it/java", "src/$it/kotlin")
                        }
                    }
                    sourceSets.getByName(it) {
                        java {
                            compileClasspath += sourceSets["generated"].output
                            runtimeClasspath += sourceSets["generated"].output
                        }
                    }
                }
            }

        }

/*
        target.tasks.create("generateSumType", GenerateSumType::class.java)
        target.tasks.create("generateSumTypes", GenerateSumTypes::class.java)
        target.tasks.create("generateProductType", GenerateProductType::class.java)
        target.tasks.create("generateProductTypes", GenerateProductTypes::class.java)
        target.tasks.create("generateDuality", GenerateDuality::class.java)
        target.tasks.create("generateDualities", GenerateDualities::class.java)
        target.tasks.create("generateProductTypeArithmetic", GenerateProductTypeArithmetic::class.java)
        target.tasks.create("generateProductTypeArithmetics", GenerateProductTypeArithmetics::class.java)
        target.tasks.create("generateEvoleqProduct", GenerateEvoleqProduct::class.java)
        target.tasks.create("generateEvoleqProducts", GenerateEvoleqProducts::class.java)
        target.tasks.create("generateEvoleqSum", GenerateEvoleqSum::class.java)
        target.tasks.create("generateEvoleqSums", GenerateEvoleqSums::class.java)

 */
    }
}



