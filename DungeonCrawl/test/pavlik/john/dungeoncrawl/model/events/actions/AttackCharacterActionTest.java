package pavlik.john.dungeoncrawl.model.events.actions;

import pavlik.john.dungeoncrawl.TestConstants;
import pavlik.john.dungeoncrawl.controller.Controller;
import pavlik.john.dungeoncrawl.model.Navigation;
import pavlik.john.dungeoncrawl.model.Player;
import pavlik.john.dungeoncrawl.model.Universe;
import pavlik.john.dungeoncrawl.model.events.Action;
import pavlik.john.dungeoncrawl.model.events.actions.AttackCharacterAction;
import pavlik.john.dungeoncrawl.model.events.actions.MessageAction;
import junit.framework.TestCase;

/**
 * @author John Pavlik
 * @see MessageAction
 */
public class AttackCharacterActionTest extends TestCase {

  Action action;
  Player player;
  Universe  world;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    final Controller wc = new Controller(TestConstants.v4TESTFILE);
    world = wc.getWorld();
    player = world.getPlayer("Jay");
    wc.travel(player, Navigation.WEST);
    action = new AttackCharacterAction("old man", "player", "TAG");
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
    action.performAction(player);
    assertTrue(world.getNonPlayerCharacter("old man").getCurrentTarget().equals(player));
    assertTrue(world.getNonPlayerCharacter("old man").inCombat());
  }
}
