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

import org.drx.plugin.algebraictypes.config.Config
import org.gradle.api.Project
import java.io.File

/**********************************************************************************************************************
 *
 * Auxiliary functions
 *
 **********************************************************************************************************************/
class Marker{
    companion object obj {}
}

fun license(): String {
    val obj = object{}
    //println(Marker.obj::class.java.getResource("."))
    val content = File("src/main/resources/org/drx/plugin/algebraictypes/LICENSE")
    val license = obj.javaClass.getResource("LICENSE").readText().replace("\n", "\n * ")
    return "/**\n * $license\n */"
}

fun usesPlugin(): String = """
    |// This file has been generated using the kotlin-algebraic-types-plugin
""".trimMargin()

fun dist() = "\n\n"

/**
 * Type names in import-statements
 * ====================================================================================================================
 */

fun String.nonNullable(): String = when(endsWith("?")) {
    true -> dropLast(1)
    false -> this
}

fun String.fixTypeNameForImport() : String = when(contains(".")) {
    false -> this
    true -> split(".")[0]
}

/**
 * Transform file-name to package-name and vice versa
 * ====================================================================================================================
 */
fun String.packageCase(): String = replace("/",".").replace("..", ".")

fun String.fileCase(): String = replace(".","/").replace("//", "/")

fun Project.file(sourceFolder: String, domain: String, packageName: String): File = File(
    "${project.projectDir}/${sourceFolder.fileCase()}/${domain.fileCase()}/${packageName.fileCase()}".replace("//","/")
)

fun Project.file(sourceFolder: String, domain: String, packageName: String, filename: String): File = File(
    project.file(sourceFolder, domain, packageName),
    filename
)


object TypePackage {
    const val product = "products"
    const val sum = "sums"
    const val duality = "duality"
    const val evoleqSum = "evoleq.$sum"
    const val evoleqProduct = "evoleq.$product"
    const val lens = "lenses"
    const val prism = "prims"
    const val annotation = "annotation"
}

fun String.productsPackage(): String = when {
    endsWith(TypePackage.annotation) -> replaceLast(TypePackage.annotation, TypePackage.product)
    endsWith(TypePackage.sum) -> replaceLast(TypePackage.sum, TypePackage.product)
    endsWith(TypePackage.duality) -> replaceLast(TypePackage.duality, TypePackage.product)
    endsWith(TypePackage.evoleqSum) -> replaceLast(TypePackage.evoleqSum, TypePackage.product)
    endsWith(TypePackage.evoleqProduct) -> replaceLast(TypePackage.evoleqProduct, TypePackage.product)
    endsWith(TypePackage.lens) -> replaceLast(TypePackage.lens, TypePackage.product)
    endsWith(TypePackage.prism) -> replaceLast(TypePackage.prism, TypePackage.product)
    endsWith(TypePackage.product) -> this
    equals("") -> TypePackage.product
    else -> this + "." + TypePackage.product
}
fun String.sumsPackage(): String = when {
    endsWith(TypePackage.annotation) -> replaceLast(TypePackage.annotation, TypePackage.product)
    endsWith(TypePackage.product) -> replaceLast(TypePackage.product, TypePackage.sum)
    endsWith(TypePackage.duality) -> replaceLast(TypePackage.duality, TypePackage.sum)
    endsWith(TypePackage.evoleqSum) -> replaceLast(TypePackage.evoleqSum, TypePackage.sum)
    endsWith(TypePackage.evoleqProduct) -> replaceLast(TypePackage.evoleqProduct, TypePackage.sum)
    endsWith(TypePackage.lens) -> replaceLast(TypePackage.lens, TypePackage.sum)
    endsWith(TypePackage.prism) -> replaceLast(TypePackage.prism, TypePackage.sum)
    endsWith(TypePackage.sum) -> this
    equals("") -> TypePackage.sum
    else -> this + "." +TypePackage.sum
}
fun String.dualityPackage(): String = when {
    endsWith(TypePackage.annotation) -> replaceLast(TypePackage.annotation, TypePackage.product)
    endsWith(TypePackage.product) -> replaceLast(TypePackage.product, TypePackage.duality)
    endsWith(TypePackage.sum) -> replaceLast(TypePackage.sum, TypePackage.duality)
    endsWith(TypePackage.evoleqSum) -> replaceLast(TypePackage.evoleqSum, TypePackage.duality)
    endsWith(TypePackage.evoleqProduct) -> replaceLast(TypePackage.evoleqProduct, TypePackage.duality)
    endsWith(TypePackage.lens) -> replaceLast(TypePackage.lens, TypePackage.duality)
    endsWith(TypePackage.prism) -> replaceLast(TypePackage.prism, TypePackage.duality)
    endsWith(TypePackage.duality) -> this
    equals("") -> TypePackage.duality
    else -> this + "." + TypePackage.duality
}

