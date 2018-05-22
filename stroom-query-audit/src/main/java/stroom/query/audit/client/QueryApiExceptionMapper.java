package stroom.query.audit.client;

import org.eclipse.jetty.http.HttpStatus;
import stroom.query.audit.service.QueryApiException;

import javax.ws.rs.core.Response;

public final class QueryApiExceptionMapper {
    private QueryApiExceptionMapper() {

    }

    public static QueryApiException create(final Response response) {
        final int status = response.getStatus();
        response.close();

        switch (status) {
            case HttpStatus.UNAUTHORIZED_401:
                return new UnauthenticatedException();
            case HttpStatus.FORBIDDEN_403:
                return new UnauthorisedException();
            case HttpStatus.NOT_FOUND_404:
                return new NotFoundException();
        }

        return new QueryApiException(String.format("Bad status returned from server: %d", status));
    }
}
