[![Download](https://img.shields.io/badge/Gradle%20Plugin%20Portal-1.0.16-blue.svg)](https://plugins.gradle.org/plugin/org.drx.kotlin-algebraic-types-plugin)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

# Algebraic Types Gradle Plugin
Generate product- and sum-types of arbitrary finite 'dimension' together with related functorial maps, generate composable non-generic / pseudo lenses and prisms.

## Add the plugin to your project
Add the plugin id to the plugin-block of your build.gradle.kts file
```kotlin
plugins{
    id("org.drx.kotlin-algebraic-types-plugin") version "1.0.16"
}

```
## Usage
Add the following to your build.gradle.kts file to configure the plugin
```kotlin
algebraicTypes{

    // Generate sym-types ranging from 5 to 8 and in dimension 42 
    sums { 
        range(5, 8)
        dimension(42)
    }

    // Generate product-types in dimensions 3,6,9 and 7
    products {
        list(3,6,9)
        dimension(7)
    }

    // Generate product-arithmetic in dimension 5
    productArithmetics {
        dimension(5)
    }

    // Generate dualities
    dualitites {
        /* ... */
    }

    // Generate evoleq-related sum types and functions
    // Note: Requires Evoleq
    evoleqSums {
        /* ... */
    }  
  
    // Generate evoleq-related product types and functions
    // Note: Requires Evoleq
    evoleqProducts {
        /* ... */
    }  

    // Generate kotlin-serializable classes representing keys
    keyGroups {

        keyGroup {
            name = "Id"
            number = 10_000 
            serialization = false // 'true' requires the kotlin-serialization library
        }   
    
        keyGroup {
            /* ... */
        }
    }
    // generate data class together with a pseudo-lens-structure 
    dataClasses {
        dataClass{
            name = "Foo"
            sourceFolder = "module/src/main/kotlin"
            packageName = "my.pack.age"
            parameter{
                name = "bar"
                type {
                    name = "MyCustomType"
                    import = "x.y.z"
                }           
            }  
            parameter{
                name = "generic"
                type{
                    name = "T"
                    isGeneric = true
                }
            }                
        }
    }
}
```
### Notes
  + The extensions 'dimension', 'range' and 'list' can be used in
      + sums
      + products
      + dualities
      + productArithmetics
      + evoleqSums
      + evoleqProducts    
  + Usage of 'evoleqSums' and 'evoleqProducts' only makes sense if you are also using [Evoleq](https://github.com/doctor-smith/evoleq) 
  + Using the serialization option requires the [kotlin-serialization](https://github.com/Kotlin/kotlinx.serialization) library
### Data classes extension
Consider the 
#### Example
```kotlin
algebraicTypes{
    /* ... */
    dataClasses {
        dataClass{
            name = "Foo"
            sourceFolder = "module/src/main/kotlin"
            packageName = "my.pack.age"
            parameter{
                name = "bar"
                type {
                    name = "MyCustomType"
                    import = "x.y.z.MyCustomType"
                }           
            }  
            parameter{
                name = "genericList"
                defaultValue = "arrayListOf()"
                type{
                    name = "ArrayList<T>"
                    isGeneric = true
                    genericIn = "T"
                }
            }    
            parameter{
                name = "genericMap"
                defaultValue = "hashMapOf()"
                type{
                    name = "Map<K,V>"
                    isGeneric = true
                    genericIn = "K, V"
                }
            }                
        }
    }
    /* ... */
}
``` 
This will generate a data class Foo
```kotlin
import x.y.z.MyCustomType

data class Foo<T>(
    val bar: MyCustomType,
    val generic: ArrayList<T> = arrayListOf()
)
```
together with a composable (non-generic / pseudo-) lens structure:
```kotlin
val foo = Foo<Int>(MyCustomType())

val fooPrime = foo.set{
    transaction{
        bar{f()}.
        generic{
            fluent{ add(1) }
        }
    }()  
}

fun MyCustomType.f(): MyCustomType = todo
```
If you need to perform operations on Foo in a coroutine, i.e if the function f from above suspends, just use
```kotlin
suspend fun MyCustomType.f(): MyCustomType = todo
GlobalScope.launch{
    val fooPrime = foo.suspendSet{
        suspendTransaction{
            bar{f()}.
            generic{
                suspendFluent{ add(1) }
            }
        }()  
    }
}

```
#### Composability
Suppose that you have generated to data classes
```kotlin
data class Foo(val bar: Bar = Bar())

data class Bar(
    val value1: String = "",
    val value2: String = "",
    val value3: String = ""
)
```
Then you compose the lenses like
```kotlin
val foo = Foo()
val manipulated = foo.set{
    transaction{
        bar{ 
            set{
                transaction{
                    value1{
                        "new value 1"
                    }.
                    value2{
                        "new value 2"
                    }.
                    value3{
                        "new value 3"
                    }
                }()
            } 
        }
    }()
}
```
### Sealed classes
#### Example
```kotlin
algebraicTypes{
    dataClasees{
        sealedClass{
            name = "Sealed"
            dataRepesentative{
                name = "One"   
                parameters {
                    /* ... */          
                }
            }
            dataRepesentative{
                nane = "Two"
                parameters{
                    /* --- */
                }
            }
        }
    }
}
```
#### Hints
  1. If parameters from different classes within a sealed class have the same name, they should also have the same type. 
     Otherwise we get a platform declaration clash 
     
### Serialization of Lenses and Prisms
#### Hints:
  1. Serialization rules out generics !

## Planned:
  + [ ] Automated kotlin-serialization for generated data classes and sealed classes 
  + [ ] Support generic types on sealed classes and their representatives
  + [ ] Support generation of nested sealed classes / prisms (using sealed classes / prisms only as parameters is possible)
  + [ ] Support other representatives than data classes
  + [x] Generate 'simple' lenses and prisms, like 
  
        fun Data.param(set: Type.()->Type): Data = copy(param = param.set())
  + [ ] Getters for prisms


## Examples
 + [FontAwesomeFX Browser](https://bitbucket.org/dr-smith/evoleq-examples/src/master/fontawesomefx-viewer/src/main/kotlin/org/drx/evoleq/examples/fontawesomefxviewer/component/stage/main-stage.kt) (lines 86 to 120): Sophisticated usage of product-types within a process.
