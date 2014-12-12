package pavlik.john.dungeoncrawl.model.events.actions;

import pavlik.john.dungeoncrawl.TestConstants;
import pavlik.john.dungeoncrawl.controller.Controller;
import pavlik.john.dungeoncrawl.model.Navigation;
import pavlik.john.dungeoncrawl.model.Player;
import pavlik.john.dungeoncrawl.model.events.actions.GiveItemAction;
import pavlik.john.dungeoncrawl.model.events.actions.TakeItemAction;
import junit.framework.TestCase;

/**
 * @author John Pavlik
 * @see GiveItemAction
 */
public class TakeItemActionTest extends TestCase {

  TakeItemAction  actionTake;
  Player          player;
  Controller wc;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    wc = new Controller(TestConstants.v3TESTFILE);
    player = wc.getWorld().getPlayer("player");
    wc.travel(player, Navigation.WEST);
    actionTake = new TakeItemAction("Old Man", "jacket", "GiveItem");
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
    assertFalse(player.getContainer().isPresent(wc.getWorld().getItem("jacket")));
    assertTrue(actionTake.performAction(player));
    assertTrue(player.getContainer().isPresent(wc.getWorld().getItem("jacket")));
  }
}
