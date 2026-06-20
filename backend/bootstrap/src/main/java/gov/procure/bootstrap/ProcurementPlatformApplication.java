package gov.procure.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Single deployable entry point for the modular monolith. Component-scans every bounded-context
 * module and the shared kernel. Each module remains independently compilable; this class is the
 * only place that knows about all of them — the seam along which a module can be extracted later.
 */
@SpringBootApplication
@ComponentScan(basePackages = "gov.procure")
public class ProcurementPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProcurementPlatformApplication.class, args);
    }
}
