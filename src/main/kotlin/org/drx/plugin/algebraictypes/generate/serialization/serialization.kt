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
package org.drx.plugin.algebraictypes.generate.serialization

import org.drx.plugin.algebraictypes.extension.DataClass
import org.drx.plugin.algebraictypes.extension.SealedClass
import org.drx.plugin.algebraictypes.config.Config.Defaults as Defaults
import org.drx.plugin.algebraictypes.util.repeatString


fun buildSerialModuleName(className: String): String {
    return className.substring(0,1).toLowerCase() + className.substring(1) + "SerialModule"
}

fun serialModule(dataClass: DataClass): String {
    val modules = hashSetOf<String>()

    // compute serial modules to be used
    dataClass.parameters.forEach {param ->
        if(param.type.serializable && !param.type.name.contains("<")) {
            modules.add(buildSerialModuleName(param.type.name))
        } else {
            param.type.dependencies.forEach { dependency ->
                if(dependency.serializable && !dependency.name.contains("<")) {
                    modules.add(buildSerialModuleName(dependency.name))
                }

            }
        }
    }

    return """
        |val ${buildSerialModuleName(dataClass.name)}: SerialModule by lazy {
        |${Defaults.offset}${if(modules.isNotEmpty()){modules.joinToString(" +\n${Defaults.offset}") { it }}else{"SerializersModule { }"}}  
        |} 
    """.trimMargin()
}

fun serialModule(sealedClass: SealedClass): String {

    val modules = hashSetOf<String>()

    // compute serial modules to be used
    sealedClass.parameters.forEach {param ->
        if(param.type.serializable) {
            modules.add(buildSerialModuleName(param.type.name))
        } else {
            param.type.dependencies.forEach { dependency ->
                if(dependency.serializable) {
                    modules.add(buildSerialModuleName(dependency.name))
                }

            }
        }
    }

    return """
        |val ${buildSerialModuleName(sealedClass.name)}: SerialModule by lazy {
        |${Defaults.offset}SerializersModule {
        |${Defaults.offset.repeatString(2)}polymorphic<${sealedClass.name}>{
        |${sealedClass.representatives.joinToString("\n") { Defaults.offset. repeatString(3) + sealedClass.name + "." + it.name + "::class with " + sealedClass.name + "." + it.name +".serializer()" }}               
        |${Defaults.offset.repeatString(2)}}
        |${Defaults.offset}${if(modules.isNotEmpty()){modules.joinToString(" +\n${Defaults.offset}", " +\n${Defaults.offset}") { it }}else{""}}}    
        |}
    """.trimMargin()
}