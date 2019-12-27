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

import org.drx.plugin.algebraictypes.DataClass
import org.drx.plugin.algebraictypes.DataClasses
import org.drx.plugin.algebraictypes.basePath
import org.gradle.api.Project
import java.io.File

/**
 * Generate pseudo lenses
 */
fun generatePseudoLenses(dataClasses: DataClasses, project: Project) {
    val dir = File("${project.projectDir}$basePath/lenses")
    if(!dir.exists()) {
        dir.mkdirs()
    }
    val file = File("${project.projectDir}$basePath/lenses/functions.kt")
    if(!file.exists()) {
        var sum = license()
        sum += dist()
        sum += "package org.drx.generated.lenses"
        sum += dist()
        sum += buildIdSetter()
        sum += dist()
        sum += buildSuspendedIdSetter()
        sum += dist()
        sum += buildExtendedFunctionComposer()
        sum += dist()
        sum += buildFluentFunction()
        sum += dist()

        file.writeText(sum)
    }

    // generate products in all needed dimensions
    with(dataClasses.dataClasses.map {
        it.parameters.size
    }.toHashSet()){
        add(2)
        forEach {
            generateProductType(it,project)
        }
    }
    dataClasses.dataClasses.forEach {
        generatePseudoLens(it,project)
    }
}

/**
 * Generate pseudo lens
 */
fun generatePseudoLens(dataClass: DataClass, project: Project) {
    // build string representation and save it
    with(file(dataClass,project)){
        //throw Exception(absolutePath)
        if(!exists()) {
            parentFile.mkdirs()
            createNewFile()

        }
        writeText(buildDataClass(dataClass))
    }
}

/**
 * Build the data class
 */
fun buildDataClass(dataClass: DataClass): String {

    val generics = generics(dataClass)

    return """${license()}
        |
        |package ${packageName(dataClass)}
        |
        |${imports(dataClass)}
        |
        |${dataClassRepresentation(dataClass, generics)}
        |
        |${setter(dataClass, generics)}
        |
        |${parameterSetters(dataClass,generics)}
        |
        |${transaction(dataClass,dataClass.parameters.size, generics)}
        |
        |${setterSuspended(dataClass, generics)}
        |
        |${parameterSettersSuspended(dataClass,generics)}
        |
        |${transactionSuspended(dataClass,dataClass.parameters.size, generics)}
    """.trimMargin()
}

/**
 * Compute the package name
 */
fun packageName(dataClass: DataClass) :String = when(dataClass.packageName) {
    "" -> "org.drx.generated.lenses"
    else -> dataClass.packageName
}

/**
 * Data class may depend on types which need to be imported.
 * Compute the imports of the data class.
 */
fun imports(dataClass: DataClass): String {
    val dimension = dataClass.parameters.size
    val imports = hashSetOf(
            "org.drx.generated.lenses.*",
            "org.drx.generated.products.*"
    )
    imports.addAll(dataClass.parameters.map{it.type.import})
    return imports.filter{it != ""}.map{"import $it"}.sortedWith(Comparator<String> { o1, o2 -> o1.compareTo(o2) })
            .joinToString("\n")
}

/**
 * Build the representation of the data class
 */
fun dataClassRepresentation(dataClass: DataClass, generics: String?): String {


    fun parameters(): String = dataClass.parameters.joinToString(",\n", "", "") { "    val ${it.name}: ${it.type.name}" }
    return """
        |data class ${dataClass.name}${if(generics!=null){"<$generics>"}else{""}}(
        |${parameters()}
        |)
    """.trimMargin()
}

/**
 * Data class may depend on generic parameters:
 */
fun generics(dataClass: DataClass): String? {
    val result = dataClass.parameters
            .filter { it.type.isGeneric }
            .map{it.type.name}
            .toSet()
            .joinToString(", ")
    if(result == "") {
        return null
    }
    return result
}

/**
 * Build the setter:
 * Data.set(setter: Data.()->Product<..., T_i.()->T_i ,...>): Data
 */
