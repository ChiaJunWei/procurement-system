// Shared kernel: cross-context primitives only (domain base types, tenant, events, workflow &
// policy ports, error handling, api helpers). NO business logic, NO dependency on any module.
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.kafka:spring-kafka")
}
