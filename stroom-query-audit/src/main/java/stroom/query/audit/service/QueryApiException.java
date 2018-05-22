package stroom.query.audit.service;

public class QueryApiException extends Exception {
    public QueryApiException() {
    }

    public QueryApiException(String message) {
        super(message);
    }

    public QueryApiException(String message, Throwable cause) {
        super(message, cause);
    }

    public QueryApiException(Throwable cause) {
        super(cause);
    }

    public QueryApiException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
