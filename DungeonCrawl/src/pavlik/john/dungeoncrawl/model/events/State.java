package pavlik.john.dungeoncrawl.model.events;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * Represents the current mood of an NPC, and contains a list of potential events the player can
 * activate by interacting with this NPC.
 *
 * @author John
 * @since 1.3
 * @version 1.3
 */
public class State implements Serializable {
  /**
   *
   */
  private static final long serialVersionUID = 1L;
  private final String              f_name;
  private final String              f_description;
  private final Collection<Trigger> f_eventTriggers;

  /**
   * State constructor, names must be unique among States in order to distinguish them in the XML
   *
   * @param name
   *          The unique identifier name of this state
   * @param description
   *          The description of this state. Set to empty string if there is no description.
   */
  public State(String name, String description) {
    f_name = Objects.requireNonNull(name, "State name cannot be null");
    f_eventTriggers = new ArrayList<>();
    f_description = Objects.requireNonNull(description, "State " + name + "'s description cannot be null");
  }

  /**
   * Add a possible event trigger to this state
   *
   * @param event
   *          The new event trigger
   * @return true if the event trigger was not already present, false otherwise
   */
  public boolean addEventTrigger(Trigger event) {
    return f_eventTriggers.add(event);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof State) {
      return getName().equals(((State) obj).getName());
    }
    return super.equals(obj);
  }

  /**
   * Get the current description
   *
   * @return a description of the current state
   */
  public String getDescription() {
    return f_description;
  }

  /**
   * Use to get all of the possible events of this State
   *
   * @return a Collection of Events.
   */
  public Collection<Trigger> getEventTriggers() {
    return f_eventTriggers;
  }

  /**
   * Get the name of this state
   *
   * @return a public identifier of the state
   */
  public String getName() {
    return f_name;
  }
}
