package org.drx.plugin.algebraictypes.process

tailrec fun<D> process(
       testObject: Boolean,
       condition: (D)->Boolean,
       data: D,
       step: (D)->D
) : D = when(testObject) {
    false -> data
    true -> {
        val newData = step(data)
        process(
            condition(newData),
            condition,
            newData
        ){
            d -> step( d )
        }
    }
}