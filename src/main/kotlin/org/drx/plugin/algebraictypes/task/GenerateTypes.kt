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
import org.drx.plugin.algebraictypes.extension.*
import org.drx.plugin.algebraictypes.generate.duality.generateDuality
import org.drx.plugin.algebraictypes.generate.evoleq.generateEvoleqProduct
import org.drx.plugin.algebraictypes.generate.evoleq.generateEvoleqSum
import org.drx.plugin.algebraictypes.generate.keys.generateKeyGroup1
import org.drx.plugin.algebraictypes.generate.optics.generatePseudoLenses
import org.drx.plugin.algebraictypes.generate.optics.generatePseudoPrisms
import org.drx.plugin.algebraictypes.generate.products.generateProductType
import org.drx.plugin.algebraictypes.generate.products.generateProductTypeArithmetic
import org.drx.plugin.algebraictypes.generate.sums.generateSumType
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
            generateKeyGroup1(it.name!!, it.number!!, it.serialization, project)
        }

        if(extension.dataClasses != null) {

            val allClasses = extension.dataClasses!!.toSet()
            // validate input

            // add info about serialization
            val serializableClasses = hashSetOf<ClassRepresentation>()
            allClasses.forEach {
                if(it.serializable) {
                    serializableClasses.add(it)
                }
            }
            val collection = hashSetOf<ClassRepresentation>()
            serializableClasses.forEach {
                it.parameters.forEach { param ->
                    val clazz = allClasses.findByNameIn(param.type.name,
                            param.type.packageName,
                            it.packageName
                    )
                    if(clazz != null) {
                        clazz.serializable = true
                        param.type.serializable = true
                        if(clazz is SealedClass) {
                            param.type.serializationType = SerializationType.Polymorphic
                        }
                        collection.add(clazz)
                    }
                    param.type.dependencies.forEach { dependency ->
                        with(
                                allClasses.findByNameIn(dependency.name,
                                        dependency.packageName,
                                        param.type.packageName,
                                        it.packageName
                                )
                        ) {
                            if(this != null) {
                                this.serializable = true
                                dependency.serializable = true
                                collection.add(this)
                            }
                        }
                    }
                }
            }
            serializableClasses.addAll(collection)
/*
            serializableClasses.forEach {clazz ->
                if(clazz is DataClass) {
                    clazz.serializationType = SerializationType.Serializable
                    clazz.parameters.forEach { param ->
                        if(param.type.serializable) {
                            if(param.defaultValue == null) {
                                val paramRep = allClasses.findByFullName(param.type.import + "." + param.type.name)
                                if(paramRep != null){

                                }
                            }
                        }
                    }
                }
            }

  */
            // generate classes
            generatePseudoLenses(extension.dataClasses!!, project)
            generatePseudoPrisms(extension.dataClasses!!, project)
        }
    }

}