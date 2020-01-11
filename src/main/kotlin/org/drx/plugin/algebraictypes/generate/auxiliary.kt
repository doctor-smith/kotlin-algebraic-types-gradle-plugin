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