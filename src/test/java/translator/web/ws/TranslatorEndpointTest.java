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
   * Simulate that the remote web service is not found by making a request to an URI that does not exist.
   *
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
   *
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
