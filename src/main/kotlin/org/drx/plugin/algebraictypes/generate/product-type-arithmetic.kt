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
open class GenerateProductTypeArithmetic: DefaultTask() {
    @Suppress("UnstableApiUsage")
    @set:Option(option = "dimension", description = "The number of factors of the product-type to be generated")
    @get:Input
    var dimension: String = "2"

    @TaskAction
    fun generate() {
        require(Integer.parseInt(dimension) >=2 )
        generateProductTypeArithmetic(Integer.parseInt(dimension), project)
    }


}


open class GenerateProductTypeArithmetics: DefaultTask() {
    @Suppress("UnstableApiUsage")
    @set:Option(option = "dimension", description = "The number of factors of the product-type to be generated")
    @get:Input
    var dimension: String = "2"

    @TaskAction
    fun generate() {
        require(Integer.parseInt(dimension) >=2 )
        IntRange(2,Integer.parseInt(dimension)).forEach {
            generateProductTypeArithmetic(it, project)
        }
    }


}
*/



fun generateProductTypeArithmetic(dimension: Int, project: Project) {
    IntRange(2, dimension).forEach { generateProductType(it, project) }

    var sumType = license()
    sumType += "\n\npackage org.drx.generated.products\n\n\n"
    sumType += buildProductOperators(dimension)

    val sumTypeFile = File("${project.projectDir}$basePath/products/product-arithmetic-$dimension.kt")
    sumTypeFile.writeText(sumType)
    println("Generating product type arithmetic of dimension $dimension")

}

fun buildProductOperators(dimension: Int) : String{
    var result = ""
    IntRange(1,dimension-1).forEach {
        result += dist()
        result += buildProductOperator(dimension, it)
    }
    return result
}

fun buildProductOperator(dimension: Int, first: Int) : String {

    val second = dimension - first
    require(first >= 1 && second >= 1)

    var result = ""
    var factorsList = arrayListOf<String>()
    if(second > 1 && first > 1) {
        IntRange(1, dimension).forEach {
            if (it <= first) {
                factorsList.add(0, "other.factor$it")
            } else {
                factorsList.add(0, "factor${it - first}")
            }
        }
        result = "operator fun <${buildGenericTypes(dimension, "F")}> (Product$second<${buildGenericTypes(dimension, "F", first + 1)}>).times(other: Product$first<${buildGenericTypes(first, "F")}>) : Product$dimension<${buildGenericTypes(dimension, "F")}> = Product$dimension(${factorsList.joinToString(",\n    ", "\n    ", "\n")})"
    }
    if(first == 1) {
        IntRange(1, dimension).forEach {
            if (it <= first) {
                factorsList.add(0, "other")
            } else {
                factorsList.add(0, "factor${it - first}")
            }
        }
        result = "operator fun <${buildGenericTypes(dimension, "F")}> (Product$second<${buildGenericTypes(dimension, "F", first + 1)}>).times(other: F1) : Product$dimension<${buildGenericTypes(dimension, "F")}> = Product$dimension(${factorsList.joinToString(",\n    ", "\n    ", "\n")})"

    }
    if(second == 1) {
        IntRange(1, dimension).forEach {
            if (it <= first) {
                factorsList.add(0, "other.factor$it")
            } else {
                factorsList.add(0, "this")
            }
        }
        result = "operator fun <${buildGenericTypes(dimension, "F")}> (F$dimension).times(other: Product$first<${buildGenericTypes(first, "F")}>) : Product$dimension<${buildGenericTypes(dimension, "F")}> = Product$dimension(${factorsList.joinToString(",\n    ", "\n    ", "\n")})"

    }
    if(first == 1 && second ==1) {
        result = "operator fun <F2,F1> (F2).times(other: F1) : Product2<F2,F1> = Product2(this, other)"
    }

    return result
}