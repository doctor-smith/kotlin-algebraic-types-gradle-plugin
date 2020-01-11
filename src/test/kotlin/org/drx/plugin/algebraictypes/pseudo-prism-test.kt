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
package org.drx.plugin.algebraictypes

import org.drx.plugin.algebraictypes.extension.SealedClass
import org.drx.plugin.algebraictypes.extension.dataRepresentative
import org.drx.plugin.algebraictypes.extension.parameter
import org.drx.plugin.algebraictypes.extension.type
import org.drx.plugin.algebraictypes.generate.optics.buildSealedClassFileContent
import org.drx.plugin.algebraictypes.util.addAll
import org.junit.Test

class PseudoPrismTest {

    @Test fun display() {
        val sealedClass = SealedClass()
        sealedClass.name = "TestClass"
        sealedClass.packageName = "org.lib.data"
        sealedClass.sourceFolder = "module/src/main/kotlin"
        sealedClass.comment.addAll(
                "This is a great",
                "comment for testing"
        )
        sealedClass.parameter {
            name = "param1"
            type {
                name = "T1"
                packageName = "org.lib.T1"
            }
        }
        sealedClass.parameter {
            name = "param2"
            type {
                name = "T2"
                packageName = "org.lib.T2"
            }
        }
        sealedClass.parameter {
            name = "param3"
            type {
                name = "T3"
                packageName = "org.lib.T3"
            }
        }
        sealedClass.dataRepresentative {
            name = "Sub1"

            overrideParameters.addAll(
                    "param1",
                    "param2"
            )
            defaultValuesSet["param3"] = "T3()"

            comment.addAll(
                    "Great comment on",
                    "a representant of",
                    "a sealed class"
            )

            parameter {
                name = "param1"
                type {
                    name = "T1"
                }

            }
            parameter {
                name = "param2"
                type {
                    name = "T2"
                }
                defaultValue = "T2()"
            }
        }
        sealedClass.dataRepresentative {
            name = "Sub2"

            overrideParameters.addAll(
                    "param1",
                    "param2",
                    "param3"
            )
            comment.addAll(
                    "Great comment on",
                    "a representant of",
                    "a sealed class"
            )

            parameter {
                name = "param1"
                type {
                    name = "T1"
                }

            }
            parameter {
                name = "param2"
                type {
                    name = "T2"
                }
                //defaultValue = "T2()"
            }
            parameter {
                name = "param3"
                type {
                    name = "T3"
                }
            }
            parameter {
                name = "param4"
                type {
                    name = "T4"
                    packageName = "org.lib.T4"
                }
            }
        }
        println(buildSealedClassFileContent(sealedClass))
    }

    @Test fun displayUsableExample() {
        val sealedClass = SealedClass()
        sealedClass.name = "SealedClass"
        sealedClass.comment.add("jfdklöa fjdkalö jfdkla")
        sealedClass.dataRepresentative {
            name = "Sub1"
            parameter {
                name = "x"
                type{
                    name = "Int"
                }
            }
            parameter {
                name = "y"
                type{
                    name = "String"
                }
            }
        }
        sealedClass.dataRepresentative {
            name = "Sub2"
            comment.add("hahaha ahaha")
            parameter {
                name = "x"
                type{
                    name = "Int"
                }
            }

            parameter {
                name = "z"
                type{
                    name = "Boolean"
                }
            }
        }
/*
        println(prismParameterSetterType(sealedClass ))
        println(prismParameterSetterType(sealedClass, true))

        println(prismSetterType(sealedClass ))
        println(prismSetterType(sealedClass, true))

        println(prismParameterSetterType(sealedClass ))
        println(prismParameterSetterType(sealedClass, true))


        println(prismTransactionArgumentType(sealedClass ))
        println(prismTransactionArgumentType(sealedClass , true))

        println(collectSharedPrismParameters(sealedClass))
        println()
        println(prismSetter(sealedClass))
        println()

        println(prismSetter(sealedClass,true))
        println()
*/
        println(buildSealedClassFileContent(sealedClass))




    }

}