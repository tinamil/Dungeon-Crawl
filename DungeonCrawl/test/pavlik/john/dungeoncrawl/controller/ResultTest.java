package pavlik.john.dungeoncrawl.controller;

import pavlik.john.dungeoncrawl.controller.Result;
import junit.framework.TestCase;

/**
 *
 * @author John
 *
 */
public class ResultTest extends TestCase {

  /**
   * Test method for 'pavlik.john.dungeoncrawl.controller.Result.setReason()' and 'getReason()'.
   */
  public void testSetReason() {
    final Result result = Result.SUCCESS;
    result.setReason("reason");
    assertEquals("reason", result.getReason());
  }
}
