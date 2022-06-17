plugins {
    id("java")
}

group = "net.tplus"
version = "3.1-BETA"

repositories {
    mavenCentral()
}

dependencies {

}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
