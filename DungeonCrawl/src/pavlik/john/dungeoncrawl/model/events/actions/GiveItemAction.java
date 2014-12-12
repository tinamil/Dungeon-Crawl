package pavlik.john.dungeoncrawl.model.events.actions;

import java.util.Objects;

import pavlik.john.dungeoncrawl.model.Character;
import pavlik.john.dungeoncrawl.model.Universe;
import pavlik.john.dungeoncrawl.model.events.Action;
import pavlik.john.dungeoncrawl.model.events.XMLAttribute;

/**
 * GiveItemAction moves a specified item from the players inventory to the specified NPCs inventory
 *
 * The player is giving away an item.
 *
 * @author John
 * @version 1.3
 * @since 1.3
 */
public class GiveItemAction extends Action {

  /**
   *
   */
  private static final long serialVersionUID = 1L;
  String f_npcName;
  String f_itemName;

  /**
   * Public constructor
   *
   * @param npcName
   *          The NPC's unique name
   * @param itemName
   *          The item's unique name
   * @param tag
   *          The XML tag to use to identify this {@link XMLAttribute}
   */
  public GiveItemAction(String npcName, String itemName, String tag) {
    super(tag, itemName);
    f_itemName = Objects.requireNonNull(itemName.trim(), "GiveItemAction itemName cannot be null");
    f_npcName = Objects.requireNonNull(npcName, "GiveItemAction npcName cannot be null");
  }

  @Override
  public boolean performAction(Character player) {
    final Universe world = player.getLocation().getWorld();
    if (player.getContainer().moveItem(world.getNonPlayerCharacter(f_npcName).getContainer(), world.getItem(f_itemName))) {
      world.playerLosesItem(player, world.getItem(f_itemName));
      return true;
    } else {
      throw new IllegalStateException("The player did not possess item " + f_itemName);
    }
  }

}