fun String.evoleqSumPackage() = when {
    endsWith(TypePackage.annotation) -> replaceLast(TypePackage.annotation, TypePackage.product)
    endsWith(TypePackage.product) -> replaceLast(TypePackage.product, TypePackage.evoleqSum)
    endsWith(TypePackage.sum) -> replaceLast(TypePackage.sum, TypePackage.evoleqSum)
    endsWith(TypePackage.duality) -> replaceLast(TypePackage.duality, TypePackage.evoleqSum)
    endsWith(TypePackage.evoleqProduct) -> replaceLast(TypePackage.evoleqProduct, TypePackage.evoleqSum)
    endsWith(TypePackage.lens) -> replaceLast(TypePackage.lens, TypePackage.evoleqSum)
    endsWith(TypePackage.prism) -> replaceLast(TypePackage.prism, TypePackage.evoleqSum)
    endsWith(TypePackage.evoleqSum) -> this
    equals("") -> TypePackage.evoleqSum
    else -> this + "." + TypePackage.evoleqSum
}
fun String.evoleqProductPackage() = when {
    endsWith(TypePackage.annotation) -> replaceLast(TypePackage.annotation, TypePackage.product)
    endsWith(TypePackage.product) -> replaceLast(TypePackage.product, TypePackage.evoleqProduct)
    endsWith(TypePackage.sum) -> replaceLast(TypePackage.product, TypePackage.evoleqProduct)
    endsWith(TypePackage.duality) -> replaceLast(TypePackage.duality, TypePackage.evoleqProduct)
    endsWith(TypePackage.evoleqSum) -> replaceLast(TypePackage.evoleqSum, TypePackage.evoleqProduct)
    endsWith(TypePackage.lens) -> replaceLast(TypePackage.lens, TypePackage.evoleqProduct)
    endsWith(TypePackage.prism) -> replaceLast(TypePackage.prism, TypePackage.evoleqProduct)
    endsWith(TypePackage.evoleqProduct) -> this
    equals("") -> TypePackage.evoleqProduct
    else -> this + "." + TypePackage.evoleqProduct
}

fun String.lensPackage(): String = when {
    endsWith(TypePackage.annotation) -> replaceLast(TypePackage.annotation, TypePackage.product)
    endsWith(TypePackage.product) -> replaceLast(TypePackage.product, TypePackage.lens)
    endsWith(TypePackage.duality) -> replaceLast(TypePackage.duality, TypePackage.lens)
    endsWith(TypePackage.evoleqSum) -> replaceLast(TypePackage.evoleqSum, TypePackage.lens)
    endsWith(TypePackage.evoleqProduct) -> replaceLast(TypePackage.evoleqProduct, TypePackage.lens)
    endsWith(TypePackage.sum) -> replaceLast(TypePackage.sum, TypePackage.lens)
    endsWith(TypePackage.prism) -> replaceLast(TypePackage.prism, TypePackage.lens)
    endsWith(TypePackage.lens) -> this
    equals("") -> TypePackage.lens
    else -> this + "." +TypePackage.lens
}

