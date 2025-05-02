plugins {
	java
	id("org.springframework.boot") version "3.4.5"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "lnu.ishchuk"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {

	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("org.springframework.boot:spring-boot-starter-graphql")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	runtimeOnly("org.postgresql:postgresql")
	implementation("com.graphql-java:java-dataloader:4.0.0")
	implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")


	implementation("org.springframework.boot:spring-boot-starter")


	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")

	implementation("org.keycloak:keycloak-spring-boot-starter:22.0.3")

	implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

	testImplementation("javax.servlet:javax.servlet-api:4.0.1")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	testImplementation("org.springframework.graphql:spring-graphql-test")
	testImplementation("com.github.tomakehurst:wiremock-jre8-standalone:2.35.0")
	testImplementation("org.testcontainers:junit-jupiter:1.18.3")
	testImplementation("org.testcontainers:postgresql:1.18.3")
	testImplementation("io.projectreactor:reactor-test")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
