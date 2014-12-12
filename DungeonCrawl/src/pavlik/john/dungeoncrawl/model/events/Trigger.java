package pavlik.john.dungeoncrawl.model.events;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import pavlik.john.dungeoncrawl.model.Character;

/**
 * Used to trigger an {@link Event}.
 *
 * @author John
 *
 */
public abstract class Trigger extends XMLAttribute {
  /**
   *
   */
  private static final long serialVersionUID = 1L;

  Event                          f_event;

  private final Set<Conditional> f_conditionals;

  /**
   * @param event
   *          The event that gets triggered
   * @param tag
   *          The XML Attribute tag
   * @param value
   *          The XML Attribute value
   * @param conditionals
   *          The conditions both characters must meet to qualify to execute this event.
   */
  public Trigger(Event event, String tag, String value, Set<Conditional> conditionals) {
    super(tag, value);
    f_event = Objects.requireNonNull(event, "event for trigger " + tag + " cannot be null");
    f_conditionals = Objects.requireNonNull(conditionals, "Event conditionals cannot be null");
  }

  /**
   * Execute this event, which was triggered by the specified character
   *
   * @param cause
   *          The character who met the conditions of this trigger
   */
  public void execute(Character cause) {
    if (meetsConditions(cause)) {
      f_event.execute(cause);
    }
  }

  /**
   * Get direct access to the list of XML attributes contained in this event.
   *
   * @return a {@link Collection} of {@link XMLAttribute}.
   */
  public Collection<XMLAttribute> getXMLAttributes() {
    final Collection<XMLAttribute> attributes = new HashSet<XMLAttribute>(f_conditionals);
    attributes.addAll(f_event.getActions());
    attributes.add(this);
    return attributes;
  }

  /**
   * Check to see if all of the conditions required for this event have been met
   *
   * @param cause
   *          The current player
   *
   * @return true if all conditions return true, false if any condition fails
   */
  public boolean meetsConditions(Character cause) {
    for (final Conditional condition : f_conditionals) {
      if (!condition.meetsConditions(cause)) {
        return false;
      }
    }
    return true;
  }
}
