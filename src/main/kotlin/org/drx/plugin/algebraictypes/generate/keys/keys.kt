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
package org.drx.plugin.algebraictypes.generate.keys

/**
 * Copyright (c) 2018-2019 Dr. Florian Schmidt
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

import org.drx.plugin.algebraictypes.basePath
import org.drx.plugin.algebraictypes.generate.file
import org.drx.plugin.algebraictypes.generate.keysPackage
import org.drx.plugin.algebraictypes.generate.license
import org.gradle.api.Project
import java.io.File

/**********************************************************************************************************************
 *
 * Keys
 *
 **********************************************************************************************************************/

fun generateKeys(prefix: String = "", number: Int = 0, project: Project, sourceFolder: String, domain: String, packageName: String) {

    require(number > -1)

    //val dir = File("${project.projectDir}$basePath/keys")
    val dir = project.file(sourceFolder,domain,packageName.keysPackage())
    if (!dir.exists()) {
        dir.mkdirs()
    }
    var keys = license()
    keys += "\n\npackage $domain.${packageName.keysPackage()}\n\n\n"

    var classRefs = ""
    var classes = ""

    var classNamePrefix = ""

    if(prefix != "") {
        classNamePrefix = prefix.substring(0,1).toUpperCase() + prefix.substring(1)
    }

    IntRange(0,number).forEach {
        classes += "\nclass ${classNamePrefix}Key$it"
        classRefs += "\n    map[$it] = ${classNamePrefix}Key$it::class"
    }

    keys += "import kotlin.reflect.KClass\n" +
            "\n" +
            "\n" +
            "val ${classNamePrefix}Keys: HashMap<Int, KClass<*>> by lazy{\n" +
            "    val map: HashMap<Int,KClass<*>> = HashMap()" +
            "\n" +
            classRefs +
            "\n" +
            "\n    map" +
            "\n}\n" +
            classes

    var filenamePrefix = ""
    if(prefix != "") {

        filenamePrefix = prefix.toLowerCase()+"-"
    }
    val keysFile = File(dir,"${filenamePrefix}keys.kt")
    keysFile.writeText(keys)
    println("Generating $number keys")
}


fun generateKeyGroup (prefix: String = "", number: Int = 0, project: Project) {
    require(number > -1)

    val dir = File("${project.projectDir}$basePath/keys")
    if (!dir.exists()) {
        dir.mkdirs()
    }
    var keys = license()
    keys += "\n\npackage org.drx.generated.keys\n\n\n"

    var keyClass = ""
    var keyObject = ""
    var getByInt = ""
    var getByKey = ""
    var classNamePrefix = ""

    if(prefix != "") {
        classNamePrefix = prefix.substring(0,1).toUpperCase() + prefix.substring(1)
    }

    IntRange(0,number).forEach {
        keyClass += "    object Key$it : ${classNamePrefix}Key()\n"
        getByInt += "        $it -> ${classNamePrefix}Key.Key$it::class\n"
        getByKey += "        ${classNamePrefix}Key.Key$it::class -> $it\n"
    }
    keyClass += "    object NoKey : ${classNamePrefix}Key()\n"
    getByInt += "        else -> ${classNamePrefix}Key.NoKey::class\n"
    getByKey += "        ${classNamePrefix}Key.NoKey::class -> -1 else -> -1\n"

    keyClass = "sealed class ${classNamePrefix}Key {\n$keyClass}"
    getByInt = "    operator fun get(index: Int): KClass<out ${classNamePrefix}Key> = when(index) {\n$getByInt    }\n"
    getByKey = "    operator fun get(key: KClass<out ${classNamePrefix}Key>): Int = when(key) {\n$getByKey    }\n"

    keyObject = "object ${classNamePrefix}Keys {\n$getByInt$getByKey}"


    keys += "import kotlin.reflect.KClass\n\n$keyClass\n\n$keyObject"

    var filenamePrefix = ""
    if(prefix != "") {

        filenamePrefix = prefix.toLowerCase()+"-"
    }
    val keysFile = File("${project.projectDir}$basePath/keys/${filenamePrefix}keys.kt")
    keysFile.writeText(keys)
    //println(keys)
    //println("Generating $number keys")
}

