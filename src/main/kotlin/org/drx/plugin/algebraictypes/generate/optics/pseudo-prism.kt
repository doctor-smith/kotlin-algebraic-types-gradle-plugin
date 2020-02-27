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
import org.drx.plugin.algebraictypes.generate.sums.generateSumType
import org.gradle.api.Project
import org.drx.plugin.algebraictypes.util.repeatString
import java.io.File


// TODO Support generic types on sealed classes and their representatives
// TODO Support nested sealed classes
// TODO Support other representatives than data classes
// TODO Support kotlin serialization
fun generatePseudoPrisms(dataClasses: DataClasses, project: Project) {
    val dir = File("${project.projectDir}$basePath/prisms")
    if(!dir.exists()) {
        dir.mkdirs()
    }
    val file = File("${project.projectDir}$basePath/prisms/functions.kt")
    file.writeText(buildAuxiliaryPrismFunctions())

    generateDslMarker(project)

    // generate sums and products in all needed dimensions
    // sums
    with(dataClasses.sealedClasses.map {
        it.representatives.size
    }.toHashSet()){
        //add(2)
        forEach {
            generateSumType(it, project)
        }
    }

    // products
    with(dataClasses.sealedClasses.map{clazz ->
        clazz.representatives.map{ representative -> representative.parameters.size }
    }.flatten().toHashSet()){
        add(2)
        forEach {
            generateProductType(it, project)
        }
    }

    // build dependency

    // generate prisms
    dataClasses.sealedClasses.forEach {
        generatePseudoPrism(it, project)
    }
}



/**
 * Generate pseudo lens
 */
fun generatePseudoPrism(sealedClass: SealedClass, project: Project) {
    // build string representation and save it
    with(file(sealedClass, project)){
        if(!exists()) {
            parentFile.mkdirs()
            createNewFile()

        }
        writeText(buildSealedClassFileContent(sealedClass))
    }
}

/**
 * Sealed class may depend on types which need to be imported.
 * Compute the imports of the sealed class.
 */
fun imports(dataClass: SealedClass): String {
    val dimension = dataClass.parameters.size
    val imports = hashSetOf(
            "org.drx.generated.AlgebraicTypesDsl",
            "org.drx.generated.prisms.*",
            "org.drx.generated.products.*",
            "org.drx.generated.sums.*"
    )
    if(dataClass.serializable) {
        imports.addAll( Config.Serialization.imports )
    }
    imports.addAll(dataClass.parameters.map{
        with(hashSetOf<String>()){
            add(it.type.packageName + "." + it.type.name.fixTypeNameForImport().nonNullable())

            // serailization
            if(it.type.serializable && dataClass.serializable) {
                add(it.type.packageName + "." + buildSerialModuleName(it.type.name))
            }
            it.type.dependencies.forEach {dependency ->
                add(dependency.packageName + "." + dependency.name.fixTypeNameForImport().nonNullable())
                if(!it.type.serializable && dataClass.serializable && dependency.serializable) {
                    add(dependency.packageName + "." + buildSerialModuleName(dependency.name))
                }
            }
            this
        }
    }.flatten())
    dataClass.representatives.forEach {representative ->
        representative.parameters.forEach {
            imports.add(it.type.packageName + "." + it.type.name)

            // serialization
            if(it.type.serializable && dataClass.serializable) {
                imports.add(it.type.packageName + "." + buildSerialModuleName(it.type.name))
            }
            it.type.dependencies.forEach {dependency ->
                imports.add(dependency.packageName + "." + dependency.name)
                if(!it.type.serializable && dataClass.serializable && dependency.serializable) {
                    imports.add(dependency.packageName + "." + buildSerialModuleName(dependency.name))
                }
            }
        }
    }
    return imports.filter{it != "" && !it.startsWith(".")}.map{"import $it"}.sortedWith(Comparator<String> { o1, o2 -> o1.compareTo(o2) })
            .joinToString("\n")
}

fun buildSealedClassFileContent(sealedClass: SealedClass): String {
    return """${license()}
        |
        |package ${packageName(sealedClass)}
        |
        |${imports(sealedClass)}
        |
        |${classRepresentation((sealedClass))}
        |${if(sealedClass.serializable){"\n${serialModule(sealedClass)}"}else{""}}
        |${prism(sealedClass)}
        |
        |${subClassLenses(sealedClass)}
    """.trimMargin()
}

/**********************************************************************************************************************
 *
 * Class representaions
 *
 **********************************************************************************************************************/


