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

import org.gradle.api.Project

sealed class DimensionSelection {
    data class Single(val dimension: Int) : DimensionSelection()
    data class Range(val from: Int = 2, val to: Int) : DimensionSelection()
    data class List(val list: ArrayList<Int>) : DimensionSelection()
    data class Complex(val list: ArrayList<DimensionSelection> = arrayListOf()) : DimensionSelection()
}

fun DimensionSelection.toSet(): HashSet<Int> = when (this) {
    is DimensionSelection.Single -> hashSetOf(dimension)
    is DimensionSelection.Range -> hashSetOf(*IntRange(from,to).toList().toTypedArray())
    is DimensionSelection.List -> list.toHashSet()
    is DimensionSelection.Complex -> {
        val result = hashSetOf<Int>()
        list.forEach { result.addAll ( it.toSet() ) }
        result
    }
}

open class AlgebraicTypesExtension {
    var productTypes: DimensionSelection? = null
    var sumTypes: DimensionSelection? = null
    var dualities: DimensionSelection? = null
    var productTypeArithmetics : DimensionSelection? = null
    var evoleqSums : DimensionSelection? = null
    var evoleqProducts : DimensionSelection? = null

    var outputs: Outputs = Outputs()

    val keys: ArrayList<Keys> = arrayListOf()

    var dataClasses: DataClasses? = null
}


open class DimensionSelectionExtension {
    var dimensionSelection: DimensionSelection? = null

    fun single(dimension: Int) {
        if(dimensionSelection != null) {
            throw Exception("Selection already set")
        }
        dimensionSelection = DimensionSelection.Single(dimension)
    }

    fun dimension(dimension: Int) {
        if(dimensionSelection is DimensionSelection.Single) {
            throw Exception("Selection already set")
        }
        if(dimensionSelection == null) {
            dimensionSelection = DimensionSelection.Complex()
        }
        (dimensionSelection as DimensionSelection.Complex).list.add(DimensionSelection.Single(dimension))
    }

    fun list(vararg dimensions: Int) {
        if(dimensionSelection  is DimensionSelection.Single) {
            throw Exception("Selection already set")
        }
        if(dimensionSelection == null) {
            dimensionSelection = DimensionSelection.Complex()
        }
        (dimensionSelection as DimensionSelection.Complex).list.add(DimensionSelection.List(arrayListOf(*dimensions.toTypedArray())))
    }

    fun range(from: Int, to: Int) {
        if(dimensionSelection  is DimensionSelection.Single) {
            throw Exception("Selection already set")
        }
        if(dimensionSelection == null) {
            dimensionSelection = DimensionSelection.Complex()
        }
        (dimensionSelection as DimensionSelection.Complex).list.add(DimensionSelection.Range(from,to))
    }
}

open class SingleDimensionSelectionExtension {
    var dimensionSelection: DimensionSelection.Single? = null

    fun dimension(dimension: Int) {
        if (dimensionSelection != null) {
            throw Exception("Selection already set")
        }
        dimensionSelection = DimensionSelection.Single(dimension)
    }
}

/**
 * Root dsl function
 */
fun Project.algebraicTypes(configuration: AlgebraicTypesExtension.()->Unit) {
    (this as org.gradle.api.plugins.ExtensionAware).extensions.configure("algebraicTypes", configuration)
}

/**
 * Products dsl
 */
fun AlgebraicTypesExtension.products(configuration: DimensionSelectionExtension.()->Unit) {
    val extension = DimensionSelectionExtension()
    extension.configuration()
    productTypes = extension.dimensionSelection
}
/**
 * Sums dsl
 */
fun AlgebraicTypesExtension.sums(configuration: DimensionSelectionExtension.()->Unit) {
    val extension = DimensionSelectionExtension()
    extension.configuration()
    sumTypes = extension.dimensionSelection
}

/**
 * Product-Arithmetics dsl
 */
fun AlgebraicTypesExtension.productArithmetics(configuration: DimensionSelectionExtension.()->Unit) {
    val extension = DimensionSelectionExtension()
    extension.configuration()
    productTypeArithmetics = extension.dimensionSelection
}

/**
 * Evoleq Products dsl
 */
