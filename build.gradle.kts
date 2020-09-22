val vKotlinx: String by project
val vKtor: String by project
val vJodatime: String by project
val vLogback: String by project
val vExposed: String by project
val vSqlite: String by project

plugins {
    application
    kotlin("jvm")
}

group = "com.task.appChecker"
version = "0.0.1"

application {
    mainClassName = "io.ktor.server.netty.EngineMain"
}

repositories {
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$vKotlinx")
    implementation("io.ktor:ktor-server-netty:$vKtor")
    implementation("io.ktor:ktor-server-core:$vKtor")
    implementation("io.ktor:ktor-html-builder:$vKtor")
    implementation("io.ktor:ktor-server-host-common:$vKtor")
    implementation("io.ktor:ktor-websockets:$vKtor")
    implementation("joda-time:joda-time:$vJodatime")
    implementation("ch.qos.logback:logback-classic:$vLogback")
    implementation("org.jetbrains.exposed:exposed:$vExposed")
    implementation("org.xerial:sqlite-jdbc:$vSqlite")
}

kotlin.sourceSets["main"].kotlin.srcDirs("src")
sourceSets["main"].resources.srcDirs("resources")
