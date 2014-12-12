package pavlik.john.dungeoncrawl.model.events.triggers;

import java.util.Set;

import pavlik.john.dungeoncrawl.model.Character;
import pavlik.john.dungeoncrawl.model.Player;
import pavlik.john.dungeoncrawl.model.events.Conditional;
import pavlik.john.dungeoncrawl.model.events.Event;
import pavlik.john.dungeoncrawl.model.events.Trigger;

/**
 * Triggered when the specified character is seen
 *
 * @author John
 *
 */
public class SightTrigger extends Trigger {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  /**
   * Public constructor
   *
   * @param event
   *          The event to trigger
   * @param tag
   *          The XML tag from persistence
   * @param value
   *          The name of the character to attack, or 'player' to attack any player
   * @param conditionals
   *          The pre-conditions that must be met
   */
  public SightTrigger(Event event, String tag, String value, Set<Conditional> conditionals) {
    super(event, tag, value, conditionals);
  }

  @Override
  public void execute(Character cause) {
    final String targetName = getValue();
    if (targetName.equalsIgnoreCase("player") && cause instanceof Player) {
      super.execute(cause);
    } else if (cause.getName().equalsIgnoreCase(targetName)) {
      super.execute(cause);
    }
  }

}
