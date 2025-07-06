plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("com.alphacephei:vosk:0.3.45")
    implementation("com.fasterxml:jackson-xml-databind:0.6.2")
}

tasks.test {
    useJUnitPlatform()
}