/**
 * Copyright (c) 2018-2019 Dr. Florian Schmidt
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

import org.apache.tools.ant.taskdefs.Java
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.kotlin.dsl.*
import org.gradle.kotlin.dsl.provider.KotlinScriptPlugin
import org.gradle.kotlin.dsl.support.KotlinPluginsBlock
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

        target.configure<JavaPluginConvention>{
            sourceSets.create("generated"){
                java.srcDirs("src/generated/java", "src/generated/kotlin")
            }
            sourceSets.getByName("main"){ java {
                compileClasspath += sourceSets["generated"].output
                runtimeClasspath += sourceSets["generated"].output
            }}

        }



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
    }
}



