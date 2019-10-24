package org.drx.plugin.algebraictypes

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import java.io.File
import kotlin.math.max

open class GenerateEvoleqSum: DefaultTask() {
    @Suppress("UnstableApiUsage")
    @set:Option(option = "dimension", description = "The dimension")
    @get:Input
    var dimension: String = "2"

    @TaskAction
    fun generate() {
        //println(license())
        generateEvoleqSum(Integer.parseInt(dimension), project)

    }


}


open class GenerateEvoleqSums: DefaultTask() {
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
            generateEvoleqSum(it, project)
        }
    }
}

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
    evoleqProduct += "import org.drx.generated.products.Product$dimension\n"
    evoleqProduct += "import org.drx.generated.sums.Sum$dimension\n"

    evoleqProduct += buildSumEvolveFunction(dimension)
    evoleqProduct += dist()
    evoleqProduct += buildSumGetFunction(dimension)


    val evoleqProductFile = File("${project.projectDir}$basePath/evoleq/sums/sum-$dimension.kt")
    evoleqProductFile.writeText(evoleqProduct)
    println("Generating evoleq sum types of dimension $dimension")
}

fun buildSumEvolveFunction(dimension: Int): String {
    val types = IntRange(1, dimension).reversed().joinToString(", ") { "T$it" }
    val evolverTypes = IntRange(1, dimension).reversed().joinToString(", ", "", "") { "Evolver<T$it>" }
    val evolvingTypes = IntRange(1, dimension).reversed().joinToString(", ") { "Evolving<T$it>" }
    //val factors = IntRange(1, dimension).reversed().joinToString(", ") { "\n    factor$it.evolve( product.factor$it )" }
    val cases1 = IntRange(1,dimension).reversed().joinToString("") { "\n    is Sum$dimension.Summand$it -> Sum$dimension.Summand$it( factor$it.evolve( sum.value ) )" }
    val cases2 = IntRange(1,dimension).reversed().joinToString("") { "\n    is Sum$dimension.Summand$it -> Sum$dimension.Summand$it( value.evolve( product.factor$it ) )" }

    return "suspend fun <$types> Product$dimension<$evolverTypes>.evolve(sum: Sum$dimension<$types>) : Sum$dimension<$evolvingTypes> = when( sum ) { $cases1 \n}" +
            dist() +
            "suspend fun <$types> Sum$dimension<$evolverTypes>.evolve(product: Product$dimension<$types>) : Sum$dimension<$evolvingTypes> = when( this ) { $cases2 \n}"
}
fun buildSumGetFunction(dimension: Int): String {
    val types = IntRange(1, dimension).reversed().joinToString(", ") { "T$it" }
    val evolvingTypes = IntRange(1, dimension).reversed().joinToString(", ") { "Evolving<T$it>" }
    val cases = IntRange(1, dimension).reversed().joinToString("") { "\n    is Sum$dimension.Summand$it -> Sum$dimension.Summand$it( value.get() )" }


    return "suspend fun <$types> Sum$dimension<$evolvingTypes>.get() : Sum$dimension<$types> = when( this ) { $cases \n}"
}