package pavlik.john.dungeoncrawl.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Objects;

import pavlik.john.dungeoncrawl.model.Consumable.EffectTarget;

/**
 * The character class that defines the combat capabilities of a character
 *
 * @author Jay Giametta
 * @author John Pavlik
 */
public class CharacterClass implements Serializable {

  /**
   *
   */
  private static final long serialVersionUID = 1L;
  private final String      f_name;
  private final int         f_maxHealth;
  private final int         f_healthRegen;
  Collection<String>        f_useableWeaponTypes;
  private final int         f_KORecovery;

  Weapon                    f_defaultWeapon;

  /**
   * Constructor to duplicate a character class (as opposed to sharing the reference)
   *
   * @param characterClass
   *          The class to duplicate
   */
  public CharacterClass(CharacterClass characterClass) {
    this(characterClass.f_name, characterClass.f_maxHealth, characterClass.f_healthRegen,
        characterClass.f_useableWeaponTypes, characterClass.getDefaultWeapon().getUseString(),
        characterClass.f_KORecovery, characterClass.getDefaultWeapon().getNumDice(), characterClass.getDefaultWeapon()
        .getNumSides(), characterClass.getDefaultWeapon().getHitChance(), characterClass.getDefaultWeapon()
        .getCooldown(), characterClass.getDefaultWeapon().getEffectTarget(), characterClass.getDefaultWeapon()
        .getHitSound(), characterClass.getDefaultWeapon().getMissSound(), characterClass.getDefaultWeapon()
        .getName());
  }

  /**
   * Public constructor
   *
   * @param name
   *          The name of the class
   * @param maxHealth
   *          The max health of the character
   * @param healthRegen
   *          The amount of health to regen every 5 ticks
   * @param useableWeaponTypes
   *          The collection of usable weapon types
   * @param attackMsg
   *          The attack message to display when this class attacks
   * @param KORecovery
   *          The amount of time this class is unconscious
   * @param attackNumDice
   *          The default damage dice this class has
   * @param attackNumSides
   *          The default number of to the dice this class has
   * @param hitChance
   *          The default hit chance of this class
   * @param cooldown
   *          The cooldown rate of the default attack
   * @param target
   *          The targeting range of the default attack
   * @param onHitSound
   *          The onHit sound for the default attack
   * @param onMissSound
   *          The onMiss sound for the default attack
   * @param defaultWeaponName
   */
  CharacterClass(String name, int maxHealth, int healthRegen, Collection<String> useableWeaponTypes, String attackMsg,
      int KORecovery, int attackNumDice, int attackNumSides, int hitChance, int cooldown, EffectTarget target,
      String onHitSound, String onMissSound, String defaultWeaponName) {
    f_name = name;
    f_maxHealth = maxHealth;
    f_healthRegen = healthRegen;
    f_useableWeaponTypes = Objects.requireNonNull(useableWeaponTypes,
        "CharacterClass useableWeaponTypes cannot be null");
    f_KORecovery = KORecovery;
    f_defaultWeapon = new Weapon(defaultWeaponName, "", "", 0, 0, new HashMap<>(), new HashMap<>(), null, "default",
        new Consumable.Effect(attackNumDice, attackNumSides), cooldown, attackMsg, hitChance, target, onHitSound,
        onMissSound);
  }

  /**
   * Check if this class can use the specified weapon
   *
   * @param weaponType
   *          The weapon type to check
   * @return true if this is a usable weapon type
   */
  public boolean canUseWeaponType(String weaponType) {
    return f_useableWeaponTypes.contains(weaponType);
  }

  /**
   * Get the default weapon for this class. Not a real weapon, just one that holds stats.
   *
   * @return the default weapon
   */
  public Weapon getDefaultWeapon() {
    return f_defaultWeapon;
  }

  /**
   * Get the health regen of this class
   *
   * @return integer health to regen
   */
  public int getHealthRegen() {
    return f_healthRegen;
  }

  /**
   * Get the KO Recovery for this class
   *
   * @return the integer recovery rate
   */
  public int getKORecovery() {
    return f_KORecovery;
  }

  /**
   * Max health of this class
   *
   * @return The integer max health
   */
  public int getMaxHealth() {
    return f_maxHealth;
  }

  /**
   * Name of this class
   *
   * @return The class name
   */
  public String getName() {
    return f_name;
  }

  /**
   * Get all the weapon types this class supports
   *
   * @return A collection of String
   */
  public Collection<String> getWeaponTypes() {
    return f_useableWeaponTypes;
  }

}
