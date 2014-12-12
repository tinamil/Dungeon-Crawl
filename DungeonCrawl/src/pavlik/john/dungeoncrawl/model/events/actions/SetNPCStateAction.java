package pavlik.john.dungeoncrawl.model.events.actions;

import java.util.Iterator;
import java.util.Objects;

import pavlik.john.dungeoncrawl.model.Character;
import pavlik.john.dungeoncrawl.model.NonPlayerCharacter;
import pavlik.john.dungeoncrawl.model.Universe;
import pavlik.john.dungeoncrawl.model.events.Action;
import pavlik.john.dungeoncrawl.model.events.State;
import pavlik.john.dungeoncrawl.model.events.XMLAttribute;

/**
 * Action to change the current state of a specified NonPlayerCharacter.
 *
 * @author John
 * @version 1.3
 * @since 1.3
 */
public class SetNPCStateAction extends Action {
  /**
   *
   */
  private static final long serialVersionUID = 1L;

  String f_npcName;

  String f_newState;
  Universe  f_world;

  /**
   * Public constructor
   *
   * @param world
   *          The current {@link Universe}.
   * @param npcName
   *          The NPC's unique identifier / name.
   * @param newState
   *          The new {@link State} for the NPC to change to.
   * @param tag
   *          The XML tag to use to identify this {@link XMLAttribute}
   */
  public SetNPCStateAction(Universe world, String npcName, String newState, String tag) {
    super(tag, newState);
    f_world = Objects.requireNonNull(world, "SetNPCStateAction world cannot be null");
    f_newState = Objects.requireNonNull(newState, "SetNPCStateAction newState cannot be null");
    f_npcName = Objects.requireNonNull(npcName, "SetNPCStateAction npcName cannot be null");
  }

  @Override
  public boolean performAction(Character player) {
    return setNPCState();
  }

  /**
   * Change the given NPC's current state to a new one.
   *
   * @return true if successful
   */
  public boolean setNPCState() {
    final NonPlayerCharacter npc = f_world.getNonPlayerCharacter(f_npcName);
    if (npc == null) {
      throw new IllegalStateException("Unable to match npc name to an NPC in the world: " + f_npcName);
    }
    final Iterator<State> iterator = npc.getStates().iterator();
    while (iterator.hasNext()) {
      final State state = iterator.next();
      if (state.getName().equalsIgnoreCase(f_newState)) {
        npc.setCurrentState(state);
        return true;
      }
    }
    throw new IllegalStateException("Unable to match state: " + f_newState + " to one of the states of " + f_npcName);
  }

}
