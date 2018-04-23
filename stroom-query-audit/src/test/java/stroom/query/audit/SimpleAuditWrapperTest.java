package stroom.query.audit;

import event.logging.EventLoggingService;
import event.logging.ObjectOutcome;
import event.logging.Outcome;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Test;
import stroom.security.ServiceUser;

import javax.ws.rs.core.Response;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class SimpleAuditWrapperTest {

    @Test
    public void test() {
        final String auditType = "TestFunction";
        final EventLoggingService eventLoggingService = spy(new QueryEventLoggingService());
        final ServiceUser user = new ServiceUser.Builder()
                .name(UUID.randomUUID().toString())
                .jwt(UUID.randomUUID().toString())
                .build();
        final Response response = SimpleAuditWrapper.withUser(user)
                .withResponse(() -> Response.ok().build())
                .withDefaultAuthSupplier()
                .withPopulateAudit((eventDetail, r, exception) -> {
                    eventDetail.setTypeId(auditType);
                    eventDetail.setDescription("Copy a Doc Ref");

                    final ObjectOutcome createObj = new ObjectOutcome();
                    final Outcome create = new Outcome();
                    createObj.setOutcome(create);
                    create.setDescription("Test Event, nothing really happens");
                    eventDetail.setCreate(createObj);
                })
                .callAndAudit(eventLoggingService);
        assertEquals(HttpStatus.OK_200, response.getStatus());
        verify(eventLoggingService).log(any());
    }
}
