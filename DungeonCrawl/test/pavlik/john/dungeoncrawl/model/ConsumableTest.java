package pavlik.john.dungeoncrawl.model;

import java.util.HashMap;

import pavlik.john.dungeoncrawl.model.Consumable;
import pavlik.john.dungeoncrawl.model.Consumable.Effect;
import pavlik.john.dungeoncrawl.model.Consumable.EffectTarget;
import pavlik.john.dungeoncrawl.model.Consumable.EffectType;
import junit.framework.TestCase;

/**
 *
 * @author John
 *
 */
public class ConsumableTest extends TestCase {

  Consumable con;

  @Override
  protected void setUp() throws Exception {
    con = new Consumable("Consumable", "a", "desc", 0, 0, new HashMap<>(), true, "", new HashMap<>(), null, new Effect(
        5, 5), EffectType.DAMAGE, 5, 1, "potion", "drink", 100, EffectTarget.SINGLE, null, null);
  }

  /**
   *
   */
  public void testCanUseItem() {
    assertFalse(con.canUseItem(0));
    assertTrue(con.canUseItem(5));
  }

  /**
   *
   */
  public void testConsumable() {
    // Setup is enough
  }

  /**
   *
   */
  public void testGetCooldown() {
    assertEquals(5, con.getCooldown());
  }

  /**
   *
   */
  public void testGetEffect() {
    assertTrue(con.hasUsesLeft());
    assertTrue(con.getEffect() > 0);
    assertFalse(con.hasUsesLeft());
    assertEquals(0, con.getEffect());
  }

  /**
   *
   */
  public void testGetEffectTarget() {
    assertEquals(EffectTarget.SINGLE, con.getEffectTarget());
  }

  /**
   *
   */
  public void testGetEffectType() {
    assertEquals(EffectType.DAMAGE, con.getEffectType());
  }

  /**
   *
   */
  public void testGetHitChance() {
    assertEquals(100, con.getHitChance());
  }

  /**
   *
   */
  public void testGetHitSound() {
    assertNull(con.getHitSound());
  }

  /**
   *
   */
  public void testGetMissSound() {
    assertNull(con.getHitSound());
  }

  /**
   *
   */
  public void testGetNumDice() {
    assertEquals(5, con.getNumDice());
  }

  /**
   *
   */
  public void testGetNumSides() {
    assertEquals(5, con.getNumSides());
  }

  /**
   *
   */
  public void testGetType() {
    assertEquals("potion", con.getType());
  }

  /**
   *
   */
  public void testGetUsesRemaining() {
    assertTrue(1 == con.getUsesRemaining());
    con.getEffect();
    assertTrue(0 == con.getUsesRemaining());
    con.getEffect();
    assertTrue(0 == con.getUsesRemaining());
  }

  /**
   *
   */
  public void testGetUseString() {
    assertEquals("drink", con.getUseString());
  }

  /**
   *
   */
  public void testHasUsesLeft() {
    assertTrue(con.hasUsesLeft());
    con.getEffect();
    assertFalse(con.hasUsesLeft());
    con.getEffect();
    assertFalse(con.hasUsesLeft());
  }

  /**
   *
   */
  public void testHitTarget() {
    assertTrue(con.hitTarget(null));
  }

  /**
   *
   */
  public void testIsDoneCooling() {
    assertFalse(con.isDoneCooling(0));
    assertTrue(con.isDoneCooling(5));
  }

  /**
   *
   */
  public void testSetLastUseTick() {
    con.setLastUseTick(5);
    assertFalse(con.isDoneCooling(5));
    assertFalse(con.isDoneCooling(6));
    assertFalse(con.isDoneCooling(7));
    assertFalse(con.isDoneCooling(8));
    assertFalse(con.isDoneCooling(9));
    assertTrue(con.isDoneCooling(10));
  }

  /**
   *
   */
  public void testTicksRemaining() {
    con.setLastUseTick(5);
    assertEquals(5, con.ticksRemaining(5));
    assertEquals(4, con.ticksRemaining(6));
    assertEquals(3, con.ticksRemaining(7));
    assertEquals(2, con.ticksRemaining(8));
    assertEquals(1, con.ticksRemaining(9));
    assertEquals(0, con.ticksRemaining(10));
    assertEquals(0, con.ticksRemaining(11));
  }

}
