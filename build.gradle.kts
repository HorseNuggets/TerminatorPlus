plugins {
    id("java")
    id("net.nuggetmc.java-conventions")
}

val jarName = "TerminatorPlus-" + version;

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":TerminatorPlus-Plugin", "reobf"))
    implementation(project(":TerminatorPlus-API"))
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = Charsets.UTF_8.name() // We want UTF-8 for everything
    filesMatching("plugin.yml") {
        expand(props)
    }
}

tasks.jar {
    from(configurations.compileClasspath.get().map { if (it.isDirectory()) it else zipTree(it) })
    archiveFileName.set(jarName + ".jar")
}

//TODO currently, the resources are in src/main/resources, because gradle is stubborn and won't include the resources in TerminatorPlus-Plugin/src/main/resources, will need to fix

/*
task copyPlugin(type: Copy) {
    from 'build/libs/' + jarName + '.jar'
    into 'run/plugins'
}
 */

tasks.register("copyPlugin", Copy::class.java) {
    from("build/libs/" + jarName + ".jar")
    into("run/plugins")
}
