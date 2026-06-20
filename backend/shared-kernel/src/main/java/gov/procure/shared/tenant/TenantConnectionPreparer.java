package gov.procure.shared.tenant;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.jdbc.datasource.DelegatingDataSource;

/**
 * Wraps the DataSource so that every checked-out connection has {@code app.current_tenant} set to
 * the current {@link TenantContext} before any statement runs. PostgreSQL RLS policies then filter
 * all queries by tenant automatically — defense in depth, even if application code forgets.
 *
 * <p>SET LOCAL scopes the GUC to the current transaction, so connection reuse across tenants in a
 * pool cannot leak. If no tenant is set, the GUC is reset to empty and RLS returns zero rows
 * (fail closed). See docs/architecture/multi-tenancy.md.
 */
public class TenantConnectionPreparer extends DelegatingDataSource {

    public TenantConnectionPreparer(DataSource target) {
        super(target);
    }

    @Override
    public Connection getConnection() throws SQLException {
        return prepare(super.getConnection());
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return prepare(super.getConnection(username, password));
    }

    private Connection prepare(Connection connection) throws SQLException {
        String tenant = TenantContext.current().map(t -> t.value().toString()).orElse("");
        try (var stmt = connection.prepareStatement("SELECT set_config('app.current_tenant', ?, false)")) {
            stmt.setString(1, tenant);
            stmt.execute();
        }
        return connection;
    }
}
