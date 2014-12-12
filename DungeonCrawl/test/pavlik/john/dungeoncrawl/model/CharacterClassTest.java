package pavlik.john.dungeoncrawl.model;

import java.util.Collection;
import java.util.HashSet;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

import pavlik.john.dungeoncrawl.model.CharacterClass;
import pavlik.john.dungeoncrawl.model.Consumable.EffectTarget;

/**
 * Test for CharacterClass
 *
 * @author John
 *
 */
public class CharacterClassTest extends TestCase {

  CharacterClass cClass1;

  @Override
  @Before
  public void setUp() throws Exception {
    final Collection<String> weaponTypes = new HashSet<String>();
    weaponTypes.add("sword");
    cClass1 = new CharacterClass("Class1", 5, 5, weaponTypes, "swing", 5, 5, 5, 100, 5, EffectTarget.SINGLE, null,
        null, "sword");
  }

  /**
   *
   */
  @Test
  public void testCanUseWeaponType() {
    assertTrue(cClass1.canUseWeaponType("sword"));
    assertFalse(cClass1.canUseWeaponType("gun"));
    assertFalse(cClass1.canUseWeaponType(null));
  }

  /**
   *
   */
  @Test
  public void testCharacterClassCharacterClass() {
    final CharacterClass duplicate = new CharacterClass(cClass1);
    assertNotSame(cClass1, duplicate);
    assertEquals(cClass1.getName(), duplicate.getName());
  }

  /**
   * JUnit named this. Just so you know.
   */
  @Test
  public void testCharacterClassStringIntIntCollectionOfStringStringIntIntIntIntIntEffectTargetStringStringString() {
    assertNotNull(cClass1);
  }

  /**
   *
   */
  @Test
  public void testGetDefaultWeapon() {
    assertNotNull(cClass1.getDefaultWeapon());
    assertEquals("sword", cClass1.getDefaultWeapon().getName());
  }

  /**
   *
   */
  @Test
  public void testGetHealthRegen() {
    assertEquals(5, cClass1.getHealthRegen());
  }

  /**
   *
   */
  @Test
  public void testGetKORecovery() {
    assertEquals(5, cClass1.getKORecovery());
  }

  /**
   *
   */
  @Test
  public void testGetMaxHealth() {
    assertEquals(5, cClass1.getMaxHealth());
  }

  /**
   *
   */
  @Test
  public void testGetName() {
    assertEquals("Class1", cClass1.getName());
  }

  /**
   *
   */
  @Test
  public void testGetWeaponTypes() {
    assertNotNull(cClass1.getWeaponTypes());
    assertTrue(cClass1.getWeaponTypes().contains("sword"));
  }
}
