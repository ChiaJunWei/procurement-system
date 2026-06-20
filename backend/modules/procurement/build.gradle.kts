// Procurement bounded context. Depends ONLY on shared-kernel (+ its own infra libs).
// It must NOT declare a dependency on any other module — cross-context comms go via api/events.
dependencies {
    implementation(project(":shared-kernel"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.testcontainers:postgresql:1.19.8")
    testImplementation("org.testcontainers:junit-jupiter:1.19.8")
}