fun classRepresentation(sealedClass: SealedClass, offset: String = ""): String {
    val generics = classGenerics(generics(sealedClass))
    return """
        |${buildComment(sealedClass.comment, offset)}${offset}${if(sealedClass.serializable){"@Serializable\n$offset"}else{""}}sealed class ${sealedClass.name}${generics}${if(sealedClass.parameters.isNotEmpty()){
        parameters(sealedClass, offset + Defaults.offset).joinToString(",\n"," (\n","$offset) {")}else{" {"}}
        |${subClassRepresentations(sealedClass, offset + Defaults.offset)}   
        |${offset}}
    """.trimMargin()
}

fun subClassRepresentations(sealedClass: SealedClass, offset: String = ""): String {

    return """ 
        |${sealedClass.representatives.joinToString("\n\n") { subClassRepresentation(it, offset) }}
    """.trimMargin()
}
fun subClassRepresentation(subClass: SubClass, offset: String = ""): String = when(subClass){
    is SubDataClass -> subClassRepresentation(subClass, offset)
    is SubSealedClass -> subClassRepresentation(subClass, offset)
    is SubObject -> subClassRepresentation(subClass, offset)
    else -> TODO() // standard class rep
}

fun subClassRepresentation(subClass: SubDataClass, offset: String = ""): String {
    val generics = classGenerics(generics(subClass))
    // TODO compute generics
    val parentalGenerics = classGenerics(generics(subClass.parent))
    return """
        |${buildComment(subClass.comment, offset)}${offset}${if(subClass.parent.serializable){"@Serializable\n$offset"}else{""}}data class ${subClass.name}$generics(
        |${subClass.parameters.joinToString( ",\n"+ offset + Defaults.offset, offset + Defaults.offset){ 
            "${if(subClass.overrideParameters.contains(it.name)){"override "}else{""}}val ${it.name} : ${it.type.name}${if(it.defaultValue != null){" = ${it.defaultValue}"}else{""}}"
        }}
        |${offset}): ${subClass.parent.name}$parentalGenerics${if(subClass.parent.parameters.isNotEmpty()){"(\n"+subClass.parent.parameters.joinToString(",\n"+ offset + Defaults.offset, offset + Defaults.offset, "\n${offset})") { if(subClass.defaultValuesSet[it.name] != null ){subClass.defaultValuesSet[it.name]!!} else {it.name} } }else{"()"}}
    """.trimMargin()
}
fun subClassRepresentation(subClass: SubObject, offset: String = ""): String = TODO()
fun subClassRepresentation(subClass: SubSealedClass, offset: String = ""): String = TODO()

/**********************************************************************************************************************
 *
 * Prism structure
 *
 **********************************************************************************************************************/


fun prism(sealedClass: SealedClass): String {
    return """
        |${buildSectionComment("Prism structure")}
        |${buildParaGraphComment("Prism structure of ${sealedClass.name}")}
        |${prismSetter(sealedClass)}
        |
        |${prismTransaction(sealedClass)}
        |
        |${prismParameterSetters(sealedClass)}
        |
        |
        |${buildParaGraphComment("Suspended prism structure of ${sealedClass.name}")}
        |${prismSetter(sealedClass, true)}
        |
        |${prismTransaction(sealedClass, true)}
        |
        |${prismParameterSetters(sealedClass, true)}
    """.trimMargin()
}

/**
 * Prism setter function
 * ====================================================================================================================
 */

fun prismSetter(sealedClass: SealedClass, suspended: Boolean = false): String {

    val funGenerics = prismSetterFunctionGenerics(sealedClass)
    val classGenerics = classGenerics(generics(sealedClass))

    return """
        |${buildComment(listOf("Prism setter function"), "", null)}
        |@AlgebraicTypesDsl
        |${suspended(suspended)}fun $funGenerics${sealedClass.name}$classGenerics.${suspended(suspended, "Set", "set")}(transaction: ${prismSetterType(sealedClass, suspended)}): ${sealedClass.name}$classGenerics = with(transaction()) {
        |${Defaults.offset} when(this) {
        |${prismSetterCases(sealedClass, suspended)}
        |${Defaults.offset}}
        |}
    """.trimMargin()
}

