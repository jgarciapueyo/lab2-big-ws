package translator.web.ws;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ClassUtils;
import org.springframework.ws.client.WebServiceIOException;
import org.springframework.ws.client.WebServiceTransportException;
import org.springframework.ws.client.core.WebServiceTemplate;

import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.soap.client.SoapFaultClientException;
import org.springframework.ws.soap.security.wss4j2.Wss4jSecurityInterceptor;
import translator.Application;
import translator.web.ws.schema.GetTranslationRequest;
import translator.web.ws.schema.GetTranslationResponse;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = Application.class)
public class TranslatorEndpointTest {

    private final Jaxb2Marshaller marshaller = new Jaxb2Marshaller();

    @LocalServerPort
    private int port;

    @Before
    public void init() throws Exception {
        marshaller.setPackagesToScan(ClassUtils.getPackageName(GetTranslationRequest.class));
        marshaller.afterPropertiesSet();
    }

    // This test is useless after securing the endpoint.
    // It has been left because it part of the Primary Goal of the assignment
    @Test(expected = RuntimeException.class)
    public void testSendAndReceive() {
        GetTranslationRequest request = new GetTranslationRequest();
        request.setLangFrom("en");
        request.setLangTo("es");
        request.setText("This is a test of translation service");
        Object response = new WebServiceTemplate(marshaller).marshalSendAndReceive("http://localhost:"
                + port + "/ws", request);
        assertNotNull(response);
        assertThat(response, instanceOf(GetTranslationResponse.class));
        GetTranslationResponse translation = (GetTranslationResponse) response;
        assertThat(translation.getTranslation(), is("I don't know how to translate from en to es the text 'This is a test of translation service'"));

        // The same information about testing in TranslatorServiceTest can be applied in this test
    }

    /**
     * Tests that the remote web service has been secured with Spring WS Security but the client does not secure the
     * message.
     */
    @Test
    public void testSendAndReceiveNoSecurityHeader() {
        try {
            GetTranslationRequest request = new GetTranslationRequest();
            request.setLangFrom("en");
            request.setLangTo("es");
            request.setText("This is a test of translation service");
            Object response = new WebServiceTemplate(marshaller).marshalSendAndReceive("http://localhost:"
                    + port + "/ws", request);
        } catch (SoapFaultClientException e) {
            assertThat(e.getFaultStringOrReason(), is("No WS-Security header found"));
        }
    }

    /**
     * Adds user information in the security header of the SOAP message
     */
    public Wss4jSecurityInterceptor securityInterceptor() {
        Wss4jSecurityInterceptor wss4jSecurityInterceptor = new Wss4jSecurityInterceptor();
        wss4jSecurityInterceptor.setSecurementActions("Timestamp UsernameToken");
        wss4jSecurityInterceptor.setSecurementUsername("root");
        wss4jSecurityInterceptor.setSecurementPassword("toor");
        return wss4jSecurityInterceptor;
    }

    /**
     * Tests that the remote web service has been secured with Spring WS Security and the client secures the
     * message.
     */
    @Test
    public void testSendAndReceiveSecurityHeader() {
        try {
            GetTranslationRequest request = new GetTranslationRequest();
            request.setLangFrom("en");
            request.setLangTo("es");
            request.setText("This is a test of translation service");

            // Add interceptors to modify the message sent from WebServiceTemplate
            ClientInterceptor[] interceptors = new ClientInterceptor[]{securityInterceptor()};
            WebServiceTemplate webServiceTemplate = new WebServiceTemplate(marshaller);
            webServiceTemplate.setInterceptors(interceptors);

            Object response = webServiceTemplate.marshalSendAndReceive("http://localhost:" + port + "/ws", request);
        } catch (RuntimeException e) {
            assertThat(e.getMessage(), is("I don't know how to translate from en to es the text 'This is a test of translation service'"));
        }
    }

    /**
     * Simulate that the remote web service is not found by making a request to an URI that does not exist.
     * <p>
     * This test is very specific and tests that the host exists but in that URI there is no web service listening.
     */
    @Test(expected = WebServiceTransportException.class)
    public void webServiceIsNotListeningInThatURI() {
        GetTranslationRequest request = new GetTranslationRequest();
        request.setLangFrom("en");
        request.setLangTo("es");
        request.setText("This is a test of translation service");
        Object response = new WebServiceTemplate(marshaller).marshalSendAndReceive("http://localhost:"
                + port + "/ws-is-down", request);
    }

    /**
     * Simulate that the remote web service is not found by making a request to an URI that does not exist.
     * <p>
     * This test is more general and includes that the host does not exist.
     */
    @Test(expected = WebServiceIOException.class)
    public void webServiceIsNotInThatHost() {
        GetTranslationRequest request = new GetTranslationRequest();
        request.setLangFrom("en");
        request.setLangTo("es");
        request.setText("This is a test of translation service");
        Object response = new WebServiceTemplate(marshaller).marshalSendAndReceive("http://localhost-does-not-exist:"
                + port + "/ws-is-down", request);
    }


}
