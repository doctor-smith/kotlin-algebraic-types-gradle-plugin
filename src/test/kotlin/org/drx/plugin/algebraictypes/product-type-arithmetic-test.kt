package org.drx.plugin.algebraictypes

import org.junit.Test

class ProductTypeArithmeticTest {

    @Test fun buildProductOperatorTest() {
        val product = buildProductOperator(7, 6)
        println(product)
    }

}