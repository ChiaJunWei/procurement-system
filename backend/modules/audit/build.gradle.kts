// Audit bounded context. Pure event consumer — depends only on shared-kernel. Builds an immutable,
// hash-chained compliance trail from every domain event on the bus.
dependencies {
    implementation(project(":shared-kernel"))
    implementation("com.fasterxml.jackson.core:jackson-databind") // EventEnvelope exposes JsonNode
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.kafka:spring-kafka")
    runtimeOnly("org.postgresql:postgresql")
}
