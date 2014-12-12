package pavlik.john.dungeoncrawl.model.events;

import pavlik.john.dungeoncrawl.model.events.Action;
import pavlik.john.dungeoncrawl.model.events.Conditional;
import junit.framework.TestCase;

/**
 * @author John Pavlik
 * @see Action
 */
public class ConditionalTest extends TestCase {

  Conditional conditional;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    conditional = new Conditional("nameTag", "name") {

      /**
       *
       */
      private static final long serialVersionUID = 1L;

      @Override
      public boolean meetsConditions(pavlik.john.dungeoncrawl.model.Character c) {
        return Math.random() > .5;
      }

    };
  }

  /**
   * Test method for pavlik.john.dungeoncrawl.model.events.Action constructor
   */
  public void testConstructor() {
    // Setup is sufficient
  }

  /**
   * Test method for pavlik.john.dungeoncrawl.model.Action#performAction()
   */
  public void testMeetsConditions() {
    conditional.meetsConditions(null);
    // Not throwing an exception is enough to complete the test
  }
}
