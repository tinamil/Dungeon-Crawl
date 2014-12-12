package pavlik.john.dungeoncrawl.model.events.triggers;

import java.util.Objects;
import java.util.Set;

import pavlik.john.dungeoncrawl.model.Character;
import pavlik.john.dungeoncrawl.model.Universe;
import pavlik.john.dungeoncrawl.model.events.Conditional;
import pavlik.john.dungeoncrawl.model.events.Event;
import pavlik.john.dungeoncrawl.model.events.Trigger;

/**
 * A trigger for saying things to NPCs in order to activate events
 *
 * @author John
 *
 */
public class SayTrigger extends Trigger {

  /**
   *
   */
  private static final long serialVersionUID = 1L;
  private final String f_message;
  private final Universe  f_world;

  /**
   * Public constructor for a trigger that is activated by saying things to an NPC
   *
   * @param event
   *          The event that gets activated
   * @param world
   *          The current world
   * @param message
   *          The message that gets spoken to the NPC
   * @param sayTag
   *          The tag from the XML
   * @param conditionals
   *          The conditions required to be met in addition to this trigger
   */
  public SayTrigger(Event event, Universe world, String message, String sayTag, Set<Conditional> conditionals) {
    super(event, sayTag, message, conditionals);
    f_message = Objects.requireNonNull(message, "Message cannot be null for SayTrigger");
    f_world = Objects.requireNonNull(world, "Universe cannot be null for SayTrigger");
  }

  @Override
  public void execute(Character player) {
    f_world.broadcastMessage(player, player.toString(), f_message);
    super.execute(player);
  }

  /**
   * Access the message to display to the user for choosing
   *
   * @return The message to trigger this event
   */
  public String getMessage() {
    return f_message;
  }

}
