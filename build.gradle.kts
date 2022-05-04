val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val kaml_version: String by project
val koin_version: String by project
val hamcrest_version: String by project
val coroutines_version: String by project
val kotlin_logging_version: String by project
val mockk_version: String by project
val junit_version: String by project

plugins {
    val kotlinVersion = "1.5.31"
    application
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
}

group = "gosha.kalosha"
version = "0.0.1"
application {
    mainClass.set("gosha.kalosha.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/ktor/eap") }
}

dependencies {
    // coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines_version")

    // server
    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-server-cio:$ktor_version")
    testImplementation("io.ktor:ktor-server-tests:$ktor_version")

    // serialization
    implementation("io.ktor:ktor-serialization:$ktor_version")
    implementation("com.charleskorn.kaml:kaml:$kaml_version")

    // client
    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")
    testImplementation("io.ktor:ktor-client-mock:$ktor_version")

    // DI
    implementation("io.insert-koin:koin-ktor:$koin_version")
    implementation("io.insert-koin:koin-logger-slf4j:$koin_version")
    testImplementation("io.insert-koin:koin-test:$koin_version")
    testImplementation("io.insert-koin:koin-test-junit5:$koin_version")

    // logging
    implementation("io.github.microutils:kotlin-logging:$kotlin_logging_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")

    // scheduling
    implementation("dev.inmo:krontab:0.7.2")

    // test
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
    testImplementation("org.hamcrest:hamcrest-all:$hamcrest_version")
    implementation("org.junit.jupiter:junit-jupiter:$junit_version")
    testImplementation("io.mockk:mockk:$mockk_version")
}

tasks.test {
    useJUnitPlatform()
}

tasks {
    "build" {
        dependsOn(fatJar)
    }
}

val fatJar = tasks.create("fatJar", Jar::class) {
    group = "my tasks"
    description = "Creates a self-contained fat JAR of the application that can be run."
    manifest.attributes["Main-Class"] = "gosha.kalosha.ApplicationKt"
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    val dependencies = configurations
        .runtimeClasspath
        .get()
        .map(::zipTree)
    from(dependencies)
    with(tasks.jar.get())
}
