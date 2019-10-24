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

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import java.io.File
import kotlin.math.max

open class GenerateEvoleqProduct: DefaultTask() {
    @Suppress("UnstableApiUsage")
    @set:Option(option = "dimension", description = "The dimension")
    @get:Input
    var dimension: String = "2"

    @TaskAction
    fun generate() {
        //println(license())
        generateEvoleqProduct(Integer.parseInt(dimension), project)

    }


}


open class GenerateEvoleqProducts: DefaultTask() {
    @Suppress("UnstableApiUsage")
    @set:Option(option = "from", description = "The lower bound of the range of the types to be generated")
    @get:Input
    var from: String = "2"

    @set:Option(option = "to", description = "The upper bound of the range of the types to be generated")
    @get:Input
    var to: String = "2"

    @TaskAction
    fun generate() {
        val to = Integer.parseInt(to)
        val from = Integer.parseInt(from)
        //require(to > 9)
        IntRange(max(2, from),to).forEach {
            generateEvoleqProduct(it, project)
        }
    }
}

fun generateEvoleqProduct(dimension: Int, project: Project) {
    val dir = File("${project.projectDir}$basePath/evoleq/products")
    if(!dir.exists()) {
        dir.mkdirs()
    }
    generateProductType(dimension, project)

    var evoleqProduct = license()

    evoleqProduct += "\n\npackage org.drx.generated.evoleq.products\n\n\n"
    evoleqProduct += "import org.drx.evoleq.flow.Evolver\n"
    evoleqProduct += "import org.drx.evoleq.evolving.Evolving\n"
    evoleqProduct += "import org.drx.generated.products.Product$dimension\n"

    evoleqProduct += buildProductEvolveFunction(dimension)
    evoleqProduct += dist()
    evoleqProduct += buildProductGetFunction(dimension)


    val evoleqProductFile = File("${project.projectDir}$basePath/evoleq/products/product-$dimension.kt")
    evoleqProductFile.writeText(evoleqProduct)
    println("Generating evoleq product types of dimension $dimension")
}

fun buildProductEvolveFunction(dimension: Int): String {
    val types = IntRange(1, dimension).reversed().joinToString(", ") { "T$it" }
    val evolverTypes = IntRange(1, dimension).reversed().joinToString(", ", "", "") { "Evolver<T$it>" }
    val evolvingTypes = IntRange(1, dimension).reversed().joinToString(", ") { "Evolving<T$it>" }
    val factors = IntRange(1, dimension).reversed().joinToString(", ") { "\n    factor$it.evolve( product.factor$it )" }

    return "suspend fun <$types> Product$dimension<$evolverTypes>.evolve(product: Product$dimension<$types>) : Product$dimension<$evolvingTypes> = Product$dimension($factors\n)"
}
fun buildProductGetFunction(dimension: Int): String {
    val types = IntRange(1, dimension).reversed().joinToString(", ") { "T$it" }
    val evolvingTypes = IntRange(1, dimension).reversed().joinToString(", ") { "Evolving<T$it>" }
    val factors = IntRange(1, dimension).reversed().joinToString(", ") { "\n    factor$it.get()" }


    return "suspend fun <$types> Product$dimension<$evolvingTypes>.get() : Product$dimension<$types> = Product$dimension($factors\n)"
}
