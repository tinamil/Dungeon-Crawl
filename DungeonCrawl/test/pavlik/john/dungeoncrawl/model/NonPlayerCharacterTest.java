package pavlik.john.dungeoncrawl.model;

import java.util.Collection;

import pavlik.john.dungeoncrawl.TestConstants;
import pavlik.john.dungeoncrawl.controller.Controller;
import pavlik.john.dungeoncrawl.exceptions.PersistenceStateException;
import pavlik.john.dungeoncrawl.model.NonPlayerCharacter;
import pavlik.john.dungeoncrawl.model.events.State;
import pavlik.john.dungeoncrawl.view.TextUtilities;
import junit.framework.TestCase;

/**
 * @author John Pavlik
 * @see NonPlayerCharacter
 */
public class NonPlayerCharacterTest extends TestCase {

  private Controller    f_wc;
  private NonPlayerCharacter npc;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    try {
      f_wc = new Controller(TestConstants.v3TESTFILE);
    } catch (final PersistenceStateException e) {
      fail(e.getMessage());
    }
    npc = f_wc.getWorld().getNonPlayerCharacter("old man");
  }

  /**
   * Test method for 'pavlik.john.dungeoncrawl.model.NonPlayerCharacter.getDescription()'
   */
  public void testDescription() {
    assertEquals("Upon close inspection he looks grimy" + TextUtilities.LINESEP + "He looks angry.", npc
        .getDescription());
  }

  /**
   * Test method for 'pavlik.john.dungeoncrawl.model.NonPlayerCharacter.getCurrentStateState()'
   */
  public void testGetCurrentState() {
    final Collection<State> oldManStates = npc.getStates();
    final State current = npc.getCurrentState();
    assertNotNull(current);
    assertTrue(oldManStates.contains(current));
  }

  /**
   * Test method for 'pavlik.john.dungeoncrawl.model.NonPlayerCharacter.getStates()'
   */
  public void testGetStates() {
    final Collection<State> oldManStates = npc.getStates();
    assertNotNull(oldManStates);
    assertFalse(oldManStates.isEmpty());
  }

  /**
   * Test method for 'pavlik.john.dungeoncrawl.model.NonPlayerCharacter.setCurrentState()'
   */
  public void testSetCurrentState() {
    final State previous = npc.getCurrentState();
    npc.setCurrentState(new State("State", "Description") {

      /**
       *
       */
      private static final long serialVersionUID = 1L;

    });
    assertNotSame(previous, npc.getCurrentState());

    try {
      npc.setCurrentState(null);
      fail();
    } catch (final NullPointerException e) {

    }
  }

}
