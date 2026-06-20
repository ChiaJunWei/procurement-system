rootProject.name = "gov-procurement-platform"

// The single deployable wires every module together.
include("bootstrap")

// Cross-context primitives. No business logic.
include("shared-kernel")

// One Gradle module per bounded context. Add new contexts here.
include("modules:procurement")
// include("modules:identity")
// include("modules:agency")
// include("modules:vendor")
// include("modules:contract")
// include("modules:workflow")
// include("modules:policy")
// include("modules:audit")
// include("modules:integration")
// include("modules:reporting")
