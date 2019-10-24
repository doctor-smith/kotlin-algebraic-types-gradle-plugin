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
package org.drx.plugin.algebraictypes.parse

import org.junit.Test

class SimpleParsingTest {

    @Test fun arithmetic() {
        // symbols (,), +,*, ^
        // R*(S+T) -> Product2<R,Sum2<S,T>>

        // S+T

        val plusParser = PlusParser()
        val plusResult = plusParser.parse(PlusParserData("R+S+T"))
        println(plusResult)

        val bandParser = BandParser()
        println(bandParser.parse(bandParser.parse(BandData(right= "123"))))


        val charParser =  CharParser("1".toCharArray()[0])
        println(charParser.parse(BandData(right= "213")))


        val symbolParser =  SymbolParser("121")
        println(symbolParser.parse(BandData(right= "aaaaaa11213")))
        println(symbolParser.parse(BandData(left = "3", current = "1", right= "21")))
    }

}