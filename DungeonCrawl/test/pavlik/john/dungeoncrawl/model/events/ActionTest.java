package pavlik.john.dungeoncrawl.model.events;

import pavlik.john.dungeoncrawl.model.events.Action;
import junit.framework.TestCase;

/**
 * @author John Pavlik
 * @see Action
 */
public class ActionTest extends TestCase {

  Action action;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    action = new Action("nameTag", "name") {

      /**
       *
       */
      private static final long serialVersionUID = 1L;

      @Override
      public boolean performAction(pavlik.john.dungeoncrawl.model.Character c) {
        // Execute
        return true;
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
  public void testPerformAction() {
    assertTrue(action.performAction(null));
  }
}
