import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "3.1.3"
	id("io.spring.dependency-management") version "1.1.3"
	kotlin("jvm") version "1.8.22"
	kotlin("plugin.spring") version "1.8.22"
}

group = "com.nikik0"
version = "1.0.0"

java {
	sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
	implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.3.0")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("jakarta.validation:jakarta.validation-api:3.0.2")
	implementation("org.springframework.boot:spring-boot-starter-actuator:3.2.0")
	implementation("io.micrometer:micrometer-registry-prometheus:1.12.1")
	implementation("net.logstash.logback:logstash-logback-encoder:7.4")
	implementation("io.sentry:sentry-spring-boot-starter:7.2.0")

	// https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-coroutines-slf4j
	runtimeOnly("org.jetbrains.kotlinx:kotlinx-coroutines-slf4j:1.8.0-RC2")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-slf4j:1.8.0-RC2")

	// https://mvnrepository.com/artifact/io.sentry/sentry-logback
	testImplementation("io.sentry:sentry-logback:7.2.0")
	runtimeOnly("org.postgresql:postgresql")
	runtimeOnly("org.postgresql:r2dbc-postgresql")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("io.projectreactor:reactor-test")
	testImplementation("org.testcontainers:testcontainers")
	testImplementation("org.testcontainers:junit-jupiter")
	testImplementation("org.testcontainers:postgresql")
	testImplementation("io.mockk:mockk:1.13.8")
	testImplementation ("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0-RC")
	testImplementation("org.flywaydb:flyway-core:9.16.0")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs += "-Xjsr305=strict"
		jvmTarget = "17"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
