package pavlik.john.dungeoncrawl.model.events.triggers;

import java.util.Set;

import pavlik.john.dungeoncrawl.model.Character;
import pavlik.john.dungeoncrawl.model.events.Conditional;
import pavlik.john.dungeoncrawl.model.events.Event;
import pavlik.john.dungeoncrawl.model.events.Trigger;

/**
 * A time trigger based on world ticks
 *
 * @author John
 *
 */
public class TimeTrigger extends Trigger {

  /**
   *
   */
  private static final long serialVersionUID = 1L;
  int f_timeTrigger;

  /**
   * A time trigger based on world ticks
   *
   * @param event
   *          The event to trigger
   * @param tag
   *          The XML tag
   * @param time
   *          The XML value as an integer
   * @param conditionals
   *          The pre-conditions that must be met to execute
   */
  public TimeTrigger(Event event, String tag, int time, Set<Conditional> conditionals) {
    super(event, tag, Integer.toString(time), conditionals);
    f_timeTrigger = time;
  }

  @Override
  public void execute(Character cause) {
    super.execute(cause);
  }

  /**
   * Get the time this trigger is supposed to execute
   *
   * @return The trigger time
   */
  public int getTriggerTime() {
    return f_timeTrigger;
  }
}
