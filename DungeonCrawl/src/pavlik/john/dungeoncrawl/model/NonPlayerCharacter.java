package pavlik.john.dungeoncrawl.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import pavlik.john.dungeoncrawl.model.events.State;
import pavlik.john.dungeoncrawl.model.events.Trigger;
import pavlik.john.dungeoncrawl.model.events.triggers.HealthTrigger;
import pavlik.john.dungeoncrawl.model.events.triggers.SayTrigger;
import pavlik.john.dungeoncrawl.model.events.triggers.TimeTrigger;
import pavlik.john.dungeoncrawl.view.TextUtilities;

/**
 * A non-player character that the {@link Player} can interact with inside of a given {@link Universe}.
 * Each NPC must have a unique name, but there could be many NPCs in a Universe.
 *
 * @author John Pavlik
 *
 * @version 1.2
 */
public class NonPlayerCharacter extends Character {
  /**
   *
   */
  private static final long serialVersionUID = 1L;
  Collection<State> f_states;
  State             f_currentState;

  /**
   * Constructor for a non-player character
   *
   * @param world
   *          The world this NPC was added to
   * @param name
   *          A {@link String} unique name
   * @param article
   *          A {@link String} article for referring to NPC, leave blank if name is a proper noun
   * @param description
   *          A {@link String} description of the NPC
   * @param inventory
   *          The {@link Container} of any items the NPC is carrying
   * @param location
   *          A {@link Place} of the NPCs location
   * @param states
   *          A collection of all possible states this NPC could be in
   * @param currentState
   *          The current state the NPC is in
   * @param respawn
   *          The place where this character should respawn after death. Set to null for no respawn
   *          or the nowhere place to make this NPC disappear after death.
   */
  NonPlayerCharacter(Universe world, String name, String article, String description, Container inventory, Place location,
      Set<State> states, State currentState, Place respawn) {
    super(world, name, article, description, inventory, location, respawn);
    f_states = Objects.requireNonNull(states, "NPC " + name + "'s states cannot be null");
    f_currentState = Objects.requireNonNull(currentState, "NPC " + name + "'s currentState cannot be null");
  }

  @Override
  protected void changeHealth(Character cause, int change) {
    for (final Trigger trigger : f_currentState.getEventTriggers()) {
      if (trigger instanceof HealthTrigger) {
        final int hpTriggerValue = ((HealthTrigger) trigger).getHealth();
        if (f_currentHealth + change <= hpTriggerValue && f_currentHealth > hpTriggerValue) {
          trigger.execute(cause);
        }
      }
    }
    super.changeHealth(cause, change);
  }

  /**
   * Current state of the Non Player Character for interacting with it
   *
   * @return a {@link State}
   */
  public State getCurrentState() {
    return f_currentState;
  }

  @Override
  public String getDescription() {
    final StringBuilder description = new StringBuilder(super.getDescription());
    final String currentStateDescription = f_currentState.getDescription();
    if (currentStateDescription.length() > 0) {
      description.append(TextUtilities.LINESEP);
      description.append(currentStateDescription);
    }
    return description.toString();
  }

  /**
   * Get all of the onSayTriggers that the current player meets the conditions to execute
   *
   * @param player
   *          The current player
   * @return a list of SayTrigger the player can execute
   */
  public List<SayTrigger> getSayTriggers(Player player) {
    final List<SayTrigger> currentEvents = new ArrayList<>();
    for (final Trigger eventTrigger : getCurrentState().getEventTriggers()) {
      if (eventTrigger.meetsConditions(player) && eventTrigger instanceof SayTrigger) {
        currentEvents.add((SayTrigger) eventTrigger);
      }
    }
    return currentEvents;
  }

  /**
   * Public access method for the states this NPC is capable of.
   *
   * @return a {@link Collection} of {@link State}
   */
  public Collection<State> getStates() {
    return f_states;
  }

  /**
   * Change this NPCs current state to a new one
   *
   * @param state
   *          the {@link State} to change to
   */
  public void setCurrentState(State state) {
    f_currentState = Objects.requireNonNull(state);
  }

  @Override
  public void tickAction(long numTicks) {
    super.tickAction(numTicks);
    for (final Trigger trigger : f_currentState.getEventTriggers()) {
      if (trigger instanceof TimeTrigger) {
        final int triggerTime = ((TimeTrigger) trigger).getTriggerTime();
        if (numTicks % triggerTime == 0) {
          trigger.execute(this);
        }
      }
    }
  }
}
