package gov.procure.shared.security;

import java.util.Optional;

/** Port for obtaining the current authenticated user. Implemented from the JWT in infrastructure. */
public interface CurrentUserProvider {

    Optional<CurrentUser> current();

    default CurrentUser require() {
        return current().orElseThrow(() -> new IllegalStateException("No authenticated user"));
    }
}