fun AlgebraicTypesExtension.evoleqProducts(configuration: DimensionSelectionExtension.()->Unit) {
    val extension = DimensionSelectionExtension()
    extension.configuration()
    evoleqProducts = extension.dimensionSelection
}

/**
 * Evoleq Sums dsl
 */
fun AlgebraicTypesExtension.evoleqSums(configuration: DimensionSelectionExtension.()->Unit) {
    val extension = DimensionSelectionExtension()
    extension.configuration()
    evoleqSums = extension.dimensionSelection
}

/**
 * Dualities dsl
 */
fun AlgebraicTypesExtension.dualities(configuration: DimensionSelectionExtension.()->Unit) {
    val extension = DimensionSelectionExtension()
    extension.configuration()
    dualities = extension.dimensionSelection
}

data class Outputs(
        val compile: ArrayList<String> = arrayListOf("main"),
        val runtime: ArrayList<String> = arrayListOf("main"),
        val test: ArrayList<String> = arrayListOf("main")
)

open class OutputExtension {
    val compile = arrayListOf<String>("main")
    val runtime = arrayListOf<String>("main")
    val test = arrayListOf<String>("main")


    fun compile(sourceSet: String) {
        compile.add(sourceSet)
    }
    fun runtime(sourceSet: String) {
        runtime.add(sourceSet)
    }
    fun test(sourceSet: String) {
        test.add(sourceSet)
    }

    fun excludeMain(from: ArrayList<String>) {
        from.forEach {
            if(it == "compile") {
                compile.remove(it)
            }
            if(it == "runtime") {
                runtime.remove(it)
            }
            if(it == "test") {
                test.remove(it)
            }
        }
    }
}

/**
 * Outputs dsl
 */
fun AlgebraicTypesExtension.outputs(configuration: OutputExtension.()->Unit) {
    val extension = OutputExtension()
    extension.configuration()

    outputs = Outputs(
            extension.compile,
            extension.runtime,
            extension.test
    )
}

/**
 * KeyGroups dsl
 */
fun AlgebraicTypesExtension.keyGroups(configuration: KeysExtension.()->Unit) {
    val extension = KeysExtension()
    extension.configuration()

    keys.addAll(extension.keys)
}

open class Keys {
    var name: String? = null
    var number: Int? = null
    var serialization: Boolean = false
}

open class KeysExtension {
    val keys = arrayListOf<Keys>()
}
/**
 * KeyGroup dsl
 */
fun KeysExtension.keyGroup(definition: Keys.()->Unit) {
    val k = Keys()
    k.definition()
    keys.add(k)
}

/**********************************************************************************************************************
 *
 * Data class Configuration
 *
 **********************************************************************************************************************/

open class DataClasses {
    val dataClasses : ArrayList<DataClass> = arrayListOf()
    val sealedClasses : ArrayList<SealedClass> = arrayListOf()
    val classes : ArrayList<Class> = arrayListOf()
    val objects : ArrayList<Object> = arrayListOf()
    val interfaces : ArrayList<Object> = arrayListOf()
}

interface ClassRepresentation{
    var name: String
    var packageName: String
    var sourceFolder: String
    val parameters: ArrayList<Parameter>
    val comment: ArrayList<String>
    var settersPostFix: String
}

open class DataClass : ClassRepresentation {
    override lateinit var name: String
    override var packageName: String = ""
    override var sourceFolder: String = ""
    override val parameters: ArrayList<Parameter> = arrayListOf()
    override val comment: ArrayList<String> = arrayListOf()
    override var settersPostFix: String = ""
}

open class Object : ClassRepresentation {
    override lateinit var name: String
    override var packageName: String = ""
    override var sourceFolder: String = ""
    override val parameters: ArrayList<Parameter> = arrayListOf()
    override val comment: ArrayList<String> = arrayListOf()
    override var settersPostFix: String = ""
}

open class Interface : ClassRepresentation {
    override lateinit var name: String
    override var packageName: String = ""
    override var sourceFolder: String = ""
    override val parameters: ArrayList<Parameter> = arrayListOf()
    override val comment: ArrayList<String> = arrayListOf()
    override var settersPostFix: String = ""
}

open class Class : ClassRepresentation {
    override lateinit var name: String
    override var packageName: String = ""
    override var sourceFolder: String = ""
    override val parameters: ArrayList<Parameter> = arrayListOf()
    override val comment: ArrayList<String> = arrayListOf()
    override var settersPostFix: String = ""
}

open class SealedClass : ClassRepresentation {
    override lateinit var name: String
    override var packageName: String = ""
    override var sourceFolder: String = ""
    override val parameters: ArrayList<Parameter> = arrayListOf()
    val representatives: ArrayList<SubClass> = arrayListOf()
    override val comment: ArrayList<String> = arrayListOf()
    override var settersPostFix: String = ""
}

open class SubClass(open val parent: ClassRepresentation) : ClassRepresentation {
    override lateinit var name: String
    override var packageName: String = ""
    override var sourceFolder: String = ""
    override val parameters: ArrayList<Parameter> = arrayListOf()
    override val comment: ArrayList<String> = arrayListOf()
    val overrideParameters: ArrayList<String> = arrayListOf()
    val defaultValuesSet:HashMap<String, String> = hashMapOf()
    override var settersPostFix: String = ""
}

open class SubObject(override val parent: ClassRepresentation) : SubClass(parent)
open class SubDataClass(override val parent: ClassRepresentation) : SubClass(parent)
open class SubSealedClass(override val parent: ClassRepresentation) : SubClass(parent) {
    val representants: ArrayList<SubClass> = arrayListOf()
}

open class Parameter {
    lateinit var name: String
    lateinit var type: ParameterType
    var defaultValue: String? = null
    val modifiers: ArrayList<String> = arrayListOf()
    val comment: ArrayList<String> = arrayListOf()
}

open class ParameterType {
    lateinit var name: String
    var import: String = ""
    var isGeneric: Boolean = false
    var genericIn: String = ""
}

fun SubDataClass.toDataClass(): DataClass {
    val dataClass = DataClass()
    dataClass.name = name
    dataClass.settersPostFix = settersPostFix
    dataClass.packageName = packageName
    dataClass.comment.addAll(comment)
    dataClass.sourceFolder = sourceFolder
    dataClass.parameters.addAll(parameters)
    return dataClass
}


fun AlgebraicTypesExtension.dataClasses(configuration: DataClasses.()->Unit) {

    val dataClasses = DataClasses()
    dataClasses.configuration()
    this.dataClasses = dataClasses
}

fun DataClasses.dataClass(configuration: DataClass.()->Unit) {
    val dataClass = DataClass()
    dataClass.configuration()
    dataClasses.add(dataClass)
}


fun DataClasses.sealedClass(configuration: SealedClass.()->Unit) {
    val sealedClass = SealedClass()
    sealedClass.configuration()
    sealedClasses.add(sealedClass)
}

fun DataClasses.clazz(configuration: Class.()->Unit) {
    val clazz = Class()
    clazz.configuration()
    classes.add(clazz)
}

fun DataClasses.objekt(configuration: Object.()->Unit) {
    val objekt = Object()
    objekt.configuration()
    objects.add(objekt)
}

fun DataClass.parameter(configuration: Parameter.() -> Unit) {
    val parameter = Parameter()
    parameter.configuration()
    parameters.add(parameter)
}

fun SealedClass.parameter(configuration: Parameter.() -> Unit) {
    val parameter = Parameter()
    parameter.configuration()
    parameters.add(parameter)
}

fun SubClass.parameter(configuration: Parameter.() -> Unit) {
    val parameter = Parameter()
    parameter.configuration()
    parameters.add(parameter)
}

fun SealedClass.representative(configuration: SubClass.() -> Unit) = subClass( configuration )

fun SealedClass.dataRepresentative(configuration: SubDataClass.() -> Unit) = representatives.add(subDataClass( configuration ))

fun ClassRepresentation.subDataClass(configuration: SubDataClass.()->Unit): SubDataClass {
    require(this !is DataClass)
    val subClass = SubDataClass(this)
    subClass.configuration()
    return subClass
}

fun ClassRepresentation.subClass(configuration: SubClass.()->Unit) {
    require(this !is DataClass)
    val subClass = SubClass(this)
    subClass.configuration()
}


fun Parameter.type(configuration: ParameterType.()->Unit) {
    val type = ParameterType()
    type.configuration()
    this.type = type
}