plugins {
    id("java")
}

group = "net.nuggetmc"
version = "3.1-BETA"

val jarName = "TerminatorPlus-" + version;

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":TerminatorPlus-Plugin"))
    implementation(project(":TerminatorPlus-API"))
}

tasks.jar {
    from(configurations.compileClasspath.get().map { if (it.isDirectory()) it else zipTree(it) })
    archiveFileName.set(jarName + ".jar")
}
//TODO currently, the resources are in src/main/resources, because gradle is stubborn and won't include the resources in TerminatroPlus-Plugin/src/main/resources, will need to fix

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
