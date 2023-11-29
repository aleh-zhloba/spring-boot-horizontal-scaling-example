plugins {
    application

    kotlin("jvm") version "1.9.20"
    kotlin("plugin.jpa") version "1.9.20"
    kotlin("plugin.spring") version "1.9.20"

    id("org.springframework.boot") version "3.2.0"
    id("io.spring.dependency-management") version "1.1.3"

    id("com.google.cloud.tools.jib") version "3.4.0"
    id("org.jmailen.kotlinter") version "4.0.0"
}

group = "app.hopsify"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("jakarta.persistence:jakarta.persistence-api")

    implementation("org.postgresql:postgresql:42.6.0")
    implementation("org.postgresql:r2dbc-postgresql:1.0.2.RELEASE")

    implementation("io.github.aleh-zhloba:postgresql-messaging:0.5.0")

    implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")

    implementation("net.javacrumbs.shedlock:shedlock-spring:5.10.0")
    implementation("net.javacrumbs.shedlock:shedlock-provider-jdbc-template:5.10.0")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.github.oshai:kotlin-logging-jvm:5.1.0")

    testImplementation(kotlin("test"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webflux")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("com.google.cloud.tools:jib-core:0.25.0")
    testImplementation("com.icegreen:greenmail:2.0.0")
    testImplementation("com.icegreen:greenmail-junit5:2.0.0")
    testImplementation("io.projectreactor.kotlin:reactor-kotlin-extensions:1.2.2")
    testImplementation("io.projectreactor:reactor-test")
}

tasks.test {
    dependsOn("jibDockerBuild")
    useJUnitPlatform()
}

jib {
    val jibContainerArch = if (System.getProperty("os.arch") == "aarch64") "arm64" else "amd64"

    from {
        image = "eclipse-temurin:21-jre-alpine"

        platforms {
            platform {
                architecture = jibContainerArch
                os = "linux"
            }
        }
    }
    to {
        image = "${rootProject.name}:$version"
    }
    container {
        mainClass = "app.hopsify.MainKt"
        ports = listOf("8080")
    }
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("MainKt")
}

kotlinter {
    ignoreFailures = false
    reporters = arrayOf("checkstyle", "plain")
}

tasks.check {
    dependsOn("installKotlinterPrePushHook")
}