fun generateKeyGroup1(prefix: String = "", number: Int = 0, serialization: Boolean, project: Project, sourceFolder: String, domain: String, packageName: String) {
    require(number > -1)

    val dir = project.file(sourceFolder, domain, packageName.keysPackage())
    if (!dir.exists()) {
        dir.mkdirs()
    }
    var keys = license()
    keys += "\n\npackage $domain.${packageName.keysPackage()}\n\n\n"

    var keyClass = ""
    var keyObject = ""
    var getByInt = ""
    var getByKey = ""
    var classNamePrefix = ""
    var keyArray: String
    if(prefix != "") {
        classNamePrefix = prefix.substring(0,1).toUpperCase() + prefix.substring(1)
    }

    val serializerName = "${classNamePrefix.substring(0,1).toUpperCase()}${classNamePrefix.substring(1)}KeySerializer"
    val keyArrayName = "${classNamePrefix.substring(0,1).toLowerCase()}${classNamePrefix.substring(1)}KeyArray"
    IntRange(0,number).forEach {
        keyClass += "    object Key$it : ${classNamePrefix}Key()\n"
    }
    keyClass += "    object NoKey : ${classNamePrefix}Key()\n"
    getByInt += "        else -> ${classNamePrefix}Key.NoKey::class\n"

    keyArray = "val ${keyArrayName}: Array<KClass<out ${classNamePrefix}Key>> by lazy{ ${classNamePrefix}Key::class.sealedSubclasses.toTypedArray() }"

    keyClass = if(serialization){"@Serializable(with = ${serializerName}::class)\n"}else{""}+"sealed class ${classNamePrefix}Key {\n$keyClass}"
    getByInt = "\n    operator fun get(index: Int): KClass<out ${classNamePrefix}Key> = try {" +
            "\n        require(index < N)" +
            "\n        $keyArrayName[index]" +
            "\n    } catch(ignored : Exception) {" +
            "\n        ${classNamePrefix}Key.NoKey::class" +
            "\n    }" +
            "\n"
    getByKey = "\n    operator fun get(key: KClass<out ${classNamePrefix}Key>): Int = when(key){" +
            "\n        ${classNamePrefix}Key.NoKey::class -> -1" +
            "\n        else -> try {" +
            "\n            $keyArrayName.indexOf(key)" +
            "\n        } catch(ignored : Exception) {" +
            "\n            -1" +
            "\n        }" +
            "\n    }" +
            "\n"

    keyObject = "object ${classNamePrefix}Keys {" +
            "\n    private val N = $keyArrayName.size" +
            "\n$getByInt" +
            "$getByKey}"

    val instanceFunction = "fun KClass<out ${classNamePrefix}Key>.instance(): ${classNamePrefix}Key = objectInstance!!"

    val classFunction = "fun ${classNamePrefix}Key.kClass(): KClass<out ${classNamePrefix}Key> = this::class"

    val serializer = "class $serializerName : KSerializer<${classNamePrefix}Key> {\n" +
            "    override fun serialize(encoder: Encoder, obj: ${classNamePrefix}Key) {\n" +
            "        encoder.encodeString(obj::class.qualifiedName!!)\n" +
            "    }\n" +
            "\n" +
            "    override val descriptor: SerialDescriptor\n" +
            "        get() = StringDescriptor.withName(\"$serializerName\")\n" +
            "\n" +
            "    override fun deserialize(decoder: Decoder): ${classNamePrefix}Key {\n" +
            "        val name = decoder.decodeString()\n" +
            "        $keyArrayName.forEach {\n" +
            "            if(name == it.qualifiedName) {\n" +
            "                return it.objectInstance!!\n" +
            "            }\n" +
            "        }\n" +
            "        return ${classNamePrefix}Key.NoKey\n" +
            "    }\n" +
            "}\n"


    keys += "import kotlin.reflect.KClass \n" +
            if(serialization){"import kotlinx.serialization.*\n"}else{""} +
            if(serialization){"import kotlinx.serialization.internal.StringDescriptor\n"}else{""}+
            "\n" +
            "$keyClass\n\n" +
            "$keyArray\n\n" +
            "$keyObject\n\n" +
            "$instanceFunction\n\n" +
            "$classFunction\n" +
            if(serialization){"\n$serializer\n"}else{""}

    var filenamePrefix = ""
    if(prefix != "") {

        filenamePrefix = prefix.toLowerCase()+"-"
    }
    val keysFile = //File("${project.projectDir}$basePath/keys/${filenamePrefix}keys.kt")
        File(dir, "${filenamePrefix}keys.kt")
    keysFile.writeText(keys)
    //println(keys)
    //println("Generating $number keys")
}


