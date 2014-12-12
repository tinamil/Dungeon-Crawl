package pavlik.john.dungeoncrawl.model.events.triggers;

import java.util.Set;

import pavlik.john.dungeoncrawl.model.events.Conditional;
import pavlik.john.dungeoncrawl.model.events.Event;
import pavlik.john.dungeoncrawl.model.events.Trigger;

/**
 * Used when a NPC is attacked. Activates associated event.
 *
 * @author John
 *
 */
public class AttackedTrigger extends Trigger {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  /**
   * Public constructor for a trigger that is activated by saying things to an NPC
   *
   * @param event
   *          The event that gets activated
   * @param tag
   *          The tag from the XML
   * @param conditionals
   *          The conditions required to be met in addition to this trigger
   */
  public AttackedTrigger(Event event, String tag, Set<Conditional> conditionals) {
    super(event, tag, "Y", conditionals);
  }
}
