package gov.procure.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Single deployable entry point for the modular monolith. Component-scans every bounded-context
 * module and the shared kernel. Each module remains independently compilable; this class is the
 * only place that knows about all of them — the seam along which a module can be extracted later.
 */
@SpringBootApplication
@ComponentScan(basePackages = "gov.procure")
@EntityScan(basePackages = "gov.procure")          // JPA entities live in each module's infrastructure
@EnableJpaRepositories(basePackages = "gov.procure") // Spring Data repos live across modules
public class ProcurementPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProcurementPlatformApplication.class, args);
    }
}