fun setter(dataClass: DataClass, generics: String?): String {
    val genericsOnClass = if(generics == null){""}else{"<$generics>"}

    return """fun $genericsOnClass ${dataClass.name}$genericsOnClass.set(transaction: ${dataClass.name}$genericsOnClass.()->${productSetterType(dataClass,generics)}): ${dataClass.name}$genericsOnClass = with(transaction()) {
        |   ${dataClass.name}(
        |${dataClass.parameters.mapIndexed{index, parameter -> "        ${parameter.name}.factor${index+1}()" }.joinToString(",\n") }      
        |   )   
        |}
    """.trimMargin()
}

/**
 * Build the suspended setter:
 * suspend Data.set(setter: suspend Data.()->Product<..., suspend T_i.()->T_i ,...>): Data
 */
fun setterSuspended(dataClass: DataClass, generics: String?): String {
    val genericsOnClass = if(generics == null){""}else{"<$generics>"}

    return """suspend fun $genericsOnClass ${dataClass.name}$genericsOnClass.suspendSet(transaction: suspend ${dataClass.name}$genericsOnClass.()->${productSetterTypeSuspended(dataClass,generics)}): ${dataClass.name}$genericsOnClass = with(transaction()) {
        |   ${dataClass.name}(
        |${dataClass.parameters.mapIndexed{index, parameter -> "        ${parameter.name}.factor${index+1}()" }.joinToString(",\n") }      
        |   )   
        |}
    """.trimMargin()
}

/**
 * Build the transaction function
 *
 */
fun transaction(dataClass: DataClass, dimension: Int, generics: String?): String {
    val genericsOnClass = if(generics == null){""}else{"<$generics>"}
    return """fun $genericsOnClass ${dataClass.name}$genericsOnClass.transaction(transaction: Product2<${dataClass.name}$genericsOnClass,${productSetterType(dataClass,generics)}>.()->Product2<${dataClass.name}$genericsOnClass,${productSetterType(dataClass,generics)}>): ${dataClass.name}$genericsOnClass.()->${productSetterType(dataClass,generics)} 
        |    = {Product2(this@transaction, Product$dimension(${dataClass.parameters.reversed().joinToString(", ") { "idSetter<${it.type.name}>()" }})).transaction().factor1}
    """.trimMargin()
}

/**
 * Build the suspended transaction function
 */
fun transactionSuspended(dataClass: DataClass, dimension: Int, generics: String?): String {
    val genericsOnClass = if(generics == null){""}else{"<$generics>"}
    return """suspend fun $genericsOnClass ${dataClass.name}$genericsOnClass.suspendTransaction(transaction: suspend Product2<${dataClass.name}$genericsOnClass,${productSetterTypeSuspended(dataClass,generics)}>.()->Product2<${dataClass.name}$genericsOnClass,${productSetterTypeSuspended(dataClass,generics)}>): suspend ${dataClass.name}$genericsOnClass.()->${productSetterTypeSuspended(dataClass,generics)} 
        |    = {Product2(this@suspendTransaction, Product$dimension(${dataClass.parameters.reversed().joinToString(", ") { "idSetterSuspended<${it.type.name}>()" }})).transaction().factor1}
    """.trimMargin()
}

/**
 * Build parameter setters: One parameter setter for each parameter of the data class
 */
fun parameterSetters(dataClass: DataClass, generics: String?): String {
    return dataClass.parameters.mapIndexed{index,parameter -> parameterSetter(parameter.name,parameter.type.name,index,dataClass,dataClass.parameters.size,generics)}.joinToString("\n\n")
}

/**
 * Build suspended parameter setters: One suspended parameter setter for each parameter of the data class
 */
fun parameterSettersSuspended(dataClass: DataClass, generics: String?): String {
    return dataClass.parameters.mapIndexed{index,parameter -> parameterSetterSuspended(parameter.name,parameter.type.name,index,dataClass,dataClass.parameters.size,generics)}.joinToString("\n\n")
}

/**
 * Build parameter setter:
 * Product2<Data, Product<...,T_i.()->T_i,...>>.param(setter: T_i.()->T_i): Product2<Data, Product<...,T_i.()->T_i,...>>
 */
