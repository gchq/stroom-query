package stroom.query.audit.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

public class SimpleJsonHttpClient<E extends Throwable> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DocRefResourceHttpClient.class);

    private ObjectMapper objectMapper = new ObjectMapper();

    private final Function<Exception, E> exceptionWrapper;

    public SimpleJsonHttpClient(final Function<Exception, E> exceptionWrapper) {
        this.exceptionWrapper = exceptionWrapper;
    }

    private Response send(final String method,
                          final String url,
                          final Object body,
                          final String jwt,
                          final Map<String, Object> queryParams) throws E {
        try {
            final StringBuilder urlToUse = new StringBuilder(url);
            if (queryParams.size() > 0) {
                urlToUse.append("?");

                final AtomicBoolean firstSeen = new AtomicBoolean(false);
                queryParams.forEach((key, value) -> {
                    if (firstSeen.get()) {
                        urlToUse.append("&");
                    } else {
                        firstSeen.set(true);
                    }
                    urlToUse.append(String.format("%s=%s", key, value));
                });
            }

            final URL urlObj = new URL(urlToUse.toString());
            final HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();

            //add request header
            con.setRequestMethod(method);
            con.setRequestProperty("accept", MediaType.APPLICATION_JSON);
            con.setRequestProperty("Content-Type", MediaType.APPLICATION_JSON);
            if (null != jwt) {
                con.setRequestProperty("Authorization", String.format("Bearer %s", jwt));
            }

            // Send the body, or just connect
            if (null != body) {
                con.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                wr.writeBytes(objectMapper.writeValueAsString(body));
                wr.flush();
                wr.close();
            } else {
                con.setDoOutput(false);
                con.connect();
            }

            final int responseCode = con.getResponseCode();
            LOGGER.debug(String.format("Sent %s to %s, received %d", method, url, responseCode));

            final StringBuilder response = new StringBuilder();

            if (responseCode == HttpStatus.OK_200) {
                try (BufferedReader in = new BufferedReader(
                        new InputStreamReader(con.getInputStream()))) {
                    String inputLine;

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                }
            }

            //print result
            return Response.status(responseCode)
                    .entity(response.toString())
                    .build();
        } catch (Exception e) {
            throw exceptionWrapper.apply(e);
        }
    }

    public RequestBuilder post(final String url) {
        return new RequestBuilder().url(url).method("POST");
    }

    public RequestBuilder put(final String url) {
        return new RequestBuilder().url(url).method("PUT");
    }

    public RequestBuilder get(final String url) {
        return new RequestBuilder().url(url).method("GET");
    }

    public RequestBuilder delete(final String url) {
        return new RequestBuilder().url(url).method("DELETE");
    }

    public class RequestBuilder {
        private String method;
        private String url;
        private Object body;
        private String jwt;
        private Map<String, Object> queryParams = new HashMap<>();

        public RequestBuilder method(final String value) {
            this.method = value;
            return this;
        }

        public RequestBuilder url(final String value) {
            this.url = value;
            return this;
        }

        public RequestBuilder body(final Object value) {
            this.body = value;
            return this;
        }

        public RequestBuilder jwt(final String value) {
            this.jwt = value;
            return this;
        }

        public RequestBuilder queryParam(final String key, final Object value) {
            this.queryParams.put(key, value);
            return this;
        }

        public Response send() throws E {
            return SimpleJsonHttpClient.this.send(this.method, this.url, this.body, this.jwt, this.queryParams);
        }
    }
}
