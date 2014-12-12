package pavlik.john.dungeoncrawl.model.events;

import pavlik.john.dungeoncrawl.model.Character;

/**
 * A generic Action interface for doing stuff. Extends {@link XMLAttribute} which requires a
 * getTag() and getValue() method as well.
 *
 * @author John
 * @version 1.3
 * @since 1.3
 */
public abstract class Action extends XMLAttribute {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  /**
   * Public constructor for Actions
   *
   * @param tag
   *          the XML attribute tag associated with this action
   * @param value
   *          the value to insert into the attribute
   */
  public Action(String tag, String value) {
    super(tag, value);
  }

  /**
   * Perform the action associated
   *
   * @param cause
   *          The player who initiated the event that caused this action to be executed
   *
   * @return true if successful
   */
  public abstract boolean performAction(Character cause);

}
