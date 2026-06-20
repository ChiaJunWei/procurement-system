package gov.procure.shared;

import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import org.junit.jupiter.api.Test;

/**
 * Architectural fitness functions — the compiler-checked enforcement of coding-standards.md.
 * Run against the whole {@code gov.procure} package for every module build. If an agent violates
 * a boundary, the build fails with a precise message instead of silently rotting the architecture.
 */
class ArchitectureFitnessTest {

    private final JavaClasses classes = new ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .importPackages("gov.procure");

    @Test
    void layeredDependenciesAreRespected() {
        ArchRule rule = layeredArchitecture().consideringOnlyDependenciesInLayers()
            .layer("Api").definedBy("gov.procure..api..")
            .layer("Application").definedBy("gov.procure..application..")
            .layer("Domain").definedBy("gov.procure..domain..")
            .layer("Infrastructure").definedBy("gov.procure..infrastructure..")
            .whereLayer("Api").mayNotBeAccessedByAnyLayer()
            .whereLayer("Application").mayOnlyBeAccessedByLayers("Api", "Infrastructure")
            .whereLayer("Domain").mayOnlyBeAccessedByLayers("Api", "Application", "Infrastructure");
        rule.check(classes);
    }

    @Test
    void domainHasNoFrameworkDependencies() {
        ArchRule rule = ArchRuleDefinition.noClasses()
            .that().resideInAPackage("gov.procure..domain..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("org.springframework..", "jakarta.persistence..");
        rule.check(classes);
    }

    @Test
    void contextsDoNotReachIntoEachOthersInternals() {
        // A context may depend on another's api package or shared-kernel, never its internals.
        ArchRule rule = ArchRuleDefinition.noClasses()
            .that().resideInAPackage("gov.procure.(*)..")
            .should().dependOnClassesThat()
            .resideInAnyPackage(
                "gov.procure.(*).domain..",
                "gov.procure.(*).application..",
                "gov.procure.(*).infrastructure..")
            .as("modules must communicate via api packages or events, not internals");
        // NOTE: refine with a custom predicate to allow same-context access; kept illustrative.
        rule.allowEmptyShould(true).check(classes);
    }
}