fun parameterSetter(parameterName: String,parameterType: String, parameterIndex: Int,dataClass: DataClass, dimension: Int, generics: String?) : String {
    val genericsOnClass = if(generics == null){""}else{"<$generics>"}
    return """fun $genericsOnClass Product2<${dataClass.name}$genericsOnClass, ${productSetterType(dataClass,generics)}>.$parameterName(setter: ${setter(parameterType)}): Product2<${dataClass.name}$genericsOnClass, ${productSetterType(dataClass,generics)}> = 
        |    Product2(this@${parameterName}.factor2,this@$parameterName.factor1.map${parameterIndex +1} {oldSetter -> oldSetter then setter})
    """.trimMargin()
}

/**
 * Build suspended parameter setter:
 * suspend Product2<Data, Product<..., suspend T_i.()->T_i,...>>.param(setter: suspend T_i.()->T_i): Product2<Data, Product<..., suspend T_i.()->T_i,...>>
 */
fun parameterSetterSuspended(parameterName: String,parameterType: String, parameterIndex: Int,dataClass: DataClass, dimension: Int, generics: String?) : String {
    val genericsOnClass = if(generics == null){""}else{"<$generics>"}
    return """suspend fun $genericsOnClass Product2<${dataClass.name}$genericsOnClass, ${productSetterTypeSuspended(dataClass,generics)}>.${parameterName}(setter: ${setterSuspended(parameterType)}): Product2<${dataClass.name}$genericsOnClass, ${productSetterTypeSuspended(dataClass,generics)}> = 
        |    Product2(this@${parameterName}.factor2,this@${parameterName}.factor1.suspendFluent({_,t->t}){ map${parameterIndex +1} {oldSetter -> oldSetter suspendThen setter}})
    """.trimMargin()
}


/**
 * Build product setter type: Product<...,T_i.()->T_i,...>
 */
fun productSetterType(dataClass: DataClass, generics: String?): String {
    val dimension = dataClass.parameters.size
    val genericsOnClass = if(generics == null){""}else{"<$generics>"}
    val setters = dataClass.parameters.reversed().joinToString(", ") { setter(it.type.name) }
    return """Product$dimension<$setters>"""

}

/**
 * Build suspended product setter type: Product<..., suspend T_i.()->T_i,...>
 */
fun productSetterTypeSuspended(dataClass: DataClass, generics: String?): String {
    val dimension = dataClass.parameters.size
    val genericsOnClass = if(generics == null){""}else{"<$generics>"}
    val setters = dataClass.parameters.reversed().joinToString(", ") { setterSuspended(it.type.name) }
    return """Product$dimension<$setters>"""

}

/**********************************************************************************************************************
 *
 * Auxiliary functions
 *
 **********************************************************************************************************************/

/**
 *
 */
fun file(dataClass: DataClass, project: Project): File = when(dataClass.sourceFolder){
    "" -> File(project.projectDir, "src/generated/kotlin/${packageName(dataClass).replace(".","/")}/${dataClass.name}.kt".replace("//","/"))
    else -> File(project.projectDir, "${dataClass.sourceFolder}/${packageName(dataClass).replace(".","/")}/${dataClass.name}.kt".replace("//","/"))
}

/**
 * Build setter: T.()->T
 */
fun setter(type: String): String = "$type.()->$type"

/**
 * Build suspended setter: suspend T.()->T
 */
fun setterSuspended(type: String): String = "suspend $type.()->$type"

/**
 *
 */
fun buildIdSetter(): String = "fun <T> idSetter(): T.()->T = {this}"

/**
 *
 */
fun buildSuspendedIdSetter(): String = "suspend fun <T> idSetterSuspended(): suspend T.()->T = {this}"

/**
 *
 */
fun buildExtendedFunctionComposer(): String = """infix fun <R,S,T> (R.()->S).then(f: S.()->T): R.()->T = {this.this@then().f()}
    |
    |suspend infix fun <R,S,T> (suspend R.()->S).suspendThen(f: suspend  S.()->T): suspend R.()->T = {this.this@then().f()}
""".trimMargin()

/**
 *
 */
fun buildFluentFunction(): String = """fun <S,T> S.fluent(merge: (S,T)-> S = {s,_ -> s}, block: S.()->T): S = merge(this,block())
    |
    |fun <S,T> S.suspendFluent(merge: suspend (S,T)-> S = {s,_ -> s}, block: suspend S.()->T): S = merge(this,block())
""".trimMargin()