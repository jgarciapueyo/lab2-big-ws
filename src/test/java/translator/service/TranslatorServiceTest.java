package translator.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import translator.Application;
import translator.domain.TranslatedText;

import static org.junit.Assert.assertEquals;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class TranslatorServiceTest {

  @Autowired
  TranslatorService translatorService;

  @Test(expected = RuntimeException.class)
  public void translateTest() {
    TranslatedText translatedText = translatorService.translate("en", "es", "This is a test of translation service");
    assertEquals("I don't know how to translate from en to es the text 'This is a test of translation service'", translatedText.getTranslation());

    // This way of testing that the TranslatorService throws a RuntimeException does not check the exception message.
    // To do that, one way would be:

    // try {
    //  TranslatedText translatedText = translatorService.translate("en", "es", "This is a test of translation service");
    //} catch (RuntimeException e) {
    //  assertEquals(e.getMessage(), "I don't know how to translate from en to es the text 'This is a test of translation service'");
    //}

    // PRO: the message of the exception can be checked
    // CONS: with the annotation is more clear and code is more concise
    // There is other ways to check the exception message using libraries like AssertJ (bundled with SpringBoot)
    // See: https://stackoverflow.com/a/58714622
  }

}
