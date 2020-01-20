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
package org.drx.plugin.algebraictypes.generate

import org.drx.plugin.algebraictypes.basePath
import org.drx.plugin.algebraictypes.generate.optics.buildAuxiliaryFunctions
import org.gradle.api.Project
import java.io.File

fun generateDslMarker(project: Project) {
    val dir = File("${project.projectDir}$basePath")
    if(!dir.exists()) {
        dir.mkdirs()
    }
    val file = File("${project.projectDir}$basePath/dsl-marker.kt")
    if(!file.exists()) {
        file.writeText(buildDslMarkerFileContent())
    }
}

fun buildDslMarkerFileContent(): String = """${license()}
    |
    |package org.drx.generated
    |
    |@DslMarker annotation class AlgebraicTypesDsl
""".trimMargin()