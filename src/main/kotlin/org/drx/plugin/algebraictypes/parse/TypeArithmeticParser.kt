package org.drx.plugin.algebraictypes.parse

import kotlin.reflect.KClass

class TypeArithmeticParser

data class PlusParserData(
        val source : String,
        val result : String = "",
        val remainder : String = "",
        val message : String = ""
)

class PlusParser : Parser<PlusParserData> {
    override val id: KClass<*>
        get() = PlusParser::class
    override val parsers: HashMap<KClass<*>, Parser<*>>
        get() = HashMap()
    override val parse: (PlusParserData) -> PlusParserData
        get() = { data ->
            val split = data.source.split("+")
            val dim = split.size

            if(dim-1 > 0) {
                PlusParserData("", "Sum$dim<${split.joinToString(",")}>", message= "DONE")
            } else {
                data
            }
        }
}