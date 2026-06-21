// Auto-provisions the Java 21 toolchain if it is not already installed locally.
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "gov-procurement-platform"

// The single deployable wires every module together.
include("bootstrap")

// Cross-context primitives. No business logic.
include("shared-kernel")

// One Gradle module per bounded context. Add new contexts here.
include("modules:procurement")
include("modules:audit")
// include("modules:identity")
// include("modules:agency")
// include("modules:vendor")
// include("modules:contract")
// include("modules:workflow")
// include("modules:policy")
// include("modules:audit")
// include("modules:integration")
// include("modules:reporting")
