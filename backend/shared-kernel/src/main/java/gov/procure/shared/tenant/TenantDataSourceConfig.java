package gov.procure.shared.tenant;

import javax.sql.DataSource;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Configuration;

/**
 * Wraps the autoconfigured {@link DataSource} with {@link TenantConnectionPreparer} so every
 * connection has {@code app.current_tenant} set from {@link TenantContext} before use. This makes
 * PostgreSQL RLS the enforcement point for tenant isolation across the whole application.
 */
@Configuration
public class TenantDataSourceConfig implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (bean instanceof DataSource dataSource && !(bean instanceof TenantConnectionPreparer)) {
            return new TenantConnectionPreparer(dataSource);
        }
        return bean;
    }
}