fun String.prismPackage(): String = when {
    endsWith(TypePackage.annotation) -> replaceLast(TypePackage.annotation, TypePackage.product)
    endsWith(TypePackage.product) -> replaceLast(TypePackage.product, TypePackage.prism)
    endsWith(TypePackage.duality) -> replaceLast(TypePackage.duality, TypePackage.prism)
    endsWith(TypePackage.evoleqSum) -> replaceLast(TypePackage.evoleqSum, TypePackage.prism)
    endsWith(TypePackage.evoleqProduct) -> replaceLast(TypePackage.evoleqProduct, TypePackage.prism)
    endsWith(TypePackage.lens) -> replaceLast(TypePackage.lens, TypePackage.prism)
    endsWith(TypePackage.sum) -> replaceLast(TypePackage.sum, TypePackage.prism)
    endsWith(TypePackage.prism) -> this
    equals("") -> TypePackage.prism
    else -> this + "." +TypePackage.prism
}

fun String.annotationPackage(): String = when {
    endsWith(TypePackage.prism) -> replaceLast(TypePackage.prism, TypePackage.annotation)
    endsWith(TypePackage.product) -> replaceLast(TypePackage.product, TypePackage.annotation)
    endsWith(TypePackage.duality) -> replaceLast(TypePackage.duality, TypePackage.annotation)
    endsWith(TypePackage.evoleqSum) -> replaceLast(TypePackage.evoleqSum, TypePackage.annotation)
    endsWith(TypePackage.evoleqProduct) -> replaceLast(TypePackage.evoleqProduct, TypePackage.annotation)
    endsWith(TypePackage.lens) -> replaceLast(TypePackage.lens, TypePackage.annotation)
    endsWith(TypePackage.sum) -> replaceLast(TypePackage.sum, TypePackage.annotation)
    endsWith(TypePackage.annotation) -> this
    equals("") -> TypePackage.annotation
    else -> this + "." +TypePackage.annotation
}

fun String.replaceLast(oldValue: String, newValue: String): String = reversed().replaceFirst(oldValue.reversed(), newValue.reversed()).reversed()
/**
 * Generic Types
 * ====================================================================================================================
 */
/**
 *
 */
fun buildGenericTypes(dimension: Int, type: String, from: Int = 1, variance: String? = null): String{
    val list = arrayListOf<String>()
    IntRange(from,dimension).forEach { list.add(0,"${if(variance != null){"$variance "}else{""}}$type$it") }
    return list.joinToString ( ", " )
}

/**
 *
 */
fun buildGenericTypes(dimension: Int, type: String, index: Int, typeAtIndex: String, variance: String? = null): String{
    var result = ""
    val list = arrayListOf<String>()
    IntRange(1,dimension).forEach {
        val string  = if(it == index){"$typeAtIndex$it"}else{"$type$it"}
        list.add(0,"${if(variance != null){"$variance "}else{""}}$string")
    }
    return list.joinToString ( ", " )
}

fun buildIdLambda(type: String) = "{ ${type.toLowerCase()} : ${type.toUpperCase()} -> ${type.toLowerCase()} }"

/**
 * Comments
 * ====================================================================================================================
 */

fun buildComment(vararg lines: String): String {
    return lines.joinToString("\n * ", "/**\n * ", "\n */\n") { it }
}

fun buildComment(lines: List<String>, offset : String = "", lineBreak: Unit? = Unit): String {
    return if(lines.isNotEmpty()) {
        lines.joinToString("\n$offset * ", "$offset/**\n$offset * ", "\n$offset */${if(lineBreak!=null){"\n"}else{""}}") { it }
    } else {
        ""
    }
}


fun buildSectionComment(vararg lines: String): String {

    val list = arrayListOf("",*lines,"")
    return list.joinToString("\n * ", "/${Config.Defaults.starLine}\n * ", "\n ${Config.Defaults.starLine}/\n") { it }

}

fun buildParaGraphComment(vararg lines: String): String {

    val list = arrayListOf(*lines, Config.Defaults.equalSignLine)
    return list.joinToString("\n * ", "/**\n * ", "\n */\n") { it }

}

