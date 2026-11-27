
package tn.bloodlink.hospital.endpoint;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.context.ApplicationContext;
import org.springframework.ws.test.server.MockWebServiceClient;
import org.springframework.xml.transform.StringSource;

import java.util.Map;

import static org.springframework.ws.test.server.RequestCreators.withPayload;
import static org.springframework.ws.test.server.ResponseMatchers.noFault;
import static org.springframework.ws.test.server.ResponseMatchers.clientOrSenderFault;
import static org.springframework.ws.test.server.ResponseMatchers.xpath;

@SpringBootTest
@ActiveProfiles("test")
class HospitalEndpointTest {

    private static final String NAMESPACE =
            "http://bloodlink.tn/hospital";

    @Autowired
    private ApplicationContext applicationContext;

    private MockWebServiceClient client;

    @BeforeEach
    void setUp() {
        client = MockWebServiceClient.createClient(applicationContext);
    }

    private String requestXml(String bloodType, int units) {
        return """
                <hos:RequestBloodUnitsRequest
                    xmlns:hos="http://bloodlink.tn/hospital">
                    <hos:hospitalName>Charles Nicolle</hos:hospitalName>
                    <hos:bloodType>%s</hos:bloodType>
                    <hos:units>%d</hos:units>
                </hos:RequestBloodUnitsRequest>
                """.formatted(bloodType, units);
    }

    @Test
    void shouldApproveValidSoapRequest() throws Exception {

        client.sendRequest(
                withPayload(new StringSource(requestXml("O-", 5)))
        )
        .andExpect(noFault())
        .andExpect(xpath(
                "//hos:status",
                Map.of("hos", NAMESPACE)
        ).evaluatesTo("APPROVED"));
    }

    @Test
    void shouldReturnClientFaultFor99Units() throws Exception {

        client.sendRequest(
                withPayload(new StringSource(requestXml("O-", 99)))
        )
        .andExpect(clientOrSenderFault());
    }

    @Test
    void shouldReturnFaultForUnknownRequestId() throws Exception {

        String xml = """
                <hos:CheckRequestStatusRequest
                    xmlns:hos="http://bloodlink.tn/hospital">
                    <hos:requestId>999</hos:requestId>
                </hos:CheckRequestStatusRequest>
                """;

        client.sendRequest(
                withPayload(new StringSource(xml))
        )
        .andExpect(clientOrSenderFault());
    }
}
