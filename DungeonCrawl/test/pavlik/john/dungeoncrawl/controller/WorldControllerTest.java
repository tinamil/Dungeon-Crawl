package pavlik.john.dungeoncrawl.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;

import pavlik.john.dungeoncrawl.TestConstants;
import pavlik.john.dungeoncrawl.controller.Result;
import pavlik.john.dungeoncrawl.controller.Controller;
import pavlik.john.dungeoncrawl.exceptions.PersistenceStateException;
import pavlik.john.dungeoncrawl.model.Navigation;
import pavlik.john.dungeoncrawl.model.Player;
import pavlik.john.dungeoncrawl.model.Universe;
import pavlik.john.dungeoncrawl.model.Item.Action;
import pavlik.john.dungeoncrawl.model.events.triggers.SayTrigger;
import pavlik.john.dungeoncrawl.view.TextUtilities;
import junit.framework.TestCase;

/**
 * @author T.J. Halloran
 * @author Brian Woolley
 */
public class WorldControllerTest extends TestCase {

  private Controller f_wc;
  private Player          player, noCombatPlayer;
  ResourceBundle          messages;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    messages = ResourceBundle
        .getBundle("pavlik/john/dungeoncrawl/properties/MessagesBundle", new Locale("en", "US"));
    try {
      f_wc = new Controller(TestConstants.v4TESTFILE);
      player = f_wc.getWorld().getPlayer("player");
      noCombatPlayer = f_wc.getWorld().getPlayer("chris");
    } catch (final PersistenceStateException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  /**
   * Test method for 'pavlik.john.dungeoncrawl.controller.Controller.attack(Player,
   * command)'
   */
  public void testAttack() {
    assertEquals(Result.SUCCESS, f_wc.attack(player, "space marine"));
    assertEquals(Result.CHARACTER_NOT_FOUND, f_wc.attack(player, "who?"));
    assertEquals(Result.CANT_USE_ITEM, f_wc.attack(player, "chris"));
    assertEquals(Result.ALREADY_IN_COMBAT, f_wc.attack(player, "space marine"));
    assertEquals(Result.CHARACTER_NOT_FOUND, f_wc.attack(player, "old man"));
    f_wc.getWorld().getPlayer("space marine").setUnconscious(player);
    assertEquals(Result.UNCONSCIOUS_TARGET, f_wc.attack(player, "space marine"));
  }

  /**
   * Test method for 'pavlik.john.dungeoncrawl.controller.Controller.canTravel(Player,
   * Navigation)'
   */
  public void testCanTravel() {
    assertTrue(f_wc.canTravel(player, Navigation.WEST));
    assertFalse(f_wc.canTravel(player, Navigation.NORTH));
    assertFalse(f_wc.canTravel(player, Navigation.EAST));
  }

  /**
   * Test method for 'pavlik.john.dungeoncrawl.controller.Controller.equip(Player player,
   * String weaponString)'
   */
  public void testEquip() {
    assertNotSame(player.getCurrentWeapon(), player.getCharacterClass().getDefaultWeapon());
    assertEquals(Result.SUCCESS, f_wc.equip(player, null));
    assertEquals(player.getCurrentWeapon(), player.getCharacterClass().getDefaultWeapon());
    assertEquals(Result.SUCCESS, f_wc.equip(player, "basic dagger"));
    assertEquals(Result.ITEM_NOT_FOUND, f_wc.equip(player, "old man's dagger"));
    assertEquals(Result.CANT_USE_ITEM, f_wc.equip(noCombatPlayer, "ground dagger"));
    assertEquals(Result.CANT_USE_ITEM, f_wc.equip(player, "laser rifle"));
    try {
      setUp();
    } catch (final Exception e) {
      fail(e.getMessage());
    }
    assertEquals(Result.SUCCESS, f_wc.equip(player, "ground dagger"));
  }

  /**
   * Test method for 'pavlik.john.dungeoncrawl.controller.Controller.getAvailablePlayers()
   */
  public void testGetAvailablePlayers() {
    final String display = f_wc.getAvailablePlayers();
    assertEquals("1. Chris\r\n2. Jay\r\n3. player\r\n4. A Space Marine\r\n".replace("\r\n", TextUtilities.LINESEP),
        display);
  }

  /**
   * Test method for 'pavlik.john.dungeoncrawl.controller.Controller.getItemForPlayer(Player
   * player, String itemName)'
   */
  public void testGetItemForPlayer() {
    assertEquals(f_wc.getWorld().getItem("rock"), f_wc.getItemForPlayer(player, "rock", false).get());
    assertEquals(Optional.empty(), f_wc.getItemForPlayer(player, "penny", false));
    assertEquals(f_wc.getWorld().getItem("basic dagger"), f_wc.getItemForPlayer(player, "basic dagger", false).get());
    assertEquals(f_wc.getWorld().getItem("ground dagger"), f_wc.getItemForPlayer(player, "ground dagger", true).get());
    assertTrue(player.getContainer().isPresent(f_wc.getWorld().getItem("ground dagger")));
  }

  /**
   * Test method for 'pavlik.john.dungeoncrawl.controller.Controller.getWorld()'
   */
  public void testGetWorld() {
    assertNotNull(f_wc.getWorld());
  }

  /**
   * Test method for 'pavlik.john.dungeoncrawl.controller.Controller.giveItem'
   */
  public void testGiveItem() {
    assertEquals(Result.SUCCESS, f_wc.giveItem("basic dagger", player, "chris"));
    assertEquals(Result.ITEM_NOT_FOUND, f_wc.giveItem("basic dagger", player, "chris"));
    assertEquals(Result.ITEM_NOT_FOUND, f_wc.giveItem("rock", player, "chris"));
    assertEquals(Result.CHARACTER_NOT_FOUND, f_wc.giveItem("ground dagger", player, "old man"));
  }

  /**
   * Test method for 'pavlik.john.dungeoncrawl.controller.Controller.giveMoney(String,
   * Player, Player)'
   */
  public void testGiveMoney() {
    assertEquals(Result.SUCCESS, f_wc.giveMoney("20", player, "chris"));
    assertEquals(Result.INSUFFICIENT_MONEY, f_wc.giveMoney("35", player, "chris"));
    assertEquals(Result.INSUFFICIENT_MONEY, f_wc.giveMoney("0", player, "chris"));
    assertEquals(Result.INSUFFICIENT_MONEY, f_wc.giveMoney("-5", player, "chris"));
    assertEquals(Result.CHARACTER_NOT_FOUND, f_wc.giveMoney("1", player, "old man"));
    assertEquals(Result.CHARACTER_NOT_FOUND, f_wc.giveMoney("1", player, "no one"));
  }

  /**
   * Test method for 'pavlik.john.dungeoncrawl.controller.Controller.isMissingItems(Player,
   * Navigation)'
   */
  public void testIsMissingItems() {
    assertTrue(f_wc.isMissingItems(player, Navigation.EAST));
    assertFalse(f_wc.isMissingItems(player, Navigation.WEST));
    assertFalse(f_wc.isMissingItems(noCombatPlayer, Navigation.EAST));
    assertFalse(f_wc.isMissingItems(player, Navigation.NORTH));
  }

  /**
   * Test method for 'pavlik.john.dungeoncrawl.controller.Controller.loadWorld(String file)'
   */
  public void testLoadWorld() {
    try {
      final Universe world = f_wc.getWorld();
      f_wc.loadWorld(TestConstants.v4TESTFILE);
      assertNotSame(world, f_wc.getWorld());
    } catch (final PersistenceStateException e) {
      fail(e.getMessage());
    }
  }

  /**
   * Test method for 'pavlik.john.dungeoncrawl.controller.Controller.playAs(String name)'
   */
  public void testPlayAs() {
    assertEquals(player, f_wc.playAs("player"));
    assertNull(f_wc.playAs("player"));
    assertEquals(noCombatPlayer, f_wc.playAs("1"));
  }

  /**
   * Test method for
   * 'pavlik.john.dungeoncrawl.controller.Controller.putItemInItemContainer(Player, String
   * item, String container)'
   */
  public void testPutItemInItemContainer() {
    assertTrue(f_wc.putItemInItemContainer(player, "basic dagger", "chest"));
    assertTrue(f_wc.putItemInItemContainer(player, "ground dagger", "chest"));
    assertFalse(f_wc.putItemInItemContainer(player, "sack of trinkets", "rock"));
  }

  /**
   * Test method for 'pavlik.john.dungeoncrawl.controller.Controller.releasePlayer(Player)'
   */
  public void testReleasePlayer() {
    assertFalse(player.isOccupied());
    f_wc.playAs("player");
    assertTrue(player.isOccupied());
    f_wc.releasePlayer(player);
    assertFalse(player.isOccupied());
  }

  /**
   * Test method for 'pavlik.john.dungeoncrawl.controller.Controller.stopWorld()'
   */
  public void testStopWorld() {
    f_wc.stopWorld();
    final long tick = f_wc.getWorld().getCurrentTick();
    try {
      Thread.sleep(250);
    } catch (final InterruptedException e) {
      e.printStackTrace();
    }
    assertEquals(tick, f_wc.getWorld().getCurrentTick());
  }

  /**
   * Test method for 'pavlik.john.dungeoncrawl.controller.Controller.swapItem(Player,
   * Container dest, Container source, String name, Action)'
   */
  public void testSwapItem() {
    assertEquals(Result.SUCCESS, f_wc.swapItem(player, player.getLocation().getContainer(), player.getContainer(),
        "basic dagger", Action.DROP));
    assertEquals(Result.SUCCESS, f_wc.swapItem(player, player.getContainer(), player.getLocation().getContainer(),
        "basic dagger", Action.TAKE));
    assertEquals(Result.ITEM_NOT_TAKEABLE, f_wc.swapItem(player, player.getContainer(), player.getLocation()
        .getContainer(), "rock", Action.TAKE));
    assertEquals(Result.ITEM_NOT_FOUND, f_wc.swapItem(player, player.getContainer(), player.getLocation()
        .getContainer(), "penny", Action.TAKE));

    assertEquals(Result.SUCCESS, f_wc.swapItem(player, player.getContainer(), player.getLocation().getContainer(),
        "ground dagger", Action.TAKE));
    assertTrue(5L == player.getScore());
    assertEquals(Result.SUCCESS, f_wc.swapItem(player, player.getLocation().getContainer(), player.getContainer(),
        "ground dagger", Action.DROP));
    assertTrue(15L == player.getScore());
  }

  /**
   * Test method for 'pavlik.john.dungeoncrawl.controller.Controller.synthesizeItems(Player,
   * Set of String)'
   */
  public void testSynthesizeItems() {
    final Set<String> itemSet = new HashSet<>();
    itemSet.add("basic dagger");
    itemSet.add("ground dagger");
    assertFalse(f_wc.getItemForPlayer(player, "Adamantium dagger", false).isPresent());
    assertEquals(Result.SUCCESS, f_wc.synthesizeItems(player, itemSet));
    assertTrue(f_wc.getItemForPlayer(player, "Adamantium dagger", false).isPresent());
    assertEquals(Result.ITEM_NOT_FOUND, f_wc.synthesizeItems(player, itemSet));
    assertEquals("basic dagger", f_wc.synthesizeItems(player, itemSet).getReason());
    itemSet.clear();
    itemSet.add("Sack of trinkets");
    assertEquals(Result.NO_ITEM_CREATED, f_wc.synthesizeItems(player, itemSet));
  }

  /**
   * Test method for
   * 'pavlik.john.dungeoncrawl.controller.Controller.takeItemFromItemContainer(Player,
   * String item, String container)'
   */
  public void testTakeItemFromItemContainer() {
    assertFalse(f_wc.takeItemFromItemContainer(player, "basic dagger", "chest"));
    assertTrue(f_wc.takeItemFromItemContainer(player, "stored item", "chest"));
    assertFalse(f_wc.takeItemFromItemContainer(player, "sack of trinkets", "rock"));
  }

  /**
   * Test method for 'pavlik.john.dungeoncrawl.controller.Controller.talkToNPC(Player,
   * NonPlayerCharacter)'
   */
  public void testTalkToNPC() {
    final List<SayTrigger> triggers = f_wc.talkToNPC(player, f_wc.getWorld().getNonPlayerCharacter("old man"));
    assertFalse(triggers.isEmpty());
  }

  /**
   * Test method for 'pavlik.john.dungeoncrawl.controller.Controller.tick(long)'
   */
  public void testTick() {
    f_wc.tick(5);
    assertTrue(5 == f_wc.getWorld().getCurrentTick());
    f_wc.tick(10);
    assertTrue(10 == f_wc.getWorld().getCurrentTick());
  }

  /**
   * Test method for 'pavlik.john.dungeoncrawl.controller.Controller.travel(Navigation)'
   */
  public void testTravel() {
    final Universe w = f_wc.getWorld();
    final Player p = player;
    assertEquals(w.getPlace("Road"), p.getLocation());
    f_wc.travel(p, Navigation.NORTH);
    assertEquals(w.getPlace("Road"), p.getLocation());
    f_wc.travel(p, Navigation.WEST);
    assertEquals(w.getPlace("Hill"), p.getLocation());
    f_wc.travel(p, Navigation.EAST);
    assertEquals(w.getPlace("Road"), p.getLocation());
    f_wc.travel(p, Navigation.EAST);
    assertEquals(w.getPlace("Road"), p.getLocation());
    p.getContainer().addItem(w.getItem("key"));
    f_wc.travel(p, Navigation.EAST);
    assertEquals(w.getPlace("Building"), p.getLocation());
    assertTrue(w.getCharacter("building guard").inCombat());
    f_wc.attack(player, "building guard");
    assertTrue(player.inCombat());
    f_wc.travel(p, Navigation.WEST);
    assertFalse(player.inCombat());
    f_wc.travel(p, Navigation.SOUTH);
    assertEquals(w.getPlace("Valley"), p.getLocation());
    try {
      f_wc.travel(p, null); // should throw nullpointerexception
      fail();
    } catch (final NullPointerException e) {
    }
    assertTrue(w.isGameWon());
  }

  /**
   * Test method for 'pavlik.john.dungeoncrawl.controller.Controller.useItem(Player, String
   * item, String target)
   */
  public void testUseItem() {
    assertEquals(Result.CHARACTER_NOT_FOUND, f_wc.useItem(player, "healing potion", "old man"));
    assertEquals(Result.CHARACTER_NOT_FOUND, f_wc.useItem(player, "healing potion", "nobody"));
    assertEquals(Result.ITEM_NOT_FOUND, f_wc.useItem(player, "no item", "jay"));
    assertEquals(Result.ALREADY_IN_COMBAT, f_wc.useItem(player, "healing potion", "chris"));
    f_wc.tick(5);
    assertEquals(Result.SUCCESS, f_wc.useItem(player, "healing potion", "chris"));
    assertEquals(Result.ALREADY_IN_COMBAT, f_wc.useItem(player, "healing potion", "chris"));
    f_wc.tick(10);
    assertEquals(Result.SUCCESS, f_wc.useItem(player, "healing potion", "jay"));
    f_wc.tick(15);
    assertEquals(Result.ITEM_CONSUMED, f_wc.useItem(player, "healing potion", "player"));
    assertEquals(Result.SUCCESS, f_wc.useItem(player, "basic dagger", "jay"));
    f_wc.swapItem(player, player.getContainer(), player.getLocation().getContainer(), "sack of trinkets", Action.TAKE);
    assertEquals(Result.CANT_USE_ITEM, f_wc.useItem(player, "Sack of Trinkets", "player"));
  }

  /**
   * Test method for 'pavlik.john.dungeoncrawl.controller.Controller.WorldController()'
   */
  public void testWorldController() {
    try {
      f_wc = new Controller();
    } catch (final PersistenceStateException e) {
      fail(e.getMessage());
    }
    assertNotNull(f_wc.getWorld());
  }

  /**
   * Test method for
   * 'pavlik.john.dungeoncrawl.controller.Controller.WorldController(String)'
   */
  public void testWorldControllerString() {
    // Just getting through setUp() is enough!
  }

}
