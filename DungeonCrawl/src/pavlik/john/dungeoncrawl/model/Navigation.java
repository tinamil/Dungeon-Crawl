package pavlik.john.dungeoncrawl.model;

import java.io.Serializable;
import java.util.Optional;

/**
 * Navigation Options
 * 
 * @author John Pavlik
 *
 * @version 1.1
 */
public enum Navigation implements Serializable {

  /**
   * 0 degrees on the compass
   */
  NORTH,
  /**
   * 180 degrees on the compass
   */
  SOUTH,
  /**
   * 90 degrees on the compass
   */
  EAST,
  /**
   * 270 degrees on the compass
   */
  WEST,
  /**
   * 90 degrees perpindicular (up) from the compass.
   */
  UP,
  /**
   * 90 degrees perpindicular (down) from the compass.
   */
  DOWN;
  /**
   * Gets the appropriate {@link Navigation} instance from a full or abbreviated string mnemonic.
   * For example, {@link Navigation#NORTH} is returned for the mnemonic "NORTH" or "N".
   *
   * @param mnemonic
   *          the full or abbreviated {@link String} name for the desired direction.
   * @return a {@link java.util.Optional} containing the appropriate {@link Navigation} instance if
   *         it was found
   * @throws NullPointerException
   *           if mnemonic == null
   *
   */
  static public Optional<Navigation> getInstance(String mnemonic) throws NullPointerException {
    if (mnemonic == null) {
      throw new NullPointerException("Received null value for mnemonic string");
    }
    for (final Navigation possibleDirection : Navigation.values()) {
      if (mnemonic.equalsIgnoreCase(possibleDirection.toString())
          || mnemonic.equalsIgnoreCase(possibleDirection.getAbbreviation())) {
        return Optional.of(possibleDirection);
      }
    }
    return Optional.empty();
  }

  /**
   * Gets the abbreviation for this direction. This is defined to be the first letter of the
   * direction, for example <code>N</code> for <code>NORTH</code>.
   *
   * @return the abbreviation used for this direction.
   */
  public String getAbbreviation() {
    return toString().substring(0, 1);
  }
}
