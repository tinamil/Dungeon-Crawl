/**
 *
 */
package pavlik.john.dungeoncrawl.model;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

/**
 * Consumable items
 *
 * @author John
 *
 */
public class Consumable extends Item {
  /**
   * Used for calculating damage based on simulating dice rolls.
   *
   * @author John
   */
  public static class Effect implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    int                       f_numDice;

    int                       f_numSides;
    transient Random          random           = new Random();

    /**
     * Public constructor
     *
     * @param numDice
     *          The number of dice to roll
     * @param numSides
     *          The number of sides on each dice, equivalent to damage in the range of 1-numSides
     *          inclusive.
     */
    public Effect(int numDice, int numSides) {
      f_numDice = numDice;
      f_numSides = numSides;
    }

    /**
     * Get an effect by simulating dice rolls
     *
     * @return The amount of the effect
     */
    public int calculate() {
      int effect = 0;
      for (int i = 0; i < f_numDice; i++) {
        effect += random.nextInt(f_numSides) + 1;
      }
      return effect;
    }
  }

  /**
   * Used to delimit item effect ranges and targets
   *
   * @author John
   */
  public enum EffectTarget {
    /**
     * Can only target the character who activates it
     */
    SELF,
    /**
     * Can target a single character
     */
    SINGLE,
    /**
     * Targets a single character but effects all characters in melee range
     */
    GROUP,
    /**
     * Effects all characters in same place
     */
    PLACE;
  }

  /**
   * The kinds of effects that can happen. Primarily DAMAGE and HEALING.
   *
   * @author John
   *
   */
  public enum EffectType {
    /**
     * Do damage
     */
    DAMAGE, /**
     * Heal damage
     */
    HEALING;
  }

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private final Effect           f_effect;
  private final EffectType       f_effectType;
  private int                    f_usesRemaining;
  private final int              f_cooldown;
  private final String           f_type;
  private final String           f_useString;
  private final int              f_hitChance;
  private final EffectTarget     f_target;
  private long                   f_lastUseTick;
  private final transient Random random = new Random();
  private final String           f_hitSound;
  private final String           f_missSound;

  Consumable(String name, String article, String description, long takePoints, long dropPointsNowhere,
      Map<Place, Long> dropPointsMap, Boolean takeable, String cantTake, Map<Place, String> blockMessages,
      Container container, Effect effect, EffectType effectType, int cooldown, int uses, String type, String useString,
      int hitChance, EffectTarget targets, String onHitSound, String onMissSound) {
    super(name, article, description, takePoints, dropPointsNowhere, dropPointsMap, takeable, cantTake, blockMessages,
        container);
    f_usesRemaining = uses;
    f_effectType = effectType;
    f_cooldown = cooldown;
    f_effect = Objects.requireNonNull(effect, "Consumable " + name + "'s effect cannot be null");
    f_type = Objects.requireNonNull(type, "Consumable " + name + "'s type cannot be null");
    f_useString = Objects.requireNonNull(useString, "Consumable " + name + "'s useString cannot be null");
    f_hitChance = hitChance;
    f_target = Objects.requireNonNull(targets, "Consumable " + name + "'s targets cannot be null");
    f_hitSound = onHitSound;
    f_missSound = onMissSound;
  }

  /**
   * Check if this item has uses remaining and is done cooling down from the last use
   *
   * @param currentTick
   *          the current tick of the world
   * @return true if successful
   */
  public boolean canUseItem(long currentTick) {
    return (hasUsesLeft() && isDoneCooling(currentTick));
  }

  /**
   * Get the cooldown rate for persistence
   *
   * @return the cooldown rate
   */
  public int getCooldown() {
    return f_cooldown;
  }

  /**
   * Get a new random effect for this consumable. Will consume one use.
   *
   * @return the integer amount of the effect of the consumable
   */
  public int getEffect() {
    if (f_usesRemaining == 0) {
      return 0;
    } else if (f_usesRemaining > 0) {
      f_usesRemaining -= 1;
    }
    // And if f_usesRemaining is negative then assume that means infinity uses
    return f_effect.calculate();
  }

  /**
   * The target effect range of this weapon
   *
   * @return an {@link Consumable.EffectTarget} enum
   */
  public EffectTarget getEffectTarget() {
    return f_target;
  }

  /**
   * @return The {@link EffectType} of this Consumable
   */
  public EffectType getEffectType() {
    return f_effectType;
  }

  /**
   * Used for persistence
   *
   * @return get the hit chance
   */
  public int getHitChance() {
    return f_hitChance;
  }

  /**
   * Get the path to the sound file to play on hitting with this consumable
   *
   * @return The string path to the sound file to play
   */
  public String getHitSound() {
    return f_hitSound;
  }

  /**
   * Get the path to the sound file to play on missing with this consumable
   *
   * @return The string path to the sound file to play
   */
  public String getMissSound() {
    return f_missSound;
  }

  /**
   * Used for persistence
   *
   * @return Get the number of dice in the effect
   */
  public int getNumDice() {
    return f_effect.f_numDice;
  }

  /**
   * User persistence
   *
   * @return get the number of sides per dice in the effect
   */
  public int getNumSides() {
    return f_effect.f_numSides;
  }

  /**
   * Get this consumable's type
   *
   * @return The String type of this consumable
   */
  public String getType() {
    return f_type;
  }

  /**
   * Used for persistence
   *
   * @return get how many uses remain
   */
  public Integer getUsesRemaining() {
    return f_usesRemaining;
  }

  /**
   * Access the use string of this weapon
   *
   * @return the string to display
   */
  public String getUseString() {
    return f_useString;
  }

  /**
   * Check if this consumable still has any remaining uses/charges.
   *
   * @return True if usesRemaining is not equal to zero.
   */
  public boolean hasUsesLeft() {
    return f_usesRemaining != 0;
  }

  /**
   * See if the hit chance is high enough to hit this target
   *
   * @param target
   *          The specified target
   * @return true if the target was hit
   */
  public boolean hitTarget(Character target) {
    return random.nextInt(100) + 1 <= f_hitChance;
  }

  /**
   * Check if this weapon is ready to attack. Does not reset last use tick.
   *
   * @param nowTick
   *          The current tick of the world
   * @return true if the cooldown period has been met
   */
  public boolean isDoneCooling(long nowTick) {
    return ticksRemaining(nowTick) == 0;
  }

  /**
   * Save the last time this weapon has been used
   *
   * @param currentTick
   *          The current time of the world at which this weapon was most recently used
   */
  public void setLastUseTick(long currentTick) {
    f_lastUseTick = currentTick;
  }

  /**
   * Get the amount of ticks remaining until this item can be used again
   *
   * @param nowTick
   *          The current tick of the world.
   * @return the number of ticks remaining until this item can be used again, or 0 if it's ready for
   *         use now
   */
  public long ticksRemaining(long nowTick) {
    return Math.max(0, f_cooldown - (nowTick - f_lastUseTick));
  }

}
