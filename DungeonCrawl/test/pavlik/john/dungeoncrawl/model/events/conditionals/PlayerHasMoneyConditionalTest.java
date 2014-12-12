package pavlik.john.dungeoncrawl.model.events.conditionals;

import pavlik.john.dungeoncrawl.TestConstants;
import pavlik.john.dungeoncrawl.controller.Controller;
import pavlik.john.dungeoncrawl.model.Player;
import pavlik.john.dungeoncrawl.model.events.Action;
import pavlik.john.dungeoncrawl.model.events.Conditional;
import pavlik.john.dungeoncrawl.model.events.actions.AdjustMoneyAction;
import pavlik.john.dungeoncrawl.model.events.conditionals.PlayerHasMoneyConditional;
import junit.framework.TestCase;

/**
 * @author John Pavlik
 * @see Action
 */
public class PlayerHasMoneyConditionalTest extends TestCase {

  Conditional     conditional;
  Controller wc;
  Player          player;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    wc = new Controller(TestConstants.v3TESTFILE);
    player = wc.getWorld().getPlayer("player");
    conditional = new PlayerHasMoneyConditional(50, "TAG");
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
    assertFalse(conditional.meetsConditions(player));
    final Action action = new AdjustMoneyAction("50", true, "TAG");
    action.performAction(player);
    assertTrue(conditional.meetsConditions(player));
  }
}