fun prismSetterCases(sealedClass: SealedClass, suspended: Boolean = false): String {
    val dimension = sealedClass.representatives.size

    return """
        |${sealedClass.representatives.mapIndexed{index,it -> prismSetterCase(it as SubDataClass, index + 1, dimension, suspended) }.joinToString("\n")}
    """.trimMargin()
}
fun prismSetterCase(representative: SubDataClass, index: Int, dimension: Int, suspended: Boolean = false): String {
    val offset = Defaults.offset

    fun constructorParameters(clazz: SubDataClass): String {
        return if(clazz.parameters.isEmpty()) {
            ""
        } else {
            val innerOffset = offset.repeatString(4)
            clazz.parameters.mapIndexed{
                i: Int, parameter: Parameter -> "${innerOffset}${if(parameter.type.isGeneric){"(${parameter.name} as ${parameter.type.name})"}else{parameter.name}}.factor${i + 1}()"
            }.joinToString(",\n","\n","\n${offset.repeatString(3)}")
        }
    }

    fun generics(representation: ClassRepresentation): String {
        val gen = listGenerics(representative).joinToString(", ", "<", ">") { "*" }
        return if(gen == "<>"){
            ""
        } else {
            gen
        }
    }
    return """
        |${offset.repeatString(2) }is Sum${dimension}.Summand${index} -> with( value ) { when( this@${suspended(suspended, "Set", "set")} ) {
        |${offset.repeatString(3) }is ${representative.parent.name}.${representative.name}${generics(representative)} -> ${representative.parent.name}.${representative.name}(${constructorParameters(representative)}) 
        |${offset.repeatString(3) }else -> this@${suspended(suspended, "Set", "set")}
        |${offset.repeatString(2)}} }
    """.trimMargin()
}




/**
 * Prism transaction function
 * ====================================================================================================================
 */

fun prismTransaction(sealedClass: SealedClass, suspended: Boolean = false): String {
    val offset = Defaults.offset
    val funGenerics = prismTransactionFunctionGenerics(sealedClass)
    val classGenerics = classGenerics(generics(sealedClass))

    return """
        ||${buildComment(listOf("Prism transaction function"), "", null)}
        |@AlgebraicTypesDsl
        |${suspended(suspended)}fun $funGenerics${sealedClass.name}$classGenerics.${suspended(suspended, "Transaction", "transaction")}(transaction: ${prismTransactionArgumentType(sealedClass, suspended)}): ${prismSetterType(sealedClass, suspended)} = { 
        |${offset.repeatString(1)}when( this@${suspended(suspended, "Transaction", "transaction")} ) {
        |${prismTransactionCases(sealedClass, suspended)}
        |${offset.repeatString(1)}}.transaction()
        |}
    """.trimMargin()
}

fun prismTransactionCases(sealedClass: SealedClass, suspended: Boolean = false): String {
    val dimension = sealedClass.representatives.size
    return """
        |${sealedClass.representatives.mapIndexed{index, representative -> prismTransactionCase(representative, index + 1, suspended) }.joinToString("\n")}
    """.trimMargin()
}

fun prismTransactionCase(representative: SubClass, index:Int, suspended: Boolean = false): String{
    val offset = Defaults.offset
    val productDimension = representative.parameters.size
    val idSetter = "${if(suspended){"idSetterSuspended"}else{"idSetter"}}()"

    fun generics(representation: ClassRepresentation): String {
        val gen = listGenerics(representative).joinToString(", ", "<", ">") { "*" }
        return if(gen == "<>"){
            ""
        } else {
            gen
        }
    }

    return "${offset.repeatString(3)}is ${representative.parent.name}.${representative.name}${generics(representative)} -> ${prismSummandType(representative.parent as SealedClass, index, suspended)}(Product$productDimension(${IntRange(1,productDimension).joinToString(", ") { idSetter }}))"
}


/**
 * Prism parameter-setter functions
 * ====================================================================================================================
 */

fun prismParameterSetters(sealedClass: SealedClass, suspended: Boolean = false): String {
    val sharedPrismParameters = collectSharedPrismParameters(sealedClass)
    return """
        |${sharedPrismParameters.joinToString("\n\n") { sharedPrismParameterSetter(sealedClass, it, suspended) }}
    """.trimMargin()
}


fun sharedPrismParameterSetter(sealedClass: SealedClass, sharedPrismParameter: SharedPrismParameter, suspended: Boolean = false): String {
    val type = prismParameterSetterType(sealedClass, suspended)
    val offset = Defaults.offset
    val funGenerics = prismParameterSetterFunctionGenerics(sealedClass)
    val classGenerics = classGenerics(generics(sealedClass))

    return """
        |${buildComment(listOf("Shared prism parameter setter function for", "parameter '${sharedPrismParameter.name}'"), "", null)}
        |@AlgebraicTypesDsl
        |${suspended(suspended)}fun $funGenerics${type}.${sharedPrismParameter.name}(setter: ${suspended(suspended)}${setter(sharedPrismParameter.type)}): $type =
        |${offset}when( val summand = this ) {
        |${sharedPrismParameterSetterCases(sealedClass, sharedPrismParameter.occurrences, suspended)}
        |${offset}}
        |
        """.trimMargin()
}

