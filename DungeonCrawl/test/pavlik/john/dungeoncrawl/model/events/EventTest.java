package pavlik.john.dungeoncrawl.model.events;

import java.util.HashSet;
import java.util.Set;

import pavlik.john.dungeoncrawl.model.events.Action;
import pavlik.john.dungeoncrawl.model.events.Conditional;
import pavlik.john.dungeoncrawl.model.events.Event;
import junit.framework.TestCase;

/**
 * @author John Pavlik
 * @see Action
 */
public class EventTest extends TestCase {

  Event  event;
  String message;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    message = null;
    final Set<Action> actions = new HashSet<>();
    actions.add(new Action("ActionTag", "Action") {

      /**
       *
       */
      private static final long serialVersionUID = 1L;

      @Override
      public boolean performAction(pavlik.john.dungeoncrawl.model.Character c) {
        message = "Not null";
        return true;
      }

    });
    final Set<Conditional> conditionals = new HashSet<>();
    conditionals.add(new Conditional("ConditionalTag", "Conditional") {
      /**
       *
       */
      private static final long serialVersionUID = 1L;

      @Override
      public boolean meetsConditions(pavlik.john.dungeoncrawl.model.Character c) {
        return message == null;
      }
    });
    event = new Event(actions);
  }

  /**
   * Test method for pavlik.john.dungeoncrawl.model.events.Action constructor
   */
  public void testConstructor() {
    // SetUp is sufficient
  }

  /**
   * Test method for pavlik.john.dungeoncrawl.model.Event#execute()
   */
  public void testExecute() {
    assertNull(message);
    event.execute(null);
    assertEquals("Not null", message);
  }
}
