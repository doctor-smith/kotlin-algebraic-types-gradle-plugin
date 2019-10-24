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


// One can add (SumX)->T + (SumY)->T = (Sum(X+Y))->T bby first opposing the summands one by one,
// then taking their product and oppose it after
//
// So sum arithmetic uses duality and product arithmetic
//
fun buildSumOperator(dimension: Int, first: Int) : String {

    val second = dimension - first
    require(first >= 1 && second >= 1)

    var result = ""
    var factorsList = arrayListOf<String>()
    if(second > 1 && first > 1) {
        result = "operator fun <${buildGenericTypes(dimension, "S")}, T> "
        result += "((Sum$second<${buildGenericTypes(dimension, "S", first +1)}>)->T).plus(other: (Sum$first<${buildGenericTypes(first, "S")}>)->T) : (Sum$dimension<${buildGenericTypes(dimension,"S")}>)->T = "
        result += "(this.oppose() * other.oppose()).oppose()"
    }

    if(second == 1 && first >1) {
        result = "operator fun <${buildGenericTypes(dimension, "S")}, T> "
        result += "((S$dimension)->T).plus(other: (Sum$first<${buildGenericTypes(first, "S")}>)->T) : (Sum$dimension<${buildGenericTypes(dimension,"S")}>)->T = "
        result += "(this * other.oppose()).oppose()"
    }

    return result
}