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
}
