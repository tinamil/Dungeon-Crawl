package pavlik.john.dungeoncrawl.model.events.actions;

import java.util.Objects;

import pavlik.john.dungeoncrawl.model.Character;
import pavlik.john.dungeoncrawl.model.NonPlayerCharacter;
import pavlik.john.dungeoncrawl.model.Place;
import pavlik.john.dungeoncrawl.model.Universe;
import pavlik.john.dungeoncrawl.model.events.Action;
import pavlik.john.dungeoncrawl.model.events.XMLAttribute;

/**
 * Action to change the state of a NPC
 *
 * @author John
 * @version 1.3
 * @since 1.3
 */
public class SetNPCLocationAction extends Action {
  /**
   *
   */
  private static final long serialVersionUID = 1L;

  String f_npcName;

  Universe  f_world;
  String f_location;

  /**
   * Public constructor
   *
   * @param world
   *          The current world
   * @param npcName
   *          The NPCs name
   * @param location
   *          The NPCs new location
   * @param tag
   *          The XML tag to use to identify this {@link XMLAttribute}
   */
  public SetNPCLocationAction(Universe world, String npcName, String location, String tag) {
    super(tag, location);
    f_npcName = Objects.requireNonNull(npcName, "SetNPCLocationAction npcName cannot be null");
    f_location = Objects.requireNonNull(location, "SetNPCLocationAction location cannot be null");
    f_world = Objects.requireNonNull(world, "SetNPCLocationAction world cannot be null");
  }

  @Override
  public boolean performAction(Character player) {
    return setNPCLocation();
  }

  /**
   * Move a given NPC to a new location
   *
   * @return true if both npcName and location existed within the world
   */
  public boolean setNPCLocation() {
    final NonPlayerCharacter npc = f_world.getNonPlayerCharacter(f_npcName);
    if (npc == null) {
      throw new IllegalStateException("Unable to match npc name to NPC in world: " + f_npcName);
    }
    final Place newPlace = f_world.getPlace(f_location);
    if (newPlace == null) {
      throw new IllegalStateException("Unable to match place name: " + f_location);
    }
    npc.setLocation(newPlace);
    return true;
  }

}
