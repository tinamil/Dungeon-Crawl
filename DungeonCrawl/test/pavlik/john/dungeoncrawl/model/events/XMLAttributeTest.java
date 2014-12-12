package pavlik.john.dungeoncrawl.model.events;

import pavlik.john.dungeoncrawl.model.events.XMLAttribute;
import junit.framework.TestCase;

/**
 * @author John Pavlik
 * @see XMLAttribute
 */
public class XMLAttributeTest extends TestCase {

  XMLAttribute xml;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    xml = new XMLAttribute("TAG", "VALUE") {

      /**
       *
       */
      private static final long serialVersionUID = 1L;
    };
  }

  /**
   * Test method for pavlik.john.dungeoncrawl.model.events.XMLAttribute constructor
   */
  public void testConstructor() {
    // Setup is sufficient
  }

  /**
   * Test method for pavlik.john.dungeoncrawl.model.XMLAttribute#getTag()
   */
  public void testGetTag() {
    assertEquals("TAG", xml.getTag());
  }

  /**
   * Test method for pavlik.john.dungeoncrawl.model.XMLAttribute#getValue()
   */
  public void testGetValue() {
    assertEquals("VALUE", xml.getValue());
  }
}