fun sharedPrismParameterSetterCases(sealedClass: SealedClass, occurrences: ArrayList<PrismParameterOccurrence>, suspended: Boolean): String {
    val dimension = sealedClass.representatives.size
    val offset = Defaults.offset
    return """
        |${occurrences.joinToString("\n") { sharedPrismParameterSetterCase(it, dimension, suspended) }}
        |${offset.repeatString(2)}else -> this
    """.trimMargin()
}

fun sharedPrismParameterSetterCase(occurrence: PrismParameterOccurrence, dimension: Int, suspended: Boolean): String {
    val offset = Defaults.offset
    return when(suspended) {
        false -> """
                    |${offset.repeatString(2)}is Sum${dimension}.Summand${occurrence.clazzIndex + 1} -> Sum${dimension}.Summand${occurrence.clazzIndex + 1}(summand.value.map${occurrence.parameterIndex + 1} {oldSetter -> oldSetter then setter})
                """.trimMargin()
        true -> """
                    |${offset.repeatString(2)}is Sum${dimension}.Summand${occurrence.clazzIndex + 1} -> Sum${dimension}.Summand${occurrence.clazzIndex + 1}(summand.value.suspendFluent({_,t->t}){ map${occurrence.parameterIndex + 1} {oldSetter -> oldSetter suspendThen setter} })
                
                """.trimMargin()
    }

}

/**
 * Prism relevant types
 * ====================================================================================================================
 */

fun prismSumType(sealedClass: SealedClass, suspended: Boolean = false): String {
    val sumDimension = sealedClass.representatives.size
    return "Sum$sumDimension<${sealedClass.representatives.reversed().joinToString(", ") {
        val dataClass = (it as SubDataClass).toDataClass()
        when (suspended) {
            false -> productSetterType(dataClass, null)
            true -> productSetterTypeSuspended(dataClass, null)
        }
    }}>"
}

fun prismSummandType(sealedClass: SealedClass, index: Int, suspended: Boolean = false): String {
    val sumDimension = sealedClass.representatives.size
    return "Sum${sumDimension}.Summand$index<${sealedClass.representatives.reversed().joinToString(", ") {
        val dataClass = (it as SubDataClass).toDataClass()
        when (suspended) {
            false -> productSetterType(dataClass, null)
            true -> productSetterTypeSuspended(dataClass, null)
        }
    }}>"
}

fun prismSetterType(sealedClass: SealedClass, suspended: Boolean = false): String {
    val classGenerics = classGenerics(generics(sealedClass))
    return "${suspended(suspended)}${sealedClass.name}${classGenerics}.()->${prismSumType(sealedClass, suspended)}"
}


fun prismTransactionArgumentType(sealedClass: SealedClass, suspended: Boolean = false): String =
        with(prismParameterSetterType(sealedClass, suspended)){
            "${suspended(suspended)}$this.()->$this"
        }

fun prismParameterSetterType(sealedClass: SealedClass, suspended: Boolean = false): String {
    return "${prismSumType(sealedClass, suspended)}"
}

fun collectSharedPrismParameters(sealedClass: SealedClass): ArrayList<SharedPrismParameter> =
    sealedClass.representatives.mapIndexed {
        clazzIndex,representative -> representative.parameters.mapIndexed{index, it ->
        PrismParameter(it.name, it.type.name, representative.name, clazzIndex, index)
        }
    }.flatten().fold(arrayListOf()){
        list: ArrayList<SharedPrismParameter>, parameter ->
            val foundParameter = list.find { it.name == parameter.name && it.type == parameter.type }
            if(foundParameter==null) {
                val newParameter = SharedPrismParameter(parameter.name, parameter.type)
                newParameter.occurrences.add(PrismParameterOccurrence(parameter.clazz, parameter.clazzIndex, parameter.parameterIndex))
                list.add(newParameter)
            } else {
                foundParameter.occurrences.add(PrismParameterOccurrence(parameter.clazz, parameter.clazzIndex, parameter.parameterIndex))
            }
        list
    }



/**********************************************************************************************************************
 *
 * Lenses of sub classes
 *
 **********************************************************************************************************************/

