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