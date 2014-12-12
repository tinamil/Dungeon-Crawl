package pavlik.john.dungeoncrawl.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.ResourceBundle;

import pavlik.john.dungeoncrawl.model.Character;
import pavlik.john.dungeoncrawl.model.CharacterClass;
import pavlik.john.dungeoncrawl.model.Consumable;
import pavlik.john.dungeoncrawl.model.Container;
import pavlik.john.dungeoncrawl.model.Item;
import pavlik.john.dungeoncrawl.model.Place;
import pavlik.john.dungeoncrawl.model.Weapon;
import pavlik.john.dungeoncrawl.model.Universe;
import pavlik.john.dungeoncrawl.model.Consumable.Effect;
import pavlik.john.dungeoncrawl.model.Consumable.EffectTarget;
import pavlik.john.dungeoncrawl.model.Consumable.EffectType;
import pavlik.john.dungeoncrawl.properties.Messages;
import pavlik.john.dungeoncrawl.view.TextUtilities;
import junit.framework.TestCase;

/**
 * @author John Pavlik
 * @see Character
 */
public class CharacterTest extends TestCase {

  private Universe          f_world;

  private Character      f_player, f_player2, f_player3;
  private CharacterClass cClass;
  private ResourceBundle f_messages;

  Weapon     weapon = new Weapon("new sword", "", "", 0, 0, new HashMap<>(), new HashMap<>(), null, "sword",
      new Effect(1, 5), 5, "swing", 100, EffectTarget.SINGLE, null, null);

  Weapon     cursed = new Weapon("cursed sword", "", "", 0, 0, new HashMap<>(), new HashMap<>(), null, "sword",
      new Effect(1, 5), 5, "swing", 100, EffectTarget.SELF, null, null);

  Consumable potion = new Consumable("healing potion", "", "", 0, 0, new HashMap<>(), true, "", new HashMap<>(), null,
      new Effect(5, 5), EffectType.HEALING, 2, 1, "potion", "potion", 100, EffectTarget.PLACE, null,
      null);

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    f_messages = Messages.loadMessages("en", "us");
    f_world = new Universe();
    f_world.createPlace("city of Dayton", "the", "You are in the glamorous mid-west city of Dayton", false, null);
    f_player = new Character(f_world, "Name", "A", "Boring", new Container(), f_world.getPlace("city of Dayton"),
        f_world.getPlace("city of Dayton")) {

      /**
       *
       */
      private static final long serialVersionUID = 1L;

    };
    f_player2 = new Character(f_world, "Name2", "The", "Boring", new Container(), f_world.getPlace("city of Dayton"),
        null) {

      /**
       *
       */
      private static final long serialVersionUID = 1L;

    };
    f_player3 = new Character(f_world, "Name3", "", "Boring", new Container(), f_world.getPlace("city of Dayton"), null) {

      /**
       *
       */
      private static final long serialVersionUID = 1L;

    };

