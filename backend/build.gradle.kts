// Root build. Conventions shared by every module live here (in a fuller setup, in buildSrc
// convention plugins). Modules declare ONLY their own dependencies + shared-kernel.
plugins {
    java
    id("org.springframework.boot") version "3.3.0" apply false
    id("io.spring.dependency-management") version "1.1.5"
}

allprojects {
    group = "gov.procure"
    version = "0.1.0"
    repositories { mavenCentral() }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "io.spring.dependency-management")

    java {
        toolchain { languageVersion.set(JavaLanguageVersion.of(21)) }
    }

    the<io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension>().apply {
        imports { mavenBom("org.springframework.boot:spring-boot-dependencies:3.3.0") }
    }

    dependencies {
        "testImplementation"("org.springframework.boot:spring-boot-starter-test")
        "testImplementation"("org.assertj:assertj-core")
        // ArchUnit enforces architectural boundaries for EVERY module.
        "testImplementation"("com.tngtech.archunit:archunit-junit5:1.3.0")
    }

    tasks.withType<Test> { useJUnitPlatform() }
}
