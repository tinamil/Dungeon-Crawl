package pavlik.john.dungeoncrawl.model.events;

import pavlik.john.dungeoncrawl.model.Character;

/**
 * A generic Conditional interface for checking if something can be done. Extends
 * {@link XMLAttribute} which requires a getTag() and getValue() method as well.
 *
 * @author John
 * @version 1.3
 * @since 1.3
 */
public abstract class Conditional extends XMLAttribute {
  /**
   *
   */
  private static final long serialVersionUID = 1L;

  /**
   * Public constructor for Conditionals
   *
   * @param tag
   *          the XML attribute tag associated with this conditional
   * @param value
   *          the value to insert into the attribute
   */
  public Conditional(String tag, String value) {
    super(tag, value);
  }

  /**
   * Have all conditions been met?
   *
   * @param cause
   *          The current player
   *
   * @return true if so
   */
  public abstract boolean meetsConditions(Character cause);

}
