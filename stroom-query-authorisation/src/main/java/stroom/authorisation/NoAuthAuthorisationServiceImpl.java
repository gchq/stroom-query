package stroom.authorisation;

import stroom.query.api.v2.DocRef;
import stroom.security.ServiceUser;

public class NoAuthAuthorisationServiceImpl implements AuthorisationService {
    @Override
    public boolean isAuthorised(final ServiceUser serviceUser,
                                final DocRef docRef,
                                final String permissionName) {
        // wave it all through
        return true;
    }
}
