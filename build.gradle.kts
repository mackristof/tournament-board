import org.jetbrains.kotlin.konan.properties.loadProperties

val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val koin_version: String by project
val koin_ktor: String by project
val exposed_version: String by project

plugins {
    application
    kotlin("jvm") version "1.7.22"
    id("io.ktor.plugin") version "2.1.3"
    id("com.jetbrains.exposed.gradle.plugin") version "0.2.1"
    id("org.flywaydb.flyway") version "9.8.1"
}

group = "com.example"
version = "0.0.1"
application {
    mainClass.set("com.example.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-auth-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-cors-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-default-headers-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktor_version")
    implementation("io.ktor:ktor-serialization-jackson-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-netty-jvm:$ktor_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.1")
    implementation("io.arrow-kt:arrow-core:1.1.2")
    implementation("io.arrow-kt:arrow-fx-coroutines:1.0.1")
    implementation("io.arrow-kt:arrow-fx-stm:1.0.1")
    implementation("io.insert-koin:koin-core:$koin_version")
    implementation("io.insert-koin:koin-ktor:$koin_ktor")
    implementation("io.insert-koin:koin-logger-slf4j:$koin_ktor")
    implementation("org.jetbrains.exposed:exposed-core:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-dao:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposed_version")
    implementation("org.flywaydb:flyway-core:8.0.1")
    implementation("org.postgresql:postgresql:42.2.24")
    implementation("com.zaxxer:HikariCP:5.0.0")
    testImplementation("io.insert-koin:koin-test:$koin_version")
    testImplementation("io.insert-koin:koin-test-junit5:$koin_version")
    testImplementation("io.ktor:ktor-server-tests-jvm:$ktor_version")
    testImplementation("io.ktor:ktor-client-auth:$ktor_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")

}

exposedCodeGeneratorConfig {

    val dbProperties = loadProperties("${projectDir}/src/main/resources/db.properties")
    configFilename = "exposedConf.yml"
    user = dbProperties["dataSource.user"].toString()
    password = dbProperties["dataSource.password"].toString()
    databaseName = dbProperties["dataSource.database"].toString()
    databaseDriver = dbProperties["dataSource.driver"].toString()
}

flyway  {
    driver = "org.postgresql.Driver"
    url = "jdbc:postgresql://localhost:5432/test"
    user = "test"
    password = "test"
}

tasks.generateExposedCode {
    dependsOn("clean")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

ktor {
    fatJar {
        archiveFileName.set("tournament.jar")
    }
}