fun subClassLenses(sealedClass: SealedClass): String{

    return """
        |${buildSectionComment("Lens structures of the representatives")}
        |${sealedClass.representatives.joinToString("\n\n\n") { subClassLens(it as SubDataClass, sealedClass.name) }}
    """.trimMargin()
}
fun subClassLens(subDataClass: SubDataClass, parentClassName: String): String {
    val dataClass = DataClass()
    dataClass.name = "$parentClassName.${subDataClass.name}"
    dataClass.settersPostFix = "_${subDataClass.name}"
    dataClass.parameters.addAll(subDataClass.parameters)
    val generics = generics(dataClass)

    return """
        |${buildParaGraphComment("Lens structure of ${dataClass.name}")}
        |${setter(dataClass, generics)}
        |
        |${transaction(dataClass, dataClass.parameters.size, generics)}
        |
        |${parameterSetters(dataClass, generics)}
        |
        |${buildParaGraphComment("Suspended lens structure of ${dataClass.name}")}
        |${setterSuspended(dataClass, generics)}
        |
        |${transactionSuspended(dataClass, dataClass.parameters.size, generics)}
        |
        |${parameterSettersSuspended(dataClass, generics)}
        |
    """.trimMargin()
}
/**********************************************************************************************************************
 *
 * Auxiliary Types and Functions
 *
 **********************************************************************************************************************/

/**
 * Helper types
 * ====================================================================================================================
 */

data class PrismParameter(
        val name: String,
        val type: String,
        val clazz: String,
        val clazzIndex: Int,
        val parameterIndex: Int
)
data class PrismParameterOccurrence(
        val clazz: String,
        val clazzIndex: Int,
        val parameterIndex: Int
)
data class SharedPrismParameter(
        val name: String,
        val type: String,
        val occurrences: ArrayList<PrismParameterOccurrence> = arrayListOf()
)

/**
 * Helper functions
 * ====================================================================================================================
 */

fun suspended(suspended: Boolean, postFix: String = " ", whenFalse: String = ""): String = when(suspended){
    true -> "suspend$postFix"
    false -> whenFalse
}

/**
 * Compute the package name
 */
fun packageName(sealedClass: SealedClass) :String = when(sealedClass.packageName) {
    "" -> "org.drx.generated.prisms"
    else -> sealedClass.packageName
}

fun prismSetterFunctionGenerics(sealedClass: SealedClass): String {
    val set = listGenerics(sealedClass)
    sealedClass.representatives.forEach {
        set.addAll(listGenerics(it))
    }
    return if(set.isEmpty()) {
        ""
    } else {
        set.joinToString(", ","<","> ") { it }
    }
}


fun prismTransactionFunctionGenerics(sealedClass: SealedClass): String {
    val set = listGenerics(sealedClass)
    sealedClass.representatives.forEach {
        set.addAll(listGenerics(it))
    }
    return if(set.isEmpty()) {
        ""
    } else {
        set.joinToString(", ","<","> ") { it }
    }
}

fun prismParameterSetterFunctionGenerics(sealedClass: SealedClass): String {
    val set = listGenerics(sealedClass)
    sealedClass.representatives.forEach {
        set.addAll(listGenerics(it))
    }
    return if(set.isEmpty()) {
        ""
    } else {
        set.joinToString(", ","<","> ") { it }
    }
}



/**
 *
 */
fun file(sealedClass: SealedClass, project: Project): File = when(sealedClass.sourceFolder){
    "" -> File(project.projectDir, "src/generated/kotlin/${packageName(sealedClass).replace(".","/")}/${sealedClass.name}.kt".replace("//","/"))
    else -> File(project.projectDir, "${sealedClass.sourceFolder}/${packageName(sealedClass).replace(".","/")}/${sealedClass.name}.kt".replace("//","/"))
}

fun SealedClass.overriddenParameters(): Set<String> = representatives.map {
    it.overrideParameters
}.flatten().toSet()

fun SubSealedClass.overriddenParameters(): Set<String> = representatives.map {
    it.overrideParameters
}.flatten().toSet()


fun parameters(sealedClass: SealedClass, offset: String): List<String> = sealedClass.parameters.map{
    val overridden = sealedClass.overriddenParameters()
    if(overridden.contains(it.name)){
        "${offset}open val ${it.name}: ${it.type.name}"
    } else {
        "${offset}val ${it.name}: ${it.type.name}"
    }
}
fun parameters(sealedClass: SubSealedClass, offset: String): List<String> = sealedClass.parameters.map{
    val overridden = sealedClass.overriddenParameters()
    if(overridden.contains(it.name)){
        "${offset}open val ${it.name}: ${it.type.name}"
    } else {
        "${offset}val ${it.name}: ${it.type.name}"
    }
}



fun buildAuxiliaryPrismFunctions(): String = """${license()}
    |
    |package org.drx.generated.prisms
    |
    |${buildIdSetter()}
    |
    |${buildSuspendedIdSetter()}
    |
    |${buildExtendedFunctionComposer()}
    |
    |${buildFluentFunction()}
""".trimMargin()
