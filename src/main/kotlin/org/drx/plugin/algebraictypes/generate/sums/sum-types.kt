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
package org.drx.plugin.algebraictypes.generate.sums

import org.drx.plugin.algebraictypes.basePath
import org.drx.plugin.algebraictypes.generate.buildGenericTypes
import org.drx.plugin.algebraictypes.generate.dist
import org.drx.plugin.algebraictypes.generate.license
import org.gradle.api.Project
import java.io.File


/**********************************************************************************************************************
 *
 * Sum Types
 *
 **********************************************************************************************************************/


fun generateSumType(dimension: Int, project: Project) {

    generateSumInterface(project)
/*
    val dir = File("${project.projectDir}$basePath/sums")
    if(!dir.exists()) {
        dir.mkdirs()
    }
*/


    var sumType = license()

    sumType += "\n\npackage org.drx.generated.sums\n\n\n"
    sumType += buildSumType(dimension)
    sumType += dist()
    sumType += buildSumFunction(dimension)
    sumType += dist()
    //sumType += buildRecSumFunction(Integer.parseInt(dimension))

    sumType += buildInjectionFunctions(dimension)
    sumType += dist()
    sumType += buildSumMaps(dimension)

    val sumTypeFile = File("${project.projectDir}$basePath/sums/sum-$dimension.kt")
    sumTypeFile.writeText(sumType)
    println("Generating sum type of dimension $dimension")
}

fun generateSumInterface(project: Project){



    val dir = File("${project.projectDir}$basePath/sums")
    if(!dir.exists()) {
        dir.mkdirs()
    }
    val file = File("${project.projectDir}$basePath/sums/sum.kt")
    if(!file.exists()) {
        var sum = license()
        sum += dist()
        sum += "package org.drx.generated.sums"
        sum += dist()
        sum += "interface Sum"
        file.writeText(sum)
    }
}

fun buildSumType(dimension: Int): String {
    val className = "Sum$dimension"
    var result = "sealed class $className<${buildGenericTypes(dimension, "S", variance = "out")}> : Sum {\n${buildSummands(dimension)}}"

    return result
}

fun buildSummands(dimension: Int): String {
    var result = ""
    val types = buildGenericTypes(dimension, "S")
    val typesOut = buildGenericTypes(dimension, "S", variance = "out")
    IntRange(1,dimension).forEach { result += "    data class Summand$it<$typesOut>(val value: S$it) : Sum$dimension<$types>()\n" }
    return result
}



 fun buildSumFunction(dimension: Int, targetType: String = "T"): String {

    var result = "fun <${buildGenericTypes(dimension, "S")}, $targetType> sum(\n${buildSumFunctionArguments(dimension, targetType)}\n): (Sum$dimension<${buildGenericTypes(dimension, "S")}>) -> $targetType = { sum -> \n    when (sum) {\n${buildSumFunctionCases(dimension)}\n    } \n}"


    return result
}

fun buildRecSumFunction(dimension: Int): String {
    val targetType = "Sum$dimension<${buildGenericTypes(dimension, "S")}>"
    return "fun <${buildGenericTypes(dimension, "S")}> sumRec(\n${buildSumFunctionArguments(dimension, targetType)}\n) : ($targetType) -> $targetType\n = { sum -> \n" +
            "    when (sum) {\n" +
            "${buildSumFunctionCases(dimension)}\n" +
            "    } \n" +
            "}"
}

fun buildSumFunctionArguments(dimension: Int, targetType: String = "T"): String {
    val list = arrayListOf<String>()
    IntRange(1,dimension).forEach { list.add(0,"    f$it: (S$it) -> $targetType") }
    return list.joinToString ( ",\n" )
}

fun buildSumFunctionCases(dimension: Int): String {
    val list = arrayListOf<String>()
    IntRange(1,dimension).forEach { list.add(0,"        is Sum$dimension.Summand$it -> f$it(sum.value)") }
    return list.joinToString ( "\n" )
}



fun buildInjectionFunctions(dimension: Int): String{
    var result = ""
    IntRange(1,dimension).forEach{
        result += dist() + buildInjectionFunction(dimension, it)
    }
    return result
}

fun buildInjectionFunction(dimension: Int, index: Int): String{
    //val types = buildGenericTypes(dimension,"S")
    //return "fun <$types> iota${dimension}_$index(s$index: S$index) : Sum$dimension<$types> = Sum$dimension.Summand$index(s$index)"
    val types = buildGenericTypes(dimension, "S")
    return "fun <$types> iota${dimension}_$index() : (S$index) -> Sum$dimension<$types> = { s$index -> Sum$dimension.Summand$index(s$index) }"
}


fun buildSumMaps(dimension: Int) : String {
    var result = ""
    IntRange(1,dimension).forEach {
        result += dist() + buildSumMap(dimension, it)
    }
    return result
}

fun buildSumMap(dimension: Int, index: Int): String {

    val Fs = buildGenericTypes(dimension, "S")
    val newFs = buildGenericTypes(dimension, "S", index, "T")
    val casesList = arrayListOf<String>()
    IntRange(1,dimension).forEach {
        val case = if(it==index){"is Sum$dimension.Summand$it -> Sum$dimension.Summand$it( f( value ) )"}else{"is Sum$dimension.Summand$it -> Sum$dimension.Summand$it( value )"}
        casesList.add(0, case)
    }
    val cases = casesList.joinToString("\n    ", "    ")
    return "infix fun <T$index, $Fs> Sum$dimension<$Fs>.map$index(f:(S$index)->T$index) : Sum$dimension<$newFs> = when(this){ \n$cases\n}"
}
