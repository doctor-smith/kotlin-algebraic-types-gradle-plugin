object Config {

    object ProjectData {
        val group = "org.drx"
        val version = "1.0.2"
        val artifactId = "kotlin-algebraic-types-plugin"
    }

    object Versions {

        val kotlin = "1.3.50"
        val coroutines = "1.3.1"
        val junit = "4.12"
    }

    object Dependencies {
        val kotlinStandardLibrary = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlin}"
        val coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}"
    }
}
