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


/**********************************************************************************************************************
 *
 * Product Types
 *
 **********************************************************************************************************************/
/*
open class GenerateProductType: DefaultTask() {
    @Suppress("UnstableApiUsage")
    @set:Option(option = "dimension", description = "The number of factors of the product-type to be generated")
    @get:Input
    var dimension: String = "2"

    @TaskAction
    fun generate() {
        generateProductType(Integer.parseInt(dimension), project)
    }


}


open class GenerateProductTypes: DefaultTask() {
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
            generateProductType(it, project)
        }
    }
}
*/
fun generateProductInterface(project: Project){

    val dir = File("${project.projectDir}$basePath/products")
    if(!dir.exists()) {
        dir.mkdirs()
    }
    val file = File("${project.projectDir}$basePath/products/product.kt")
    if(!file.exists()) {
        var sum = license()
        sum += dist()
        sum += "package org.drx.generated.products"
        sum += dist()
        sum += "interface Product"
        file.writeText(sum)
    }
}


fun generateProductType(dimension: Int, project: Project) {

    generateProductInterface(project)
    /*
    val dir = File("${project.projectDir}$basePath/products")
    if (!dir.exists()) {
        dir.mkdirs()
    }
    */
    var sumType = license()
    sumType += "\n\npackage org.drx.generated.products\n\n\n"
    sumType += buildProductType(dimension)
    sumType += dist()
    sumType += buildProductFunction(dimension)
    sumType += dist()
    // sumType += buildProjectionFunctions( dimension )
    sumType += buildSimpleProjectionFunctions(dimension)
    sumType += dist()
    sumType += buildProductMaps(dimension)

    val sumTypeFile = File("${project.projectDir}$basePath/products/product-$dimension.kt")
    sumTypeFile.writeText(sumType)
    println("Generating product type of dimension $dimension")
}


fun buildProductType(dimension: Int): String {
    var result = "data class Product$dimension<${buildGenericTypes(dimension, "F", variance = "out")}>(${buildProductTypeArguments(dimension, "val")}) : Product\n"
    result += dist()
    result += "@Suppress(\"FunctionName\")\n"
    result += "fun <${buildGenericTypes(dimension, "F")}> Product(${buildProductTypeArguments(dimension)}) : Product$dimension<${buildGenericTypes(dimension, "F")}> = Product$dimension(${buildProductArguments(dimension)})"
    return result
}

fun buildProductTypeArguments(dimension: Int, modifier: String? = null): String {

    val list = arrayListOf<String>()
    IntRange(1,dimension).forEach { list.add(0,"${if(modifier != null){"$modifier "}else{""}}factor$it: F$it") }
    return list.joinToString ( ", " )

}

fun buildProductArguments(dimension: Int, modifier: String? = null): String {
    val list = arrayListOf<String>()
    IntRange(1,dimension).forEach { list.add(0,"${if(modifier != null){"$modifier "}else{""}}factor$it") }
    return list.joinToString ( ", " )
}

fun buildProjectionFunctions(dimension: Int): String {
    var result = ""
    IntRange(1,dimension).forEach {
        result += dist() + buildProjectionFunction(dimension, it)
    }
    return result
}
fun buildProjectionFunction(dimension: Int, index: Int): String {
    val types = buildGenericTypes(dimension, "F")
    return "fun <$types> pi${dimension}_$index(product: Product$dimension<$types>) : F$index = product.factor$index"
}
fun buildSimpleProjectionFunctions(dimension: Int): String {
    var result = ""
    IntRange(1,dimension).forEach {
        result += dist() + buildSimpleProjectionFunction(dimension, it)
    }
    return result
}
fun buildSimpleProjectionFunction(dimension: Int, index: Int): String {
    //   val types = buildGenericTypes(dimension,"F")
    //   return "fun <$types> Product$dimension<$types>.pi$index() : F$index = factor$index"
    val types = buildGenericTypes(dimension, "F")
    return "fun <$types> pi${dimension}_$index() : (Product$dimension<$types>) -> F$index = { product -> product.factor$index }"
}

fun buildProductFunction(dimension: Int) : String {


    val Ss = buildGenericTypes(dimension, "S")
    val Ts = buildGenericTypes(dimension, "T")



    val functionArgsList = arrayListOf<String>()
    val resultFactorsList = arrayListOf<String>()
    IntRange(1,dimension).forEach{
        functionArgsList.add(0,"f$it:(S$it)->T$it")
        resultFactorsList.add(0, "f$it(product.factor$it)")
    }
    val functionArgs = functionArgsList.joinToString(", ")
    val resultFactors = resultFactorsList.joinToString(", ")



    return "fun <$Ss, $Ts> product($functionArgs): (Product$dimension<$Ss>) -> (Product$dimension<$Ts>) = { product -> Product($resultFactors) }"

}

fun buildProductMaps(dimension: Int) : String {
    var result = ""
    IntRange(1,dimension).forEach {
        result += dist() + buildProductMap(dimension, it)
    }
    return result
}

fun buildProductMap(dimension: Int, index: Int): String {

    val Fs = buildGenericTypes(dimension, "F")
    val newFs = buildGenericTypes(dimension, "F", index, "G")
    val resultProductList = arrayListOf<String>()
    IntRange(1,dimension).forEach {
        val value = if(it == index){"f(factor$it)"}else{"factor$it"}
        resultProductList.add(0,value)
    }
    val resultProduct = resultProductList.joinToString(", ")
    return "infix fun <G$index, $Fs> Product$dimension<$Fs>.map$index(f:(F$index)->G$index) : Product$dimension<$newFs> = Product($resultProduct)"
}
