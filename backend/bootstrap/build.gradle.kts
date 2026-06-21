// The single deployable. Wires every bounded-context module + shared-kernel into one Spring Boot
// app. Owns Flyway migrations (one schema-per-context). Adding a context = add it here + settings.
plugins {
    id("org.springframework.boot")
    application
}

dependencies {
    implementation(project(":shared-kernel"))
    implementation(project(":modules:procurement"))
    implementation(project(":modules:audit"))
    // implementation(project(":modules:identity"))  // ... other contexts as they land

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa") // @EntityScan/@EnableJpaRepositories
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("com.h2database:h2") // in-memory DB for the runnable mock profile (see MOCKS.md)

    // Full-stack slice integration tests run here (bootstrap sees every module + migrations).
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa")
    testImplementation("org.springframework.kafka:spring-kafka")
    testImplementation("org.testcontainers:postgresql:1.19.8")
    testImplementation("org.testcontainers:junit-jupiter:1.19.8")
}

application {
    mainClass.set("gov.procure.bootstrap.ProcurementPlatformApplication")
}
