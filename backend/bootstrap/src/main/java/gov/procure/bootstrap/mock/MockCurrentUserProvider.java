package gov.procure.bootstrap.mock;

import gov.procure.shared.security.CurrentUser;
import gov.procure.shared.security.CurrentUserProvider;
import java.util.Optional;
import java.util.Set;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * MOCK current user: always the fixed dev identity. Replaces {@code JwtCurrentUserProvider} under the
 * {@code mock} profile so no Keycloak token is required. See MOCKS.md.
 */
@Component
@Profile("mock")
public class MockCurrentUserProvider implements CurrentUserProvider {

    @Override
    public Optional<CurrentUser> current() {
        return Optional.of(new CurrentUser(
            MockIdentity.USER_ID, MockIdentity.AGENCY_ID,
            Set.of("procurement:requisition:create")));
    }
}
