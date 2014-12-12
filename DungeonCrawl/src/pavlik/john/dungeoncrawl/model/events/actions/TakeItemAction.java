package pavlik.john.dungeoncrawl.model.events.actions;

import java.util.Objects;

import pavlik.john.dungeoncrawl.model.Character;
import pavlik.john.dungeoncrawl.model.Universe;
import pavlik.john.dungeoncrawl.model.events.Action;
import pavlik.john.dungeoncrawl.model.events.XMLAttribute;

/**
 * TakeItemAction will move an item from the specified NPC's inventory to the player's inventory.
 *
 * The player is taking an item.
 *
 * @author John
 * @version 1.3
 * @since 1.3
 */
public class TakeItemAction extends Action {

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
  public TakeItemAction(String npcName, String itemName, String tag) {
    super(tag, itemName);
    f_itemName = Objects.requireNonNull(itemName.trim(), "TakeItemAction itemName cannot be null");
    f_npcName = Objects.requireNonNull(npcName, "TakeItemAction npcName cannot be null");
  }

  @Override
  public boolean performAction(Character player) {
    final Universe world = player.getLocation().getWorld();
    if (world.getNonPlayerCharacter(f_npcName).getContainer()
        .moveItem(player.getContainer(), world.getItem(f_itemName))) {
      world.playerGainsItem(player, world.getItem(f_itemName));
      return true;
    } else {
      throw new IllegalStateException("The NPC " + f_npcName + " did not possess item " + f_itemName);
    }
  }

}
