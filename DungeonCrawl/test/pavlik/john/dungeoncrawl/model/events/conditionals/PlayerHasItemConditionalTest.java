package pavlik.john.dungeoncrawl.model.events.conditionals;

import pavlik.john.dungeoncrawl.TestConstants;
import pavlik.john.dungeoncrawl.controller.Controller;
import pavlik.john.dungeoncrawl.model.Navigation;
import pavlik.john.dungeoncrawl.model.Player;
import pavlik.john.dungeoncrawl.model.events.Action;
import pavlik.john.dungeoncrawl.model.events.Conditional;
import pavlik.john.dungeoncrawl.model.events.actions.TakeItemAction;
import pavlik.john.dungeoncrawl.model.events.conditionals.PlayerHasItemConditional;
import junit.framework.TestCase;

/**
 * @author John Pavlik
 * @see Action
 */
public class PlayerHasItemConditionalTest extends TestCase {

  Conditional     conditional;
  Controller wc;
  Player          player;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    wc = new Controller(TestConstants.v3TESTFILE);
    player = wc.getWorld().getPlayer("player");
    conditional = new PlayerHasItemConditional("jacket", "tag");
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
    wc.travel(player, Navigation.WEST);
    final Action action = new TakeItemAction("old man", "jacket", "TAG");
    action.performAction(player);
    assertTrue(conditional.meetsConditions(player));
  }
}