    cClass = new CharacterClass("Class", 1, 1, new ArrayList<>(), "", 0, 5, 5, 100, 0, EffectTarget.SINGLE, null, null,
        "weapon");
    f_player.setCharacterClass(cClass);
    f_player3.setCharacterClass(cClass);
  }

  /**
   * Test method for 'pavlik.john.dungeoncrawl.model.Character.changeMoney()'
   */
  public void testChangeMoney() {
    int money = f_player.getMoney();
    f_player.changeMoney(50);
    money += 50;
    assertEquals(money, f_player.getMoney());
    f_player.changeMoney(0);
    assertEquals(money, f_player.getMoney());
    f_player.changeMoney(-50);
    money -= 50;
    assertEquals(money, f_player.getMoney());
  }

  /**
   * Test method for pavlik.john.dungeoncrawl.model.Character constructor
   */
  public void testCharacter() {
    try {
      new Character(null, "Name", "A", "Boring", new Container(), f_world.getPlace("city of Dayton"), null) {

        /**
         *
         */
        private static final long serialVersionUID = 1L;
      };
      fail();
    } catch (final NullPointerException e) {
    }
    try {
      new Character(f_world, "Name", null, "Boring", new Container(), f_world.getPlace("city of Dayton"), null) {

        /**
         *
         */
        private static final long serialVersionUID = 1L;
      };
      fail();
    } catch (final NullPointerException e) {
    }
    try {
      new Character(f_world, "Name", "A", null, new Container(), f_world.getPlace("city of Dayton"), null) {

        /**
         *
         */
        private static final long serialVersionUID = 1L;
      };
      fail();
    } catch (final NullPointerException e) {
    }
    try {
      new Character(f_world, "Name", "A", "Boring", null, f_world.getPlace("city of Dayton"), null) {

        /**
         *
         */
        private static final long serialVersionUID = 1L;
      };
      fail();
    } catch (final NullPointerException e) {
    }
    try {
      new Character(f_world, "Name", "A", "Boring", new Container(), null, null) {

        /**
         *
         */
        private static final long serialVersionUID = 1L;
      };
      fail();
    } catch (final NullPointerException e) {
    }
  }

  /**
   * Test method for 'pavlik.john.dungeoncrawl.model.Character.disengageCombat()'
   */
  public void testDisengageCombat() {
    f_player2.startAttack(f_player);
    assertTrue(f_player2.inCombat());
    f_player2.disengageCombat();
    assertFalse(f_player2.inCombat());
  }

  /**
   * Test method for 'pavlik.john.dungeoncrawl.model.Character.equals(Object)'
   */
  public void testEquals() {
    assertFalse(f_player.equals(f_player2));
    assertFalse(f_player.equals(null));
    assertTrue(f_player.equals(f_player));
  }

  /**
   * Test method for 'pavlik.john.dungeoncrawl.model.Character.getArticle()'
   */
  public void testGetArticle() {
    assertEquals("A", f_player.getArticle());
    assertEquals("The", f_player2.getArticle());
  }

  /**
   * Test method for 'pavlik.john.dungeoncrawl.model.Character.getCharacterClass()'
   */
  public void testGetCharacterClass() {
    assertNull(f_player2.getCharacterClass());
    assertNotSame(cClass, f_player.getCharacterClass());
    assertEquals(cClass.getName(), f_player.getCharacterClass().getName());
  }

  /**
   * Test method for 'pavlik.john.dungeoncrawl.model.Character.getContainer()'
   */
  public void testGetContainer() {
    assertNotNull(f_player.getContainer());
  }

  /**
   * Test method for 'pavlik.john.dungeoncrawl.model.Character.getCurrentHealth()'
   */
  public void testGetCurrentHealth() {
    assertEquals(1, f_player.getCurrentHealth());
    assertEquals(0, f_player2.getCurrentHealth());
  }

  /**
   * Test method for 'pavlik.john.dungeoncrawl.model.Character.getCurrentWeapon()'
   */
  public void testGetCurrentWeapon() {
    assertEquals(f_player.getCharacterClass().getDefaultWeapon(), f_player.getCurrentWeapon());
    assertNotSame(cClass.getDefaultWeapon(), f_player.getCurrentWeapon());
    assertNull(f_player2.getCurrentWeapon());
  }

  /**
   * Test method for 'pavlik.john.dungeoncrawl.model.Character.getDescription()'
   */
  public void testGetDescription() {
    assertEquals("Boring", f_player.getDescription());
  }

  /**
   * Test method for 'pavlik.john.dungeoncrawl.model.Character.getFullDescription(ResourceBundle
   * messages)'
   */
  public void testGetFullDescription() {
    final String description = f_player.getFullDescription(f_messages);
    assertEquals(description, "A Name: <|HP:[color=blue]1[/color]|>" + TextUtilities.LINESEP + "Boring"
        + TextUtilities.LINESEP + "A Name's currently equipped weapon is weapon.");
  }

  /**
   * Test method for 'pavlik.john.dungeoncrawl.model.Character.getInventory(ResourceBundle
   * messages)
   */
  public void testGetInventory() {
    assertEquals("You do not have anything.", f_player.getInventory(f_messages));
    f_player.getContainer().addItem(
        new Item("item", "article", "desc", 0, 0, new HashMap<>(), true, "", new HashMap<>(), null));
    assertEquals(f_player.getInventory(f_messages), "You have 0 gold pieces." + TextUtilities.LINESEP
        + "You have article item." + TextUtilities.LINESEP + "A Name's currently equipped weapon is weapon.");
  }

  /**
   * Test method for 'pavlik.john.dungeoncrawl.model.Character.getLocation()'
   */
  public void testGetLocation() {
    final Place location = f_world.getPlace("city of Dayton");
    assertEquals(location, f_player.getLocation());
  }

  /**
   * Test method for 'pavlik.john.dungeoncrawl.model.Character.getMoney()'
   */
  public void testGetMoney() {
    assertEquals(0, f_player.getMoney());
    f_player.changeMoney(50);
    assertEquals(50, f_player.getMoney());
  }

  /**
   * Test method for 'pavlik.john.dungeoncrawl.model.Character.getName()'
   */
  public void testGetName() {
    assertEquals("Name", f_player.getName());
  }

  /**
   * Test method for 'pavlik.john.dungeoncrawl.model.Character.getRespawnLocation()'
   */
  public void testGetRespawnLocation() {
    assertNull(f_player2.getRespawnLocation());
    assertEquals(f_world.getPlace("City of Dayton"), f_player.getRespawnLocation());
  }

  /**
   * Test method for 'pavlik.john.dungeoncrawl.model.Character.getShortDescription()'
   */
  public void testGetShortDescription() {
    assertEquals("A Name", f_player.getShortDescription());
    assertEquals("Name3", f_player3.getShortDescription());
  }

  /**
   * Test method for 'pavlik.john.dungeoncrawl.model.Character.inCombat()'
   */
  public void testInCombat() {
    assertFalse(f_player.inCombat());
    f_player.startAttack(f_player2);
    assertTrue(f_player.inCombat());
  }

  /**
   * Test method for 'pavlik.john.dungeoncrawl.model.Character.isConscious()'
   */
  public void testIsConcscious() {
    assertTrue(f_player.isConscious());
    f_player.changeHealth(f_player2, -5);
    assertFalse(f_player.isConscious());
  }
  /**
   * Test method for 'pavlik.john.dungeoncrawl.model.Character.sameLocationAs()'
   */
  public void testSameLocationAs() {
    assertTrue(f_player.sameLocationAs(f_player));
    assertTrue(f_player.sameLocationAs(f_player2));
    f_player3.setLocation(f_world.getNowherePlace());
    assertFalse(f_player.sameLocationAs(f_player3));
  }
  /**
   * Test method for 'pavlik.john.dungeoncrawl.model.Character.setCharacterClass()'
   */
  public void testSetCharacterClass() {
    assertEquals(cClass.getName(), f_player.getCharacterClass().getName());
    try {
      f_player.setCharacterClass(null);
      fail();
    } catch (final NullPointerException e) {

    }
  }

  /**
   * Test method for 'pavlik.john.dungeoncrawl.model.Character.setCurrentWeapon()'
   */
  public void testSetCurrentWeapon() {
    f_player.setCurrentWeapon(weapon);
    assertEquals(weapon, f_player.getCurrentWeapon());
    f_player.setCurrentWeapon(null);
    assertEquals(f_player.getCharacterClass().getDefaultWeapon(), f_player.getCurrentWeapon());
  }

  /**
   * Test method for 'pavlik.john.dungeoncrawl.model.Character.setLocation(Place)'
   */
  public void testSetLocation() {
    f_player.setLocation(f_world.getNowherePlace());
    assertEquals(f_world.getNowherePlace(), f_player.getLocation());

    try {
      f_player.setLocation(null);
      fail();
    } catch (final NullPointerException e) {
    }
  }

  /**
   * Test method for 'pavlik.john.dungeoncrawl.model.Character.setUnconscious(Character)'
   */
  public void testSetUnconscious() {
    f_player.getContainer().addItem(weapon);
    f_player.setCurrentWeapon(weapon);
    f_player.changeMoney(5);
    assertTrue(f_player.isConscious());
    f_player.setUnconscious(f_player2);
    assertFalse(f_player.isConscious());
    assertFalse(f_player.getContainer().isPresent(weapon));
    assertNotSame(weapon, f_player.getCurrentWeapon());
    assertEquals(0, f_player.getMoney());
    assertEquals(5, f_player2.getMoney());
  }

  /**
   * Test method for 'pavlik.john.dungeoncrawl.model.Character.startAttack(Character)'
   */
  public void testStartAttack() {
    assertFalse(f_player.inCombat());
    f_player.startAttack(f_player2);
    assertTrue(f_player.inCombat());
    assertEquals(f_player2, f_player.getCurrentTarget());
  }

  /**
   * Test method for 'pavlik.john.dungeoncrawl.model.Character.tickAction(long)'
   */
  public void testTickAction() {
    f_player.startAttack(f_player3);
    f_player.tickAction(1);
    assertFalse(f_player.inCombat());
    assertFalse(f_player3.isConscious());
    f_player3.tickAction(5);
    assertTrue(f_player3.isConscious());
    assertFalse(f_player3.getCurrentHealth() > 0);
    f_player3.tickAction(15);
    assertTrue(f_player3.getCurrentHealth() > 0);
  }

  /**
   * Test method for 'pavlik.john.dungeoncrawl.model.Character.toString()'
   */
  public void testToString() {
    assertEquals("A Name", f_player.toString());
  }

  /**
   * Test method for 'pavlik.john.dungeoncrawl.model.Character.useItem(Consumable, Character)'
   */
  public void testUseItem() {
    f_world.tick(5);
    assertTrue(weapon.canUseItem(f_world.getCurrentTick()));
    assertTrue(f_player.sameLocationAs(f_player3));
    f_player.setUnconscious(f_player3);
    f_player.startAttack(f_player3);
    f_player.useItem(weapon, f_player3);
    assertFalse(f_player.inCombat());
    f_player3.startAttack(f_player);
    f_player3.useItem(weapon, f_player);
    assertFalse(f_player3.inCombat());
    f_player.tickAction(5);
    assertTrue(f_player.isConscious());
    f_player.startAttack(f_player3);
    f_player.useItem(weapon, null);
    assertFalse(f_player.inCombat());
    f_player.startAttack(f_player3);
    f_player.useItem(null, f_player3);
    assertFalse(f_player.inCombat());
    f_player3.setLocation(f_world.getNowherePlace());
    f_player.startAttack(f_player3);
    f_player.useItem(weapon, f_player3);
    assertFalse(f_player.inCombat());
    f_player.startAttack(f_player2);
    f_player.useItem(weapon, f_player2);
    assertTrue(f_player2.isConscious());
    assertTrue(f_player.inCombat());
    assertFalse(weapon.canUseItem(f_world.getCurrentTick()));
    f_world.tick(10);
    f_player3.setLocation(f_player.getLocation());
    f_player.useItem(weapon, f_player3);
    assertFalse(f_player.inCombat());

    f_player.startAttack(f_player3);
    f_player.useItem(weapon, f_player3);
    assertFalse(f_player3.isConscious());
    f_player3.tickAction(15);
    assertTrue(f_player3.isConscious());
    assertTrue(f_player3.getCurrentHealth() == 0);
    f_player.useItem(cursed, f_player3);
    assertFalse(f_player.isConscious());
    f_player3.useItem(potion, f_player3);
    assertTrue(f_player3.getCurrentHealth() > 0);
  }
}
