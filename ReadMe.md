[![Download](https://img.shields.io/badge/Gradle%20Plugin%20Portal-1.0.6-blue.svg)](https://plugins.gradle.org/plugin/org.drx.kotlin-algebraic-types-plugin)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

# Algebraic Types Gradle Plugin
Generate product- and sum-types of arbitrary finite 'dimension' together with related functorial maps.

## Add the plugin to your project
Add the plugin id to the plugin-block of your build.gradle.kts file
```kotlin
plugins{
    id("org.drx.kotlin-algebraic-types-plugin") version "1.0.6"
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
  

## Examples
 + [FontAwesomeFX Browser](https://bitbucket.org/dr-smith/evoleq-examples/src/master/fontawesomefx-viewer/src/main/kotlin/org/drx/evoleq/examples/fontawesomefxviewer/component/stage/main-stage.kt) (lines 86 to 120): Sophisticated usage of product-types within a process.
