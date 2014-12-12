package pavlik.john.dungeoncrawl.model.events;

import java.util.HashSet;
import java.util.Set;

import pavlik.john.dungeoncrawl.model.events.Action;
import pavlik.john.dungeoncrawl.model.events.Conditional;
import pavlik.john.dungeoncrawl.model.events.Event;
import pavlik.john.dungeoncrawl.model.events.State;
import junit.framework.TestCase;

/**
 * @author John Pavlik
 * @see Action
 */
public class StateTest extends TestCase {

  State state;
  Event event;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    final Set<Action> actions = new HashSet<>();
    actions.add(new Action("ActionTag", "Action") {

      /**
       *
       */
      private static final long serialVersionUID = 1L;

      @Override
      public boolean performAction(pavlik.john.dungeoncrawl.model.Character c) {
        state = new State("State2", "State2Description");
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
        return true;
      }
    });
    event = new Event(actions);
    state = new State("State1", "StateDescription");
  }

  /**
   * Test method for pavlik.john.dungeoncrawl.model.events.Action constructor
   */
  public void testConstructor() {
    // SetUp is sufficient
  }

  /**
   * Test method for pavlik.john.dungeoncrawl.model.State#getDescription()
   */
  public void testEquals() {
    assertTrue(state.equals(state));
    assertFalse(state.equals(null));
  }

  /**
   * Test method for pavlik.john.dungeoncrawl.model.State#getDescription()
   */
  public void testGetDescription() {
    assertEquals("StateDescription", state.getDescription());
  }

  /**
   * Test method for pavlik.john.dungeoncrawl.model.State#getName()
   */
  public void testGetName() {
    assertEquals("State1", state.getName());
  }

}
