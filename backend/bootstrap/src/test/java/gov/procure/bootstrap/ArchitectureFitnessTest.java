package gov.procure.bootstrap;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Architectural fitness functions — the compiler-checked enforcement of coding-standards.md.
 * Runs in {@code bootstrap} because that module depends on every bounded-context module, so the
 * whole {@code gov.procure} class graph is on the classpath and visible to ArchUnit. A boundary
 * violation fails the build with a precise message instead of silently rotting the architecture.
 */
class ArchitectureFitnessTest {

    private final JavaClasses classes = new ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .importPackages("gov.procure");

    /** The domain is the core: it may not reach outward into application, api, or infrastructure. */
    @Test
    void domainDoesNotDependOnOuterLayers() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..application..", "..api..", "..infrastructure..");
        rule.check(classes);
    }

    /** The application layer orchestrates the domain but must not depend on api or infrastructure. */
    @Test
    void applicationDoesNotDependOnApiOrInfrastructure() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..api..", "..infrastructure..");
        rule.check(classes);
    }

    /** Domain stays persistence- and framework-agnostic (no Spring, no JPA leaking into the core). */
    @Test
    void domainHasNoFrameworkDependencies() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("org.springframework..", "jakarta.persistence..");
        rule.check(classes);
    }

    /** Bounded contexts must form a DAG — no cyclic dependencies between contexts. */
    @Test
    void boundedContextsAreFreeOfCycles() {
        ArchRule rule = SlicesRuleDefinition.slices()
            .matching("gov.procure.(*)..")
            .should().beFreeOfCycles();
        rule.check(classes);
    }

    /**
     * A bounded-context module may depend on another context only through its {@code api} package
     * (or shared-kernel / events) — never its internals. Add new modules to {@code contexts}.
     */
    @Test
    void contextsDoNotReachIntoEachOthersInternals() {
        String[] contexts = {"procurement", "audit"};
        for (String context : contexts) {
            ArchRule rule = noClasses()
                .that().resideInAPackage("gov.procure." + context + "..")
                .should().dependOnClassesThat(otherContextInternals(context))
                .as(context + " must use other contexts via their api package or events");
            rule.check(classes);
        }
    }

    private DescribedPredicate<com.tngtech.archunit.core.domain.JavaClass> otherContextInternals(
            String context) {
        return DescribedPredicate.describe("another context's internals", clazz -> {
            String pkg = clazz.getPackageName();
            if (!pkg.startsWith("gov.procure.")) return false;
            if (pkg.startsWith("gov.procure." + context)) return false;
            if (pkg.startsWith("gov.procure.shared")) return false;
            if (pkg.startsWith("gov.procure.bootstrap")) return false;
            return pkg.contains(".domain") || pkg.contains(".application")
                || pkg.contains(".infrastructure");
        });
    }
}
