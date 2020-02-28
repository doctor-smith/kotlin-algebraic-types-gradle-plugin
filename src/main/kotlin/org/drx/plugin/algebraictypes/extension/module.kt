package org.drx.plugin.algebraictypes.extension

open class ModuleExtension {
    lateinit var sourceFolder: String
    lateinit var domain: String
    var productTypes: DimensionSelection? = null
    var sumTypes: DimensionSelection? = null
    var dualities: DimensionSelection? = null
    var productTypeArithmetics: DimensionSelection? = null
    var evoleqSums: DimensionSelection? = null
    var evoleqProducts: DimensionSelection? = null
    var outputs: Outputs = Outputs()
    val keys: ArrayList<Keys> = arrayListOf()
    var dataClasses: DataClasses? = null
}

@AlgebraicTypesDsl
fun AlgebraicTypesExtension.sourceSet(configuration: ModuleExtension.()->Unit) {
    val module = ModuleExtension()
    module.configuration()
    with(module) moduleExtension@{
        // algebraic types
        this@sourceSet.productTypes = this@moduleExtension.merge(this@sourceSet.productTypes, productTypes)
        this@sourceSet.sumTypes = this@moduleExtension.merge(this@sourceSet.sumTypes, sumTypes)
        this@sourceSet.dualities = this@moduleExtension.merge(this@sourceSet.dualities, dualities)
        this@sourceSet.productTypeArithmetics = this@moduleExtension.merge(this@sourceSet.productTypeArithmetics, productTypeArithmetics)
        this@sourceSet.evoleqSums = this@moduleExtension.merge(this@sourceSet.evoleqSums, evoleqSums)
        this@sourceSet.evoleqProducts = this@moduleExtension.merge(this@sourceSet.evoleqProducts, evoleqProducts)
        if(keys != null) {
            this@sourceSet.keys.addAll(this@moduleExtension.keys.modularize(this@moduleExtension))
        }
        // data classes
        this@sourceSet.dataClasses = this@moduleExtension.merge(this@sourceSet.dataClasses, dataClasses)
    }
}

fun ModuleExtension.merge(base: DimensionSelection?, integrand: DimensionSelection?): DimensionSelection.Complex? =
    if(integrand != null){
        with(integrand.complexify().modularize(this@merge)) integrand@{
            if(base == null) {
                this@integrand
            }
            else {
                (base as DimensionSelection.Complex).list.addAll(this.list)
               base
            }
        }
    } else {
        base as DimensionSelection.Complex?
    }

fun ModuleExtension.merge(base: DataClasses?, integrand: DataClasses?): DataClasses? =
    if(integrand != null){
        with(integrand.modularize(this@merge)) integrand@{
            if(base == null) {
                this@integrand
            } else {
                base.dataClasses.addAll(integrand.dataClasses)
                base.sealedClasses.addAll(integrand.sealedClasses)
                // todo add others
                base
            }
        }
    } else {
        base
    }

/**
 * Returns a complex dimension-selection containing only single selections
 */
fun DimensionSelection.complexify(): DimensionSelection.Complex = when(this) {
    is DimensionSelection.Complex -> with(DimensionSelection.Complex(arrayListOf(),sourceFolder, packageName)){
        this@complexify.list.forEach { selection -> this.list.addAll( selection.complexify().list ) }
        this
    }
    is DimensionSelection.List -> DimensionSelection.Complex(
        arrayListOf(*list.map{ x -> DimensionSelection.Single(x,sourceFolder,packageName)}.toTypedArray()),
        sourceFolder,
        domain,
        packageName
    )
    is DimensionSelection.Range -> DimensionSelection.Complex(
        arrayListOf(*(from..to).map { DimensionSelection.Single(it,sourceFolder, packageName) }.toTypedArray()),
        sourceFolder,
        domain,
        packageName
    )
    is DimensionSelection.Single -> DimensionSelection.Complex(arrayListOf(this),
        sourceFolder,
        domain,
        packageName
    )
}

/**
 * Assumes that selectionlist only contains Single selections
 */
fun DimensionSelection.Complex.modularize(moduleExtension: ModuleExtension): DimensionSelection.Complex = with(DimensionSelection.Complex(
    arrayListOf(),
    moduleExtension.sourceFolder,
    moduleExtension.domain,
    packageName
    )) {
        list.addAll(arrayListOf(*this@modularize.list.map{
            selection -> DimensionSelection.Single(
                (selection as DimensionSelection.Single).dimension,
                sourceFolder,
                moduleExtension.domain,
                selection.packageName
            )
        }.toTypedArray()))
        this
    }

fun DataClasses.modularize(moduleExtension: ModuleExtension) : DataClasses = with(DataClasses()) {
    this@modularize.dataClasses.forEach { dataClass ->
        this.dataClass {
            name = dataClass.name
            comment.addAll(dataClass.comment)
            sourceFolder = moduleExtension.sourceFolder
            domain = moduleExtension.domain
            packageName = dataClass.packageName
            parameters.addAll(dataClass.parameters)
            settersPostFix = dataClass.settersPostFix
            serializable = dataClass.serializable
            serializationType = dataClass.serializationType
        }
    }
    this@modularize.sealedClasses.forEach { sealedClass ->
        this.sealedClass {
            name = sealedClass.name
            comment.addAll(sealedClass.comment)
            sourceFolder = moduleExtension.sourceFolder
            domain = moduleExtension.domain
            packageName = sealedClass.packageName
            parameters.addAll(sealedClass.parameters)
            representatives.addAll(sealedClass.representatives)
            settersPostFix = sealedClass.settersPostFix
            serializable = sealedClass.serializable
            serializationType = sealedClass.serializationType
        }
    }
    this
}

fun ArrayList<Keys>.modularize(moduleExtension: ModuleExtension): ArrayList<Keys> = with(KeysExtension()) {
    this@modularize.forEach { key -> keyGroup {
        name = key.name
        number = key.number
        serialization = key.serialization
        sourceFolder = moduleExtension.sourceFolder
        domain = moduleExtension.domain
        packageName = key.packageName
    } }
    keys
}

/**
 * Products dsl
 */
@AlgebraicTypesDsl
fun ModuleExtension.products(configuration: DimensionSelectionExtension.()->Unit) {
    val extension = DimensionSelectionExtension()
    extension.configuration()
    productTypes = extension.dimensionSelection
}
/**
 * Sums dsl
 */
@AlgebraicTypesDsl
fun ModuleExtension.sums(configuration: DimensionSelectionExtension.()->Unit) {
    val extension = DimensionSelectionExtension()
    extension.configuration()
    sumTypes = extension.dimensionSelection
}

/**
 * Product-Arithmetics dsl
 */
@AlgebraicTypesDsl
fun ModuleExtension.productArithmetics(configuration: DimensionSelectionExtension.()->Unit) {
    val extension = DimensionSelectionExtension()
    extension.configuration()
    productTypeArithmetics = extension.dimensionSelection
}

/**
 * Evoleq Products dsl
 */
@AlgebraicTypesDsl
fun ModuleExtension.evoleqProducts(configuration: DimensionSelectionExtension.()->Unit) {
    val extension = DimensionSelectionExtension()
    extension.configuration()
    evoleqProducts = extension.dimensionSelection
}

/**
 * Evoleq Sums dsl
 */
@AlgebraicTypesDsl
fun ModuleExtension.evoleqSums(configuration: DimensionSelectionExtension.()->Unit) {
    val extension = DimensionSelectionExtension()
    extension.configuration()
    evoleqSums = extension.dimensionSelection
}

/**
 * Dualities dsl
 */
@AlgebraicTypesDsl
fun ModuleExtension.dualities(configuration: DimensionSelectionExtension.()->Unit) {
    val extension = DimensionSelectionExtension()
    extension.configuration()
    dualities = extension.dimensionSelection
}

/**
 * KeyGroups dsl
 */

@AlgebraicTypesDsl
fun ModuleExtension.keyGroups(configuration: KeysExtension.()->Unit) {
    val extension = KeysExtension()
    extension.configuration()
    keys.addAll(extension.keys)
}

@AlgebraicTypesDsl
fun ModuleExtension.dataClasses(configuration: DataClasses.()->Unit) {
    val dataClasses = DataClasses()
    dataClasses.configuration()
    this.dataClasses = dataClasses
}




