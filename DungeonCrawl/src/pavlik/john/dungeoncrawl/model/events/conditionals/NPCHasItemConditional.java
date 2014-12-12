package pavlik.john.dungeoncrawl.model.events.conditionals;

import java.util.Objects;

import pavlik.john.dungeoncrawl.model.Character;
import pavlik.john.dungeoncrawl.model.Universe;
import pavlik.john.dungeoncrawl.model.events.Conditional;
import pavlik.john.dungeoncrawl.model.events.XMLAttribute;

/**
 * Check if the provided item is present in the specified container, at the time meetsConditions()
 * is called.
 *
 * @author John
 * @version 1.3
 */
public class NPCHasItemConditional extends Conditional {
  /**
   *
   */
  private static final long serialVersionUID = 1L;
  String f_npcName;
  Universe  f_world;
  String f_item;

  /**
   * Public Constructor
   *
   * @param world
   *          The current world
   * @param npcName
   *          The npc's unique name
   * @param item
   *          The name of the item to check for
   * @param xmlTag
   *          The XML tag to use to identify this {@link XMLAttribute}
   */
  public NPCHasItemConditional(Universe world, String npcName, String item, String xmlTag) {
    super(xmlTag, item);
    f_world = Objects.requireNonNull(world, "NPCHasItemConditional world cannot be null");
    f_npcName = Objects.requireNonNull(npcName, "NPCHasItemConditional npcName cannot be null");
    f_item = Objects.requireNonNull(item, "NPCHasItemConditional item cannot be null");
  }

  @Override
  public boolean meetsConditions(Character player) {
    return f_world.getNonPlayerCharacter(f_npcName).getContainer().isPresent(f_world.getItem(f_item));
  }
}
