package pavlik.john.dungeoncrawl.model.events;

import java.io.Serializable;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;

import pavlik.john.dungeoncrawl.model.Character;

/**
 * One possible interaction event between a Player Character and a Non-Player Character. Only
 * allowed if conditionals are met, actions are performed immediately upon execution, changes NPC
 * state and location.
 *
 * @author John
 *
 */
public class Event implements Serializable {
  /**
   *
   */
  private static final long serialVersionUID = 1L;
  private final Set<Action> f_actions;

  /**
   * Constructor for an Event
   *
   * @param actions
   *          The actions performed by this event
   *
   */
  public Event(Set<Action> actions) {
    f_actions = Objects.requireNonNull(actions, "Event actions cannot be null");
  }

  /**
   * The player has decided to perform say f_message. Execute all actions associated with this
   * event.
   *
   * @param cause
   *          The current player
   */
  public void execute(Character cause) {
    for (final Action action : f_actions) {
      if (!action.performAction(cause)) {
        throw new IllegalStateException("Action failed, unable to continue, inconsistency in Universe detected");
      }
    }

  }

  /**
   * Access the actions in this event for persistence
   *
   * @return a Collection of XML attributes
   */
  public Collection<? extends XMLAttribute> getActions() {
    return f_actions;
  }
}
