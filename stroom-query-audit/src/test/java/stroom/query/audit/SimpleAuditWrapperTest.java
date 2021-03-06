package stroom.query.audit;

import event.logging.EventLoggingService;
import event.logging.ObjectOutcome;
import event.logging.Outcome;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.Test;
import stroom.query.security.ServiceUser;

import javax.ws.rs.core.Response;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

class SimpleAuditWrapperTest {

    @Test
    void test() {
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
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200);
        verify(eventLoggingService).log(any());
    }
}
