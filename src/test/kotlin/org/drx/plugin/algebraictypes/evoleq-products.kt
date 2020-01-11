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

import org.drx.plugin.algebraictypes.generate.evoleq.buildProductEvolveFunction
import org.drx.plugin.algebraictypes.generate.evoleq.buildProductEvolveFunctionWithSideEffect
import org.drx.plugin.algebraictypes.generate.evoleq.buildProductGetFunction
import org.drx.plugin.algebraictypes.generate.evoleq.generateEvoleqProduct
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

class EvoleqProductsTest {

    @Test fun test() {


        println(buildProductEvolveFunction(3))

        println(buildProductEvolveFunctionWithSideEffect(3))

        println(buildProductGetFunction(10))


        val project: Project = ProjectBuilder.builder().build()

        generateEvoleqProduct(3, project)


    }

}