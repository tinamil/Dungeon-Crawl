package pavlik.john.dungeoncrawl.model.events.actions;

import pavlik.john.dungeoncrawl.TestConstants;
import pavlik.john.dungeoncrawl.controller.Controller;
import pavlik.john.dungeoncrawl.model.NonPlayerCharacter;
import pavlik.john.dungeoncrawl.model.Universe;
import pavlik.john.dungeoncrawl.model.events.Action;
import pavlik.john.dungeoncrawl.model.events.actions.SetNPCStateAction;
import junit.framework.TestCase;

/**
 * @author John Pavlik
 * @see SetNPCStateAction
 */
public class SetNPCStateActionTest extends TestCase {

  Action action;
  Universe  world;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    final Controller wc = new Controller(TestConstants.v3TESTFILE);
    world = wc.getWorld();
    action = new SetNPCStateAction(world, "old man", "helpful", "TAG");
  }

  /**
   * Test method for pavlik.john.dungeoncrawl.model.events.actions.AdjustMoneyAction constructor
   */
  public void testConstructor() {
    // Setup is sufficient
  }

  /**
   * Test method for pavlik.john.dungeoncrawl.model.actions.AdjustMoneyAction#performAction()
   */
  public void testPerformAction() {
    final NonPlayerCharacter npc = world.getNonPlayerCharacter("old man");
    assertEquals("gruff", npc.getCurrentState().getName());
    action.performAction(npc);
    assertEquals("helpful", npc.getCurrentState().getName());
  }
}
