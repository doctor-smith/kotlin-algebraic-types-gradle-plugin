package org.drx.plugin.algebraictypes

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

class EvoleqProductsTest {

    @Test fun test() {


        println(buildProductEvolveFunction(3))

        println(buildProductGetFunction(10))


        val project: Project = ProjectBuilder.builder().build()

        generateEvoleqProduct(3,project)


    }

}