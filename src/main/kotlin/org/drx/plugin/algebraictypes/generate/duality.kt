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
package org.drx.plugin.algebraictypes.generate

import org.drx.plugin.algebraictypes.basePath
import org.gradle.api.Project
import java.io.File


/*
open class GenerateDuality: DefaultTask() {
    @Suppress("UnstableApiUsage")
    @set:Option(option = "dimension", description = "The number of factors of the product-type to be generated")
    @get:Input
    var dimension: String = "2"

    @TaskAction
    fun generate() {
        generateDuality(Integer.parseInt(dimension), project)
    }


}

open class GenerateDualities: DefaultTask() {
    @Suppress("UnstableApiUsage")
    @set:Option(option = "from", description = "The lower bound of the range of the product types to be generated")
    @get:Input
    var from: String = "2"

    @set:Option(option = "to", description = "The upper bound of the range of the product types to be generated")
    @get:Input
    var to: String = "2"

    @TaskAction
    fun generate() {
        val to = Integer.parseInt(to)
        val from = Integer.parseInt(from)
        //require(to > 9)
        IntRange(max(2, from),to).forEach {
            generateDuality(it, project)
        }
    }
}
*/


fun generateDuality(dimension: Int, project: Project){

    generateSumType(dimension, project)
    generateProductType(dimension, project)

    val content = buildDuals(dimension)
    val dir = File("${project.projectDir}$basePath/duality")
    if(!dir.exists()) {
        dir.mkdirs()
    }
    val file = File("${project.projectDir}$basePath/duality/duality$dimension.kt")
    file.writeText(content)
}

fun buildDuals(dimension: Int): String {
    var result = license()
    result += "\npackage org.drx.generated.duality\n\n\n"
    result += "\nimport org.drx.generated.products.Product$dimension"
    result += "\nimport org.drx.generated.sums.*"
    result += dist()
    result += dist()
    result += buildOpposeProductFunction(dimension)
    result += dist()
    result += buildOpposeSumFunction(dimension)
    result += dist()
    result += buildSumMeasureFunction(dimension)
    result += dist()
    result += buildSimpleSumMeasureFunction(dimension)

    return result
}

fun buildOpposeProductFunction(dimension: Int) : String {

    val types = buildGenericTypes(dimension, "F")
    val list = arrayListOf<String>()
    val comment = buildComment("Turn a product of functions (F_i)->T into a", "function whose domain is a sum")
    IntRange(1,dimension).forEach {
        list.add(0, "factor$it")
    }
    return "$comment fun <$types ,T> Product$dimension<${buildFunctionTypes(dimension, "F")}>.oppose(): (Sum$dimension<$types>)->T = sum(${list.joinToString(",\n    ", "\n    ", "\n")})"
}

fun buildOpposeSumFunction(dimension: Int) : String {
    val types = buildGenericTypes(dimension, "F")
    val list = arrayListOf<String>()
    IntRange(1,dimension).forEach {
        list.add(0, "factor$it")
    }
    val comment = buildComment("Turn a function (Sum<F_n,...,F_1)->T", "into a product of functions")
    return "fun <$types ,T> ((Sum$dimension<$types>)->T).oppose(): Product$dimension<${buildFunctionTypes(dimension, "F")}> = Product$dimension(${buildOpposedFunctionArgs(dimension, "F")})"

}

fun buildFunctionTypes(dimension: Int, sourceType: String = "S",targetType: String = "T") : String {
    val list = arrayListOf<String>()
    IntRange(1,dimension).forEach {
        list.add(0,"($sourceType$it)->$targetType")
    }
    return list.joinToString(", ")
}

fun buildOpposedFunctionArgs(dimension: Int, sourceType: String = "S",targetType: String = "T"): String {
    val list = arrayListOf<String>()
    val sourceArg = sourceType.toLowerCase()
    IntRange(1,dimension).forEach {
        list.add(0,"{$sourceArg: $sourceType$it -> this@oppose(iota${dimension}_$it<${buildGenericTypes(dimension, "F")}>()($sourceArg))}")
    }
    return list.joinToString(",\n    ", "\n    ", "\n")
}

/**
 * Measuring sums
 */
fun buildSumMeasureFunction(dimension: Int): String {
    val range = IntRange(1,dimension).reversed()

    val comment = buildComment("Measure a sum type")
    val types = range.joinToString(", ") { "T$it" }
    val measures = range.joinToString(", ") { "(T$it) -> M" }
    val cases = range.joinToString("") { "\n    is Sum$dimension.Summand$it -> measure.factor$it( value )" }


    return "${comment}fun <M, $types> Sum$dimension<$types>.measure(measure: Product$dimension<$measures>): M = when( this ) {$cases\n}"
}

/**
 * Measuring simple sums
 */
fun buildSimpleSumMeasureFunction(dimension: Int): String {
    val range = IntRange(1,dimension).reversed()

    val comment = buildComment("Measure a sum type")
    val types = range.joinToString(", ") { "T" }
    //val measures = range.joinToString(", ") { "(T$it) -> M" }
    val cases = range.joinToString("") { "\n    is Sum$dimension.Summand$it -> measure( value )" }


    return "${comment}fun <M, T> Sum$dimension<$types>.measure(measure: (T) -> M): M = when( this ) {$cases\n}"
}

