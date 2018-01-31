package stroom.query.audit.authorisation;

import stroom.query.api.v2.DocRef;
import stroom.query.audit.security.ServiceUser;

public class NoAuthAuthorisationServiceImpl implements AuthorisationService {
    @Override
    public boolean isAuthorised(ServiceUser serviceUser, DocRef docRef, String permissionName) {
        // wave it all through
        return true;
    }
}
