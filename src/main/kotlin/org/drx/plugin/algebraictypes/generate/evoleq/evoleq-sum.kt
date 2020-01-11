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
package org.drx.plugin.algebraictypes.generate.evoleq

import org.drx.plugin.algebraictypes.basePath
import org.drx.plugin.algebraictypes.generate.buildComment
import org.drx.plugin.algebraictypes.generate.dist
import org.drx.plugin.algebraictypes.generate.products.generateProductType
import org.drx.plugin.algebraictypes.generate.license
import org.gradle.api.Project
import java.io.File

fun generateEvoleqSum(dimension: Int, project: Project) {
    val dir = File("${project.projectDir}$basePath/evoleq/sums")
    if(!dir.exists()) {
        dir.mkdirs()
    }
    generateProductType(dimension, project)

    var evoleqProduct = license()

    evoleqProduct += "\n\npackage org.drx.generated.evoleq.sums\n\n\n"
    evoleqProduct += "import org.drx.evoleq.flow.Evolver\n"
    evoleqProduct += "import org.drx.evoleq.evolving.Evolving\n"
    if(dimension != 2) {
        evoleqProduct += "import org.drx.generated.products.Product2\n"
    }
    evoleqProduct += "import org.drx.generated.products.Product$dimension\n"
    evoleqProduct += "import org.drx.generated.sums.Sum$dimension\n\n"
    evoleqProduct += dist()
    evoleqProduct += buildSumEvolveFunction(dimension)
    evoleqProduct += dist()
    evoleqProduct += buildSumGetFunction(dimension)


    val evoleqProductFile = File("${project.projectDir}$basePath/evoleq/sums/sum-$dimension.kt")
    evoleqProductFile.writeText(evoleqProduct)
    println("Generating evoleq sum types of dimension $dimension")
}

fun buildSumEvolveFunction(dimension: Int): String {

    val progression = IntRange(1, dimension).reversed()

    val types = progression.joinToString(", ") { "T$it" }
    val evolverTypes = progression.joinToString(", ", "", "") { "Evolver<T$it>" }
    val evolvingTypes = progression.joinToString(", ") { "Evolving<T$it>" }
    val injections = progression.joinToString(", ") { "(D) -> T$it" }
    val cases1 = progression.joinToString("") { "\n    is Sum$dimension.Summand$it -> Sum$dimension.Summand$it( factor$it.evolve( sum.value ) )" }
    val cases2 = progression.joinToString("") { "\n    is Sum$dimension.Summand$it -> Sum$dimension.Summand$it( value.evolve( product.factor$it ) )" }
    val cases3 = progression.joinToString("") { "\n    is Sum$dimension.Summand$it -> value.evolve( data )" }
    val cases4 = progression.joinToString("") { "\n    is Sum$dimension.Summand$it -> Sum$dimension.Summand$it( (factor2 as Sum$dimension.Summand$it<$evolverTypes>).value.evolve( factor1.factor$it( data ) ) )" }
    val comment1 = buildComment(
            "Evolve a sum type"
    )
    val comment2 = buildComment(
            "Evolve with respect to sum-type-flow"
    )
    val comment3 = buildComment(
            "Evolve with respect to sum-type-flow"
    )
    val comment4 = buildComment(
            "Evolve with respect to sum-type-flow with dynamic data"
    )
    val comment5 = buildComment(
            "Setup dynamic Sum$dimension evolver"
    )
    return "${comment1}suspend fun <$types> Product$dimension<$evolverTypes>.evolve(sum: Sum$dimension<$types>) : Sum$dimension<$evolvingTypes> = when( sum ) { $cases1 \n}" +
            dist() +
            "${comment2}suspend fun <$types> Sum$dimension<$evolverTypes>.evolve(product: Product$dimension<$types>) : Sum$dimension<$evolvingTypes> = when( this ) { $cases2 \n}" +
            dist() +
            "${comment3}suspend fun <D> Sum$dimension<${progression.joinToString(", ") { "Evolver<D>" }}>.evolve(data: D): Evolving<D> = when( this ) {$cases3\n}" +
            dist() +
            "${comment4}suspend fun <D, $types> Product2<Sum$dimension<$evolverTypes>, Product$dimension<$injections>>.evolve( data : D): Sum$dimension<$evolvingTypes> = when( factor2 ) {$cases4\n}" +
            dist() +
            "${comment5}@Suppress(\"FunctionName\")\nfun <D, $types> Sum${dimension}Evolver(evolver: Sum$dimension<$evolverTypes>, data: ()->Product$dimension<$injections>): Product2<Sum$dimension<$evolverTypes>, Product$dimension<$injections>> = Product2(evolver, data())"

}
fun buildSumGetFunction(dimension: Int): String {
    val progression = IntRange(1, dimension).reversed()

    val types = progression.joinToString(", ") { "T$it" }
    val evolvingTypes = progression.joinToString(", ") { "Evolving<T$it>" }
    val cases = progression.joinToString("") { "\n    is Sum$dimension.Summand$it -> Sum$dimension.Summand$it( value.get() )" }


    return "suspend fun <$types> Sum$dimension<$evolvingTypes>.get() : Sum$dimension<$types> = when( this ) { $cases \n}"
}