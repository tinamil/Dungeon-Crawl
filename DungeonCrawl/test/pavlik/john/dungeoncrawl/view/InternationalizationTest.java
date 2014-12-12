package pavlik.john.dungeoncrawl.view;

import java.util.Locale;
import java.util.ResourceBundle;

import pavlik.john.dungeoncrawl.properties.Messages;
import junit.framework.TestCase;

/**
 * Test cases for internationalization of messages. The messages are only in English, but this
 * allows them to be loaded dynamically instead of being hard coded into the application.
 *
 * @author John
 *
 */
public class InternationalizationTest extends TestCase {

  private ResourceBundle f_messages;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    f_messages = ResourceBundle.getBundle("pavlik/john/dungeoncrawl/properties/MessagesBundle", new Locale("en",
        "US"));
  }

  /**
   * Test to make sure the files loaded were from the en_US resource bundle as expected
   */
  public void testMessage() {
    assertEquals("Welcome to Dungeon Crawl!", f_messages.getString(Messages.TITLE));
  }

  /**
   * Test to make sure the files were setUp correctly and loaded from the classpath.
   */
  public void testResourceLoading() {
    assertNotNull(f_messages);
  }
}
