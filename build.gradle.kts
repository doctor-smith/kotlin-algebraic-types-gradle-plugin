plugins {
    maven
    `maven-publish`
    `java-gradle-plugin`
    `kotlin-dsl`
    java
    groovy
    id ("com.github.hierynomus.license") version "0.15.0"
}

group = "org.drx"
version = "1.0.0"

repositories {
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

gradlePlugin {
    plugins {
        create("KotlinAlgebraicTypesPlugin") {
            id = "org.drx.algebraic-types-plugin"
            implementationClass = "org.drx.plugin.algebraictypes.KotlinAlgebraicTypesPlugin"
        }
    }
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
        //from(tasks.get("javadoc"))
    }
/*
    val dokkaJar by creating(Jar::class) {
        group = JavaBasePlugin.DOCUMENTATION_GROUP
        description = "Assembles Kotlin docs with Dokka"
        classifier = "javadoc"
        from(tasks["dokka"])
    }
*/
    artifacts {
        add("archives", sourcesJar)
        //add("archives", dokkaJar)
    }


}


task("writeNewPom") {
    doLast {
        maven.pom {
            withGroovyBuilder {
                "project" {
                    // setProperty("inceptionYear", "2008")
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
    /*(publications) {
        "EvoleqPublication"(MavenPublication::class) {*/
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
                root.appendNode("description", "Generate Algebraic Types")
                root.appendNode("name", Config.ProjectData.artifactId)
                root.appendNode("url", "https://github.com/doctor-smith/evoleq.git")
                root.children().addAll(maven.pom().dependencies)
            }

            pom {
                developers{
                    developer{
                        id.set("drx")
                        name.set("Dr. Florian Schmidt")
                        email.set("schmidt@alpha-structure.com")
                    }
                }
            }

        }
    }
}
