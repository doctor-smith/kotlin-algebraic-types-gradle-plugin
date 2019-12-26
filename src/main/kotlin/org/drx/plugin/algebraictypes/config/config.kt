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

        const val productsFolder = "$baseFolder/products"
        //const val productFilenamePrefix = "product-"
        const val sumsFolder = "$baseFolder/sums"
        const val keysFolder = "$baseFolder/keys"
        const val dualitiesFolder = "$baseFolder/duality"
    }
    object Tasks {
        const val generateAlgebraicTypes = "generateAlgebraicTypes"
    }
}