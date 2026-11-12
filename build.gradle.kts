plugins {
    java
    application
    id("org.springframework.boot") version "4.1.0"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "dev.brice"
version = "1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

application {
    mainClass = "dev.brice.parking.Application"
}

repositories {
    mavenCentral()
}

dependencies {
    // adaptador de vistas: Spring + Thymeleaf + login
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity6")

    // adaptador REST
    implementation("io.javalin:javalin:7.2.4")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    implementation("org.mindrot:jbcrypt:0.4") // hash de contraseñas

    // base de datos: pendiente (JDBI + la BD que asigne el docente)
    // implementation("org.jdbi:jdbi3-core:3.55.0")
    // implementation("org.jdbi:jdbi3-sqlobject:3.55.0")

    // reportes PDF: pendiente
    // implementation("com.github.librepdf:openpdf:3.0.5")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8" // evita textos con tildes dañados al compilar en Windows
}

tasks.test {
    useJUnitPlatform()
}
