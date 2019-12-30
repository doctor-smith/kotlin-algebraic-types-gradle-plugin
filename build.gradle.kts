plugins {
    maven
    `maven-publish`
    `java-gradle-plugin`
    id("com.gradle.plugin-publish") version "0.10.0"
    `kotlin-dsl`
    java
    groovy
    id ("com.github.hierynomus.license") version "0.15.0"
    id ("org.jetbrains.dokka") version "0.10.0"
}

group = Config.ProjectData.group
version = Config.ProjectData.version

repositories {
    mavenLocal()
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(gradleApi())
    implementation(localGroovy())
    compile("org.codehaus.groovy:groovy-all:2.4.15")
    testCompile("junit", "junit", "4.12")
}



configure<JavaPluginConvention> {
    sourceSets{
        getByName("main"){
            resources.srcDirs("src/main/resources")
        }
    }
}

tasks {

    val sourceSets: SourceSetContainer by project

    val sourcesJar by creating(Jar::class) {
        dependsOn(JavaPlugin.CLASSES_TASK_NAME)
        classifier = "sources"
        from(sourceSets["main"].allSource)
        from(sourceSets["main"].resources)
    }

    val javadocJar by creating(Jar::class) {
        dependsOn(JavaPlugin.JAVADOC_TASK_NAME)
        classifier = "javadoc"
        from(tasks["javadoc"])
    }

    val dokkaJar by creating(Jar::class) {
        group = JavaBasePlugin.DOCUMENTATION_GROUP
        description = "Assembles Kotlin docs with Dokka"
        classifier = "javadoc"
        from(tasks["dokka"])
    }

    artifacts {
        add("archives", sourcesJar)
        add("archives", dokkaJar)
    }

    getByName("test"){
        dependsOn(getByName("publishToMavenLocal"))
    }
}

pluginBundle {
    website = "https://github.com/doctor-smith/kotlin-algebraic-types-plugin"
    vcsUrl = "https://github.com/doctor-smith/kotlin-algebraic-types-plugin.git"
    tags = listOf("kotlin", "functional", "algebraic types", "generate", "category", " lens", "prism")
}

gradlePlugin {
    plugins {
        create("KotlinAlgebraicTypesPlugin") {
            id = "org.drx.kotlin-algebraic-types-plugin"
            displayName = "Kotlin Algebraic Types Plugin"
            description = "Generate algebraic types of arbitrary finite dimension"
            implementationClass = "org.drx.plugin.algebraictypes.KotlinAlgebraicTypesPlugin"
            version = Config.ProjectData.version
        }
    }
}


task("writeNewPom") {
    doLast {
        maven.pom {
            withGroovyBuilder {
                "project" {
                    "licenses" {
                        "license" {
                            setProperty("name", "The Apache Software License, Version 2.0")
                            setProperty("url", "http://www.apache.org/licenses/LICENSE-2.0.txt")
                            setProperty("distribution", "repo")
                        }
                    }
                }
            }
        }.writeTo("$buildDir/pom.xml")
    }
}

publishing {
    publications {
        create<MavenPublication>("AlgebraicTypesPublication"){
            artifactId = Config.ProjectData.artifactId
            groupId = Config.ProjectData.group
            from (components["java"])

            artifact (tasks.getByName("sourcesJar")) {
                classifier = "sources"
            }

            artifact (tasks.getByName("javadocJar")) {
                classifier = "javadoc"
            }

            pom.withXml {
                val root = asNode()
                root.appendNode("description", Config.ProjectData.description)
                root.appendNode("name", Config.ProjectData.artifactId)
                root.appendNode("url", Config.ProjectData.vcsUrl)
                root.children().addAll(maven.pom().dependencies)
            }

            pom {
                developers{
                    developer{
                        id.set(Config.ProjectData.Developers.DrX.id)
                        name.set(Config.ProjectData.Developers.DrX.name)
                        email.set(Config.ProjectData.Developers.DrX.email)
                    }
                }
            }

        }
    }
}