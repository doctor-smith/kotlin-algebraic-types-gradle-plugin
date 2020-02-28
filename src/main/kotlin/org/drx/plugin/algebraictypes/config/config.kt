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
package org.drx.plugin.algebraictypes.config

object Config {
    object GeneratedSources {
        const val baseFolder = "/src/generated/kotlin/org/drx/generated"
        const val basePackage = "org.drx.generated"

        object AlgebraicTypes {
            const val packageName = "algebraic"
        }
        
        object Base {
            const val folderName = "/src/generated/kotlin/org/drx/generated"
            const val packageName = "org.drx.generated"
        }

        object Products {
            const val folderName = "$baseFolder/products"
            const val fileNamePrefix = "product"
            const val packageName = "$basePackage.products"
        }

        object Sums {
            const val folderName = "$baseFolder/sums"
            const val fileNamePrefix = "sum"
            const val packageName = "$basePackage.sums"
        }


        object DataClasses {
            const val folderName = "$baseFolder/lenses"
        }

        object SealedClasses {
            const val folderName = "$baseFolder/prisms"
        }
        const val productsFolder = "$baseFolder/products"
        const val productFileNamePrefix = "product"


        const val sumsFolder = "$baseFolder/sums"
        const val sumFileNamePrefix = "sum"

        const val keysFolder = "$baseFolder/keys"
        const val dualitiesFolder = "$baseFolder/duality"
        const val dualityFileNamePrefix = "duality"
    }

    object Tasks {
        const val generateAlgebraicTypes = "generateAlgebraicTypes"
    }

    object Serialization {
        val imports = arrayListOf(
            "kotlinx.serialization.*",
            "kotlinx.serialization.internal.StringDescriptor",
            "kotlinx.serialization.modules.SerialModule",
            "kotlinx.serialization.modules.SerializersModule",
            "kotlinx.serialization.modules.plus"
        )
    }

    object Defaults {
        const val offset = "    "
        const val starLine = "**********************************************************************************************************************"
        const val equalSignLine = "===================================================================================================================="
    }
}