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
}

open class DataClass {
    lateinit var name: String
    var packageName: String = ""
    var sourceFolder: String = ""
    val parameters: ArrayList<Parameter> = arrayListOf()
}

open class Parameter {
    lateinit var name: String
    lateinit var type: ParameterType
    var defaultValue: String? = null
}

open class ParameterType {
    lateinit var name: String
    var import: String = ""
    var isGeneric: Boolean = false
    var genericIn: String = ""
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

fun DataClass.parameter(configuration: Parameter.() -> Unit) {
    val parameter = Parameter()
    parameter.configuration()
    parameters.add(parameter)
}

fun Parameter.type(configuration: ParameterType.()->Unit) {
    val type = ParameterType()
    type.configuration()
    this.type = type
}