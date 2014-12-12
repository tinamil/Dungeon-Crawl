package pavlik.john.dungeoncrawl.model.events.triggers;

import java.util.Set;

import pavlik.john.dungeoncrawl.model.events.Conditional;
import pavlik.john.dungeoncrawl.model.events.Event;
import pavlik.john.dungeoncrawl.model.events.Trigger;

/**
 * Health Trigger for detecting when NPCs have reached a certain level of damage
 *
 * @author John
 *
 */
public class HealthTrigger extends Trigger {
  /**
   *
   */
  private static final long serialVersionUID = 1L;
  private final int f_hp;

  /**
   * Public constructor for a trigger that is activated by reaching a certain level of HP (going
   * down only).
   *
   * @param hp
   *          The health value for this trigger
   *
   * @param event
   *          The event that gets activated
   * @param tag
   *          The tag from the XML
   * @param conditionals
   *          The conditions required to be met in addition to this trigger
   */
  public HealthTrigger(int hp, Event event, String tag, Set<Conditional> conditionals) {
    super(event, tag, Integer.toString(hp), conditionals);
    f_hp = hp;
  }

  /**
   * The health value to trigger this event at
   *
   * @return an int of the hp level trigger
   */
  public int getHealth() {
    return f_hp;
  }
}
