plugins {
    id("java")
    id("org.springframework.boot") version "3.3.13"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "ru.itrum.wallet"
version = "1.0.0"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
}

val springBootVersion = "3.3.13"
dependencies {
    implementation("org.springframework.boot:spring-boot-starter:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-web:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-validation:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:$springBootVersion")
    implementation("org.postgresql:postgresql:42.7.10")
    implementation("org.liquibase:liquibase-core:5.0.1")

    testImplementation("org.springframework.boot:spring-boot-starter-test:$springBootVersion")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.getByName<Jar>("jar") {
    enabled = false
}

tasks.test {
    useJUnitPlatform()
}