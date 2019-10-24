package org.drx.plugin.algebraictypes.parse

import kotlin.reflect.KClass

interface Parser<D> {
    val id: KClass<*>
    val parse: (D)->D
    val parsers: HashMap<KClass<*>,Parser<*>>
}
data class BandData( val left: String = "", val current: String = "", val right: String)
open class BandParser() : Parser<BandData> {
    override val id: KClass<*>
        get() = BandParser::class
    override val parsers: HashMap<KClass<*>, Parser<*>>
        get() = HashMap()
    override val parse: (BandData) -> BandData
        get() = {data ->
            when(data.right) {
                "" -> data
                else -> BandData(

                        data.left + data.current,
                        data.right.substring(0,1),
                        data.right.substring(1)
                )
            }

        }
}

class CharParser(val char: Char) : BandParser() {
    override val id: KClass<*>
        get() = CharParser::class
    override val parse: (BandData) -> BandData
        get() = {
            when(it.current){
                "$char" -> it
                else -> this.parse(super.parse(it))
            }
        }
}

open class SymbolParser(open val symbol: String): Parser<BandData> {
    private val map: HashMap<KClass<*>, Parser<*>> = HashMap()
    private val symbolLength: Int
    init{
        map[BandParser::class] = BandParser()
        symbolLength = symbol.length
    }

    override val id: KClass<*>
        get() = SymbolParser::class
    override val parsers: HashMap<KClass<*>, Parser<*>>
        get() = map
    override val parse: (BandData) -> BandData
        get() = { data ->
            when (data.current.length < symbolLength) {
                true -> when(data.right.length - (symbolLength-data.current.length) >= 0){
                    true -> parse(BandData(
                            data.left,
                            data.current + data.right.substring(0,1),
                            data.right.substring(1)
                    ))
                    false -> BandData(data.left+data.current + data.right,"","")
                }
                false -> when(data.current){
                    symbol -> data
                    else -> parse(BandData(
                            data.left + data.current.substring(0,1),
                            data.current.substring(1) + data.right.substring(0,1),
                            data.right.substring(1)
                    ))
                }
            }
        }
}
/*
class StartsWithParser(val symbol: String) : Parser<BandData> {
    override val id: KClass<*>
        get() = StartsWithParser::class
    override val parsers: HashMap<KClass<*>, Parser<*>>
        get() = HashMap()
    override val parse: (BandData) -> BandData
        get() = {data -> when(){

        }

            data
        }
}
*/
data class Encodings(val remainder: String,val items: ArrayList<Pair<String,String>> = arrayListOf())
class TextEncoder : Parser<Encodings> {
    override val id: KClass<*>
        get() = TextEncoder::class
    override val parsers: HashMap<KClass<*>, Parser<*>>
        get() = HashMap()
    override val parse: (Encodings) -> Encodings
        get() = {data ->

            data
        }
}

class ExtractionParser(val leftSymbol: String, val rightSymbol: String) : Parser<BandData> {
    override val id: KClass<*>
        get() = ExtractionParser::class
    override val parsers: HashMap<KClass<*>, Parser<*>>
        get() = HashMap()
    override val parse: (BandData) -> BandData
        get() = {data ->

            data
        }
}