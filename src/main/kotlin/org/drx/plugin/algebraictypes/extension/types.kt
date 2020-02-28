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
package org.drx.plugin.algebraictypes.extension

/**
 * Base classes and interfaces
 * ====================================================================================================================
 */

interface Identifier {
    /**
     * Type name
     */
    val name: String
    /**
     * Package, the type belongs to
     */
    val packageName: String
    /**
     * Source folder, the type belongs to
     */
    val sourceFolder: String

}

interface Type : Identifier {
    /**
     * Dependencies of the type
     */
    val dependencies: ArrayList<Type>
    /**
     * The type is generic?
     */
    val isGeneric : IsGeneric
    /**
     * The type is serializable?
     */
    val isSerializable: IsSerializable

}

interface VaryingType : Type {
    val variance: TypeVariance
}

sealed class TypeVariance {
    object No : TypeVariance()
    object Out : TypeVariance()
    object In : TypeVariance()
}
sealed class IsGeneric {
    object No : IsGeneric()
    object AsSuch : IsGeneric()
    object WrapperType : IsGeneric()
    data class In(val types: HashSet<VaryingType> = hashSetOf()): IsGeneric() {
        constructor(type: VaryingType, vararg types: VaryingType) : this(hashSetOf(type,*types))
    }
}

sealed class IsSerializable {
    object No : IsSerializable()
    object WrapperType : IsSerializable()
    object Polymorphic : IsSerializable()
    object Transient : IsSerializable()
    data class AsSuch(
            val serializer: Identifier?,
            val serialModule: Identifier?
    ) : IsSerializable()

}

fun Identifier.fullName(): String = when(packageName) {
    "" -> name
    else -> "$packageName.$name"
}


@AlgebraicTypesDsl
@Suppress("FunctionName")
fun Type(
        name: String,
        packageName: String,
        sourceFolder: String,
        dependencies: ArrayList<Type>,
        isGeneric: IsGeneric,
        isSerializable: IsSerializable
): Type = object : Type {
    /**
     * Dependencies of the type
     */
    override val dependencies: ArrayList<Type>
        get() = dependencies
    /**
     * The type is generic?
     */
    override val isGeneric: IsGeneric
        get() = isGeneric
    /**
     * The type is serializable?
     */
    override val isSerializable: IsSerializable
        get() = isSerializable
    /**
     * Type name
     */
    override val name: String
        get() = name
    /**
     * Package, the type belongs to
     */
    override val packageName: String
        get() = packageName
    /**
     * Source folder, the type belongs to
     */
    override val sourceFolder: String
        get() = sourceFolder

}

@AlgebraicTypesDsl
fun Type.generic(): Type = when(this) {
    is VaryingType -> VaryingType(name, packageName, sourceFolder,variance, dependencies, IsGeneric.AsSuch, isSerializable)
    else -> Type(name, packageName, sourceFolder, dependencies, IsGeneric.AsSuch, isSerializable)
}
@AlgebraicTypesDsl
fun Type.genericIn(type: VaryingType): Type {
    if(isGeneric !is IsGeneric.In){
        return when(this) {
            is VaryingType -> VaryingType(name, packageName,sourceFolder, variance, dependencies, IsGeneric.In(type), isSerializable)
            else -> Type(name, packageName,sourceFolder, dependencies, IsGeneric.In(type), isSerializable)
        }
    }
    (isGeneric as IsGeneric.In).types.add(type)
    return this
}
@AlgebraicTypesDsl
fun Type.notGeneric(): Type = when(this) {
    is VaryingType -> VaryingType(name, packageName, sourceFolder,variance, dependencies, IsGeneric.No, isSerializable)
    else -> Type(name, packageName, sourceFolder, dependencies, IsGeneric.No, isSerializable)
}

@AlgebraicTypesDsl
fun Type.vary(variance: TypeVariance = TypeVariance.No): VaryingType = VaryingType(
        name, packageName,sourceFolder, variance, dependencies, isGeneric, isSerializable
)

@AlgebraicTypesDsl
@Suppress("FunctionName")
fun VaryingType(
        name: String,
        packageName: String,
        sourceFolder: String,
        variance: TypeVariance,
        dependencies: ArrayList<Type>,
        isGeneric: IsGeneric,
        isSerializable: IsSerializable
): VaryingType = object : VaryingType {

    override val variance: TypeVariance
        get() = variance
    /**
     * Dependencies of the type
     */
    override val dependencies: ArrayList<Type>
        get() = dependencies
    /**
     * The type is generic?
     */
    override val isGeneric: IsGeneric
        get() = isGeneric
    /**
     * The type is serializable?
     */
    override val isSerializable: IsSerializable
        get() = isSerializable
    /**
     * Type name
     */
    override val name: String
        get() = name
    /**
     * Package, the type belongs to
     */
    override val packageName: String
        get() = packageName
    /**
     * Source folder, the type belongs to
     */
    override val sourceFolder: String
        get() = sourceFolder

}

