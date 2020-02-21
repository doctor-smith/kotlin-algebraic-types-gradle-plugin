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
package org.drx.plugin.algebraictypes.generate.optics

import org.drx.plugin.algebraictypes.*
import org.drx.plugin.algebraictypes.config.Config
import org.drx.plugin.algebraictypes.config.Config.Defaults as Defaults
import org.drx.plugin.algebraictypes.extension.*
import org.drx.plugin.algebraictypes.generate.*
import org.drx.plugin.algebraictypes.generate.products.generateProductType
import org.drx.plugin.algebraictypes.generate.serialization.buildSerialModuleName
import org.drx.plugin.algebraictypes.generate.serialization.serialModule
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
    file.writeText(buildAuxiliaryFunctions())

    generateDslMarker(project)

    // generate products in all needed dimensions
    with(dataClasses.dataClasses.map {
        it.parameters.size
    }.toHashSet()){
        add(2)
        forEach {
            generateProductType(it, project)
        }
    }


    // generate classes
    dataClasses.dataClasses.forEach {
        generatePseudoLens(it, project)
    }
}

/**
 * Generate pseudo lens
 */
fun generatePseudoLens(dataClass: DataClass, project: Project) {
    // build string representation and save it
    with(file(dataClass, project)){
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
        |
        |${buildComment(dataClass.comment)}
        |${dataClassRepresentation(dataClass, generics)}
        |${if(dataClass.serializable){"\n${serialModule(dataClass)}"}else{""}}
        |
        |${buildSectionComment("Pseudo lens structure of ${dataClass.name}")}
        |${buildParaGraphComment("Transaction based operations")}
        |${setter(dataClass, generics)}
        |
        |${transaction(dataClass, dataClass.parameters.size, generics)}
        |
        |${parameterSetters(dataClass, generics)} 
        |
        |${buildParaGraphComment("Simple setters")}
        |${simpleParameterSetters(dataClass, generics)}
        |
        |${buildSectionComment("Suspended pseudo lens structure of ${dataClass.name}")}
        |${buildParaGraphComment("Transaction based suspended operations")}
        |${setterSuspended(dataClass, generics)}
        |
        |${transactionSuspended(dataClass, dataClass.parameters.size, generics)}
        |
        |${parameterSettersSuspended(dataClass, generics)}
        |
        |${buildParaGraphComment("Simple suspended setters")}
        |${simpleParameterSettersSuspended(dataClass, generics)}
        |
    """.trimMargin()
}



/**
 * Data class may depend on types which need to be imported.
 * Compute the imports of the data class.
 */
fun imports(dataClass: DataClass): String {
    val dimension = dataClass.parameters.size
    val imports = hashSetOf(
        "org.drx.generated.AlgebraicTypesDsl",
        "org.drx.generated.lenses.*",
        "org.drx.generated.products.*"
    )
    if(dataClass.serializable) {
        imports.addAll( Config.Serialization.imports )
    }
    imports.addAll(dataClass.parameters.map{
        with(hashSetOf<String>()){
            if(!it.type.name.contains("<")) {
                add(it.type.packageName + "." + it.type.name.nonNullable())
            }
            if(it.type.serializable && dataClass.serializable && !it.type.name.contains("<")) {
                add(it.type.packageName + "." + buildSerialModuleName(it.type.name))
            }
            it.type.dependencies.forEach {dependency ->
                add(dependency.packageName + "." + dependency.name.nonNullable())
                if(!it.type.serializable && dataClass.serializable && dependency.serializable) {
                    add(dependency.packageName + "." + buildSerialModuleName(dependency.name))
                }
            }
            this
        }
    }.flatten())
    return imports.filter{it != ""&& !it.startsWith(".")}.map{"import $it"}.sortedWith(Comparator<String> { o1, o2 -> o1.compareTo(o2) })
            .joinToString("\n")
}

/**
 * Build the representation of the data class
 */
fun dataClassRepresentation(dataClass: DataClass, generics: String?): String {
    fun serializableTypeDependencies(parameter: Parameter): String {
        var param = parameter.type.name
        if(parameter.type.dependencies.isNotEmpty()) {
            parameter.type.dependencies.forEach {
                if(it.serializationType is SerializationType.Polymorphic) {
                    param = param.replace(it.name+",", "@Polymorphic ${it.name},")
                            .replace(it.name+">", "@Polymorphic ${it.name}>")

                }
            }
            return param
        }
        return param
    }

    fun parameter(parameter: Parameter): String {
        return ""
    }

    fun parameters(): String = dataClass.parameters
            .joinToString(",\n", "", "") {
                "    ${if(it.type.serializationType is SerializationType.Transient){"@Transient "}else{""}}val ${it.name}: ${if(it.type.serializationType is SerializationType.Polymorphic){"@Polymorphic "}else{""}}${serializableTypeDependencies(it)}${if(it.defaultValue == null){""}else{" = ${it.defaultValue!!}"}}"
            }



    return """
        |${if(dataClass.serializable){"@Serializable\n"}else{""}}data class ${dataClass.name}${if(generics!=null){"<$generics>"}else{""}}(
        |${parameters()}
        |)
    """.trimMargin()
}

/**
 * Data class may depend on generic parameters:
 */
fun generics(dataClass: ClassRepresentation): String? {
    val result = dataClass.parameters
            .asSequence()
            .filter { it.type.isGeneric }
            .map{when(it.type.genericIn){
                "" -> listOf(it.type.name)
                else -> it.type.genericIn.split(",").map{term -> term.trim()}
            }}
            .flatten()
            .toSet()
            .joinToString(", ")
    if(result == "") {
        return null
    }
    return result
}

fun listGenerics(clazz: ClassRepresentation) : HashSet<String> = clazz.parameters
        .asSequence()
        .filter { it.type.isGeneric }
        .map{when(it.type.genericIn){
            "" -> listOf(it.type.name)
            else -> it.type.genericIn.split(",").map{term -> term.trim()}
        }}
        .flatten()
        .toHashSet()

fun classGenerics(generics: String?) : String = if(generics == null){""}else{"<$generics>"}

fun functionGenerics(generics: String?) : String = if(generics == null){""}else{"<$generics> "}

/**
 * Build the setter:
 * Data.set(setter: Data.()->Product<..., T_i.()->T_i ,...>): Data
 */
fun setter(dataClass: DataClass, generics: String?): String {
    val genericsOnClass = if(generics == null){""}else{"<$generics>"}
    val genericsOnFun = if(generics == null){""}else{"<$generics> "}
    return """${buildComment("Setter function")}@AlgebraicTypesDsl
        |fun $genericsOnFun${dataClass.name}$genericsOnClass.set${if(dataClass.settersPostFix != ""){dataClass.settersPostFix}else{""}}(transaction: ${dataClass.name}$genericsOnClass.()->${productSetterType(dataClass, generics)}): ${dataClass.name}$genericsOnClass = with(transaction()) {
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
    val genericsOnFun = if(generics == null){""}else{"<$generics> "}
    return """${buildComment("Suspended setter function")}@AlgebraicTypesDsl
        |suspend fun $genericsOnFun${dataClass.name}$genericsOnClass.suspendSet${if(dataClass.settersPostFix != ""){dataClass.settersPostFix}else{""}}(transaction: suspend ${dataClass.name}$genericsOnClass.()->${productSetterTypeSuspended(dataClass, generics)}): ${dataClass.name}$genericsOnClass = with(transaction()) {
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
/*
fun transaction(dataClass: DataClass, dimension: Int, generics: String?): String {
    val genericsOnClass = if(generics == null){""}else{"<$generics>"}
    val genericsOnFun = if(generics == null){""}else{"<$generics> "}
    val postFix  = if(dataClass.settersPostFix != ""){dataClass.settersPostFix}else{""}
    return """${buildComment("Transaction function")}fun $genericsOnFun${dataClass.name}$genericsOnClass.transaction${postFix}(transaction: Product2<${dataClass.name}$genericsOnClass,${productSetterType(dataClass,generics)}>.()->Product2<${dataClass.name}$genericsOnClass,${productSetterType(dataClass,generics)}>): ${dataClass.name}$genericsOnClass.()->${productSetterType(dataClass,generics)}
        |    = {Product2(this@transaction${postFix}, Product$dimension(${dataClass.parameters.reversed().joinToString(", ") { "idSetter<${it.type.name}>()" }})).transaction().factor1}
    """.trimMargin()
}
*/
fun transaction(dataClass: DataClass, dimension: Int, generics: String?): String {
    val genericsOnClass = if(generics == null){""}else{"<$generics>"}
    val genericsOnFun = if(generics == null){""}else{"<$generics> "}
    val postFix  = if(dataClass.settersPostFix != ""){dataClass.settersPostFix}else{""}
    return """${buildComment("Transaction function")}@AlgebraicTypesDsl
        |fun $genericsOnFun${dataClass.name}$genericsOnClass.transaction${postFix}(transaction: ${productSetterType(dataClass, generics)}.()->${productSetterType(dataClass, generics)}): ${dataClass.name}$genericsOnClass.()->${productSetterType(dataClass, generics)} 
        |    = {Product$dimension(${dataClass.parameters.reversed().joinToString(", ") { "idSetter<${it.type.name}>()" }}).transaction()}
    """.trimMargin()
}

/**
 * Build the suspended transaction function
 */
/*

fun transactionSuspended(dataClass: DataClass, dimension: Int, generics: String?): String {
    val genericsOnClass = if(generics == null){""}else{"<$generics>"}
    val genericsOnFun = if(generics == null){""}else{"<$generics> "}
    val postFix  = if(dataClass.settersPostFix != ""){dataClass.settersPostFix}else{""}
    return """${buildComment("Suspended transaction function")}suspend fun $genericsOnFun${dataClass.name}$genericsOnClass.suspendTransaction${postFix}(transaction: suspend Product2<${dataClass.name}$genericsOnClass,${productSetterTypeSuspended(dataClass,generics)}>.()->Product2<${dataClass.name}$genericsOnClass,${productSetterTypeSuspended(dataClass,generics)}>): suspend ${dataClass.name}$genericsOnClass.()->${productSetterTypeSuspended(dataClass,generics)}
        |    = {Product2(this@suspendTransaction${postFix}, Product$dimension(${dataClass.parameters.reversed().joinToString(", ") { "idSetterSuspended<${it.type.name}>()" }})).transaction().factor1}
    """.trimMargin()
}*/

fun transactionSuspended(dataClass: DataClass, dimension: Int, generics: String?): String {
    val genericsOnClass = if(generics == null){""}else{"<$generics>"}
    val genericsOnFun = if(generics == null){""}else{"<$generics> "}
    val postFix  = if(dataClass.settersPostFix != ""){dataClass.settersPostFix}else{""}
    return """${buildComment("Suspended transaction function")}@AlgebraicTypesDsl
        |suspend fun $genericsOnFun${dataClass.name}$genericsOnClass.suspendTransaction${postFix}(transaction: suspend ${productSetterTypeSuspended(dataClass, generics)}.()->${productSetterTypeSuspended(dataClass, generics)}): suspend ${dataClass.name}$genericsOnClass.()->${productSetterTypeSuspended(dataClass, generics)} 
        |    = {Product$dimension(${dataClass.parameters.reversed().joinToString(", ") { "idSetterSuspended<${it.type.name}>()" }}).transaction()}
    """.trimMargin()
}

fun simpleParameterSetters(dataClass: DataClass, generics: String?): String {
    val postFix  = if(dataClass.settersPostFix != ""){dataClass.settersPostFix}else{""}
    return dataClass.parameters.mapIndexed{index,parameter -> simpleParameterSetter(parameter.name + postFix, parameter.type.name, index, dataClass, dataClass.parameters.size, generics) }.joinToString("\n\n")
}

fun simpleParameterSettersSuspended(dataClass: DataClass, generics: String?): String {
    val postFix  = if(dataClass.settersPostFix != ""){dataClass.settersPostFix}else{""}
    return dataClass.parameters.mapIndexed{index,parameter -> simpleParameterSetterSuspended(parameter.name + postFix, parameter.type.name, index, dataClass, dataClass.parameters.size, generics) }.joinToString("\n\n")
}

/**
 * Build parameter setters: One parameter setter for each parameter of the data class
 */
fun parameterSetters(dataClass: DataClass, generics: String?): String {
    val postFix  = if(dataClass.settersPostFix != ""){dataClass.settersPostFix}else{""}
    return dataClass.parameters.mapIndexed{index,parameter -> parameterSetter(parameter.name + postFix, parameter.type.name, index, dataClass, dataClass.parameters.size, generics) }.joinToString("\n\n")
}

/**
 * Build suspended parameter setters: One suspended parameter setter for each parameter of the data class
 */
fun parameterSettersSuspended(dataClass: DataClass, generics: String?): String {
    val postFix  = if(dataClass.settersPostFix != ""){dataClass.settersPostFix}else{""}
    return dataClass.parameters.mapIndexed{index,parameter -> parameterSetterSuspended(parameter.name + postFix, parameter.type.name, index, dataClass, dataClass.parameters.size, generics) }.joinToString("\n\n")
}

fun simpleParameterSetter(parameterName: String, parameterType: String, parameterIndex: Int, dataClass: DataClass, dimension: Int, generics: String?): String {
    val genericsOnClass = if(generics == null){""}else{"<$generics>"}
    val genericsOnFun = if(generics == null){""}else{"<$generics> "}
    return """${buildComment("Simple parameter setter function for parameter '${parameterName}'")}@AlgebraicTypesDsl
        |fun ${genericsOnFun}${dataClass.name}${genericsOnClass}.${parameterName}(set: ${parameterType}.()->${parameterType}): ${dataClass.name}${genericsOnClass}
        |${Defaults.offset}= copy(${parameterName} = ${parameterName}.set()) 
    """.trimMargin()
}

// suspend${parameterName.substring(0,1).toUpperCase()+parameterName.substring(1)}
fun simpleParameterSetterSuspended(parameterName: String, parameterType: String, parameterIndex: Int, dataClass: DataClass, dimension: Int, generics: String?): String {
    val genericsOnClass = if(generics == null){""}else{"<$generics>"}
    val genericsOnFun = if(generics == null){""}else{"<$generics> "}
    return """${buildComment("Simple suspended parameter setter function for parameter '${parameterName}'")}@AlgebraicTypesDsl
        |suspend fun ${genericsOnFun}${dataClass.name}${genericsOnClass}.suspend${parameterName.substring(0,1).toUpperCase()+parameterName.substring(1)}(set: suspend ${parameterType}.()->${parameterType}): ${dataClass.name}${genericsOnClass}
        |${Defaults.offset}= copy(${parameterName} = ${parameterName}.set()) 
    """.trimMargin()
}


/**
 * Build parameter setter:
 * Product2<Data, Product<...,T_i.()->T_i,...>>.param(setter: T_i.()->T_i): Product2<Data, Product<...,T_i.()->T_i,...>>
 */
/*
fun parameterSetter(parameterName: String,parameterType: String, parameterIndex: Int,dataClass: DataClass, dimension: Int, generics: String?) : String {
    val genericsOnClass = if(generics == null){""}else{"<$generics>"}
    val genericsOnFun = if(generics == null){""}else{"<$generics> "}
    return """${buildComment("Parameter setter function for parameter '${parameterName}'")}fun ${genericsOnFun}Product2<${dataClass.name}$genericsOnClass, ${productSetterType(dataClass,generics)}>.$parameterName(setter: ${setter(parameterType)}): Product2<${dataClass.name}$genericsOnClass, ${productSetterType(dataClass,generics)}> =
        |    Product2(this@${parameterName}.factor2,this@$parameterName.factor1.map${parameterIndex +1} {oldSetter -> oldSetter then setter})
    """.trimMargin()
}

 */


fun parameterSetter(parameterName: String, parameterType: String, parameterIndex: Int, dataClass: DataClass, dimension: Int, generics: String?) : String {
    val genericsOnClass = if(generics == null){""}else{"<$generics>"}
    val genericsOnFun = if(generics == null){""}else{"<$generics> "}
    return """${buildComment("Parameter setter function for parameter '${parameterName}'")}@AlgebraicTypesDsl
        |fun ${genericsOnFun}${productSetterType(dataClass, generics)}.$parameterName(setter: ${setter(parameterType)}): ${productSetterType(dataClass, generics)} = 
        |    this@$parameterName.map${parameterIndex +1} {oldSetter -> oldSetter then setter}
    """.trimMargin()
}

/**
 * Build suspended parameter setter:
 * suspend Product2<Data, Product<..., suspend T_i.()->T_i,...>>.param(setter: suspend T_i.()->T_i): Product2<Data, Product<..., suspend T_i.()->T_i,...>>
 */
/*
fun parameterSetterSuspended(parameterName: String,parameterType: String, parameterIndex: Int,dataClass: DataClass, dimension: Int, generics: String?) : String {
    val genericsOnClass = if(generics == null){""}else{"<$generics>"}
    val genericsOnFun = if(generics == null){""}else{"<$generics> "}
    return """${buildComment("Suspended parameter setter function for parameter '${parameterName}'")}suspend fun ${genericsOnFun}Product2<${dataClass.name}$genericsOnClass, ${productSetterTypeSuspended(dataClass,generics)}>.${parameterName}(setter: ${setterSuspended(parameterType)}): Product2<${dataClass.name}$genericsOnClass, ${productSetterTypeSuspended(dataClass,generics)}> =
        |    Product2(this@${parameterName}.factor2,this@${parameterName}.factor1.suspendFluent({_,t->t}){ map${parameterIndex +1} {oldSetter -> oldSetter suspendThen setter}})
    """.trimMargin()
}

 */

fun parameterSetterSuspended(parameterName: String, parameterType: String, parameterIndex: Int, dataClass: DataClass, dimension: Int, generics: String?) : String {
    val genericsOnClass = if(generics == null){""}else{"<$generics>"}
    val genericsOnFun = if(generics == null){""}else{"<$generics> "}
    return """${buildComment("Suspended parameter setter function for parameter '${parameterName}'")}@AlgebraicTypesDsl
        |suspend fun ${genericsOnFun}${productSetterTypeSuspended(dataClass, generics)}.${parameterName}(setter: ${setterSuspended(parameterType)}): ${productSetterTypeSuspended(dataClass, generics)} = 
        |    this@${parameterName}.suspendFluent({_,t->t}){ map${parameterIndex +1} {oldSetter -> oldSetter suspendThen setter}}
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
 * Compute the package name
 */
fun packageName(dataClass: DataClass) :String = when(dataClass.packageName) {
    "" -> "org.drx.generated.lenses"
    else -> dataClass.packageName
}

/**
 *
 */
fun file(dataClass: DataClass, project: Project): File = when(dataClass.sourceFolder){
    "" -> File(project.projectDir, "src/generated/kotlin/${packageName(dataClass).replace(".","/")}/${dataClass.name}.kt".replace("//","/"))
    else -> File(project.projectDir, "${dataClass.sourceFolder}/${packageName(dataClass).replace(".","/")}/${dataClass.name}.kt".replace("//","/"))
}

fun buildAuxiliaryFunctions(): String = """${license()}
    |
    |package org.drx.generated.lenses
    |
    |${buildIdSetter()}
    |
    |${buildSuspendedIdSetter()}
    |
    |${buildExtendedFunctionComposer()}
    |
    |${buildFluentFunction()}
""".trimMargin()


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
    |infix fun <R,S,T> (suspend R.()->S).suspendThen(f: suspend  S.()->T): suspend R.()->T = {this.this@suspendThen().f()}
""".trimMargin()

/**
 *
 */
fun buildFluentFunction(): String = """fun <S,T> S.fluent(merge: (S,T)-> S = {s,_ -> s}, block: S.()->T): S = merge(this,block())
    |
    |suspend fun <S,T> S.suspendFluent(merge: suspend (S,T)-> S = {s,_ -> s}, block: suspend S.()->T): S = merge(this,block())
""".trimMargin()