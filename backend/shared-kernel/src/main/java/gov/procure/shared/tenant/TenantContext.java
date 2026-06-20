package gov.procure.shared.tenant;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**
 * Holds the current tenant for the executing thread. Set by {@code TenantContextFilter}
 * for web requests and by event consumers / scheduled jobs via {@link #runAs}.
 *
 * <p>If unset, data access fails closed because PostgreSQL RLS returns zero rows
 * when {@code app.current_tenant} is not configured.
 */
public final class TenantContext {

    private static final ThreadLocal<TenantId> CURRENT = new ThreadLocal<>();

    private TenantContext() {}

    public static void set(TenantId tenantId) {
        CURRENT.set(tenantId);
    }

    public static Optional<TenantId> current() {
        return Optional.ofNullable(CURRENT.get());
    }

    public static TenantId require() {
        return current().orElseThrow(() ->
            new IllegalStateException("No tenant in context — request not tenant-scoped"));
    }

    public static void clear() {
        CURRENT.remove();
    }

    /** Run a block under an explicit tenant, restoring the previous tenant afterward. */
    public static <T> T runAs(TenantId tenantId, Supplier<T> work) {
        TenantId previous = CURRENT.get();
        try {
            CURRENT.set(tenantId);
            return work.get();
        } finally {
            if (previous != null) CURRENT.set(previous); else CURRENT.remove();
        }
    }

    public static void runAs(TenantId tenantId, Runnable work) {
        runAs(tenantId, () -> { work.run(); return null; });
    }
}
