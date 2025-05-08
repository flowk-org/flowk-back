plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	id("org.springframework.boot") version "3.4.2"
	id("io.spring.dependency-management") version "1.1.7"
	kotlin("plugin.jpa") version "1.9.25"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

	// mongodb
	implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
	implementation("org.jetbrains.kotlin:kotlin-reflect")

	implementation("com.github.docker-java:docker-java-transport-httpclient5:3.2.3")
	implementation("com.github.docker-java:docker-java:3.3.4")
	implementation("com.github.docker-java:docker-java-transport-okhttp:3.3.4")

	// minio
	implementation("io.minio:minio:8.5.7")

	implementation("org.postgresql:postgresql:42.7.2")
	implementation("org.modelmapper:modelmapper:3.2.0")
	implementation("ru.yandex.clickhouse:clickhouse-jdbc:0.3.2")
	implementation("com.clickhouse:client-v2:0.6.5")
	implementation("org.eclipse.jgit:org.eclipse.jgit:6.9.0.202403050737-r")

	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.0")

	// WebSocket + STOMP
	implementation("org.springframework.boot:spring-boot-starter-websocket")
	implementation("org.springframework.kafka:spring-kafka")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("io.fabric8:kubernetes-client:6.9.1")
	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

allOpen {
	annotation("jakarta.persistence.Entity")
	annotation("jakarta.persistence.MappedSuperclass")
	annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
