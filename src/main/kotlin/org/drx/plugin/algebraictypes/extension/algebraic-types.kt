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
package org.drx.plugin.algebraictypes.extension

import org.gradle.api.Project

sealed class DimensionSelection(
    open val sourceFolder: String,
    open val domain: String,
    open val packageName: String
) {
    data class Single(
        val dimension: Int,
        override val sourceFolder: String = "",
        override val domain: String = "",
        override  val packageName: String = ""
    ) : DimensionSelection(
        sourceFolder,
        domain,
        packageName
    )
    data class Range(
        val from: Int = 2,
        val to: Int,
        override val sourceFolder: String = "",
        override val domain: String = "",
        override  val packageName: String = ""
    ) : DimensionSelection(
        sourceFolder,
        domain,
        packageName
    )
    data class List(
        val list: ArrayList<Int>,
        override val sourceFolder: String = "",
        override val domain: String = "",
        override  val packageName: String = ""
    ) : DimensionSelection(
        sourceFolder,
        domain,
        packageName
    )
    data class Complex(
        val list: ArrayList<DimensionSelection> = arrayListOf(),
        override val sourceFolder: String = "",
        override val domain: String = "",
        override  val packageName: String = ""
    ) : DimensionSelection(
        sourceFolder,
        domain,
        packageName
    )
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

data class SimplifiedDimensionSelection(
    val dimension: Int,
    val sourceFolder: String,
    val domain: String,
    val packageName: String
)

fun DimensionSelection.simplify(): HashSet<SimplifiedDimensionSelection> = when(this) {
    is DimensionSelection.Single -> hashSetOf(SimplifiedDimensionSelection(dimension, sourceFolder,domain, packageName))
    is DimensionSelection.Range -> hashSetOf(*IntRange(from,to).toList().map{dimension -> SimplifiedDimensionSelection(dimension, sourceFolder,domain, packageName)}.toTypedArray())
    
    is DimensionSelection.List -> list.map{dimension -> SimplifiedDimensionSelection(dimension, sourceFolder,domain, packageName)}.toHashSet()
    
    is DimensionSelection.Complex -> {
        val result = hashSetOf<SimplifiedDimensionSelection>()
        list.forEach { result.addAll ( it.simplify() ) }
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

    fun single(dimension: Int, sourceFolder: String = "", packageName: String = "") {
        if(dimensionSelection != null) {
            throw Exception("Selection already set")
        }
        dimensionSelection = DimensionSelection.Single(dimension,sourceFolder,packageName)
    }

    fun dimension(dimension: Int, sourceFolder: String = "", packageName: String = "") {
        if(dimensionSelection is DimensionSelection.Single) {
            throw Exception("Selection already set")
        }
        if(dimensionSelection == null) {
            dimensionSelection = DimensionSelection.Complex( sourceFolder = sourceFolder, packageName = packageName)
        }
        (dimensionSelection as DimensionSelection.Complex).list.add(DimensionSelection.Single(dimension,sourceFolder, packageName))
    }

    fun list(vararg dimensions: Int, sourceFolder: String = "", packageName: String = "") {
        if(dimensionSelection  is DimensionSelection.Single) {
            throw Exception("Selection already set")
        }
        if(dimensionSelection == null) {
            dimensionSelection = DimensionSelection.Complex( sourceFolder = sourceFolder, packageName = packageName)
        }
        (dimensionSelection as DimensionSelection.Complex).list.add(DimensionSelection.List(arrayListOf(*dimensions.toTypedArray()),sourceFolder, packageName))
    }

    fun range(from: Int, to: Int, sourceFolder: String = "",packageName: String = "") {
        if(dimensionSelection  is DimensionSelection.Single) {
            throw Exception("Selection already set")
        }
        if(dimensionSelection == null) {
            dimensionSelection = DimensionSelection.Complex(sourceFolder = sourceFolder, packageName = packageName)
        }
        (dimensionSelection as DimensionSelection.Complex).list.add(DimensionSelection.Range(from, to, sourceFolder, packageName))
    }
}

open class SingleDimensionSelectionExtension {
    var dimensionSelection: DimensionSelection.Single? = null

    fun dimension(dimension: Int, sourceFolder: String = "", packageName: String = "") {
        if (dimensionSelection != null) {
            throw Exception("Selection already set")
        }
        dimensionSelection = DimensionSelection.Single(dimension, sourceFolder, packageName)
    }
}

/**
 * Root dsl function
 */
@AlgebraicTypesDsl
fun Project.algebraicTypes(configuration: AlgebraicTypesExtension.()->Unit) {
    (this as org.gradle.api.plugins.ExtensionAware).extensions.configure("algebraicTypes", configuration)
}

/**
 * Products dsl
 */
@AlgebraicTypesDsl
fun AlgebraicTypesExtension.products(configuration: DimensionSelectionExtension.()->Unit) {
    val extension = DimensionSelectionExtension()
    extension.configuration()
    productTypes = extension.dimensionSelection
}
/**
 * Sums dsl
 */
@AlgebraicTypesDsl
fun AlgebraicTypesExtension.sums(configuration: DimensionSelectionExtension.()->Unit) {
    val extension = DimensionSelectionExtension()
    extension.configuration()
    sumTypes = extension.dimensionSelection
}

/**
 * Product-Arithmetics dsl
 */
@AlgebraicTypesDsl
fun AlgebraicTypesExtension.productArithmetics(configuration: DimensionSelectionExtension.()->Unit) {
    val extension = DimensionSelectionExtension()
    extension.configuration()
    productTypeArithmetics = extension.dimensionSelection
}

/**
 * Evoleq Products dsl
 */
@AlgebraicTypesDsl
fun AlgebraicTypesExtension.evoleqProducts(configuration: DimensionSelectionExtension.()->Unit) {
    val extension = DimensionSelectionExtension()
    extension.configuration()
    evoleqProducts = extension.dimensionSelection
}

/**
 * Evoleq Sums dsl
 */
@AlgebraicTypesDsl
fun AlgebraicTypesExtension.evoleqSums(configuration: DimensionSelectionExtension.()->Unit) {
    val extension = DimensionSelectionExtension()
    extension.configuration()
    evoleqSums = extension.dimensionSelection
}

/**
 * Dualities dsl
 */
@AlgebraicTypesDsl
fun AlgebraicTypesExtension.dualities(configuration: DimensionSelectionExtension.()->Unit) {
    val extension = DimensionSelectionExtension()
    extension.configuration()
    dualities = extension.dimensionSelection
}


//fun AlgebraicTypesExtension.module()

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
@AlgebraicTypesDsl
fun AlgebraicTypesExtension.outputs(configuration: OutputExtension.()->Unit) {
    val extension = OutputExtension()
    extension.configuration()

    outputs = Outputs(
            extension.compile,
            extension.runtime,
            extension.test
    )
}
