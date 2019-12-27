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
package org.drx.plugin.algebraictypes.task

import org.drx.plugin.algebraictypes.*
import org.drx.plugin.algebraictypes.generate.*
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File

open class GenerateTypes : DefaultTask() {



    @Input
    val extension : AlgebraicTypesExtension = project.extensions.getByType(AlgebraicTypesExtension::class.java)

    @TaskAction
    fun generate() {
        // cleanup
        val dirs = arrayListOf("duality","evoleq","sums","products")
        dirs.forEach {
            val file = File("${project.projectDir}/$basePath/$it")
            if(file.exists()) {
                file.deleteRecursively()
            }
        }
        // TODO optimize generation process
        // generate
        if(extension.evoleqSums != null) {
            extension.evoleqSums!!.toSet().forEach {
                generateEvoleqSum(it, project)
            }
        }
        if(extension.evoleqProducts != null) {
            extension.evoleqProducts!!.toSet().forEach {
                generateEvoleqProduct(it, project)
            }
        }
        if(extension.sumTypes != null) {
            extension.sumTypes!!.toSet().forEach {
                generateSumType(it, project)
            }
        }
        if(extension.productTypes != null) {
            extension.productTypes!!.toSet().forEach {
                generateProductType(it, project)
            }
        }
        if(extension.dualities != null) {
            extension.dualities!!.toSet().forEach {
                generateDuality(it, project)
            }
        }
        if(extension.productTypeArithmetics != null) {
            extension.productTypeArithmetics!!.toSet().forEach {
                generateProductTypeArithmetic(it, project)
            }
        }

        extension.keys.forEach {
            generateKeyGroup1(it.name!!,it.number!!, it.serialization, project)
        }

        if(extension.dataClasses != null) {
            generatePseudoLenses(extension.dataClasses!!, project)
        }
    }

}