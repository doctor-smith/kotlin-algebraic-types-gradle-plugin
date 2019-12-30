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
package org.drx.plugin.algebraictypes

object TestConfig {
    const val version = "1.0.12"
    const val group = "org.drx"
    const val pluginName = "kotlin-algebraic-types-plugin"
    object BuildFile {
        const val initContent =
                "import org.drx.plugin.algebraictypes.*\n" +
                "\n" +
                "\n" +
                "plugins{\n" +
                "    id(\"org.drx.kotlin-algebraic-types-plugin\") version \"$version\"\n" +
                "}\n"

        val initContentLocalRepo = """import org.drx.plugin.algebraictypes.*
            |
            |
            |buildscript {
            |    repositories {
            |        mavenLocal()
            |        mavenCentral()
            |        jcenter()
            |    }
            |    dependencies {
            |        classpath("$group:$pluginName:$version")
            |    }
            |}
            |apply(plugin = "$group.$pluginName")


        """.trimMargin()
    }
}