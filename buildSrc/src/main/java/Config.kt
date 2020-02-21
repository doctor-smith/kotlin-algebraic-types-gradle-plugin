object Config {

    object ProjectData {
        const val group = "org.drx"
        const val version = "1.0.16"
        const val artifactId = "kotlin-algebraic-types-plugin"
        const val description = "Generate Algebraic Types"

        const val vcsUrl = "https://github.com/doctor-smith/kotlin-algebraic-types-plugin.git"

        object Developers {
            object DrX {
                const val id = "drx"
                const val name = "Dr. Florian Schmidt"
                const val email = "schmidt@alpha-structure.com"
            }
        }
    }

    object Versions {
        const val kotlin = "1.3.50"
        const val coroutines = "1.3.1"
        const val junit = "4.12"

        const val evoleq = "1.1.1"
    }

    object Dependencies {
        const val kotlinStandardLibrary = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlin}"
        const val coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}"

        const val evoleq = "org.drx:evoleq:${Versions.evoleq}"
    }
}
