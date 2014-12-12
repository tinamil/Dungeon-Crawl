package pavlik.john.dungeoncrawl.model.events.conditionals;

import java.util.Objects;

import pavlik.john.dungeoncrawl.model.Character;
import pavlik.john.dungeoncrawl.model.events.Conditional;
import pavlik.john.dungeoncrawl.model.events.XMLAttribute;

/**
 * Check if the provided item is present in the specified container, at the time meetsConditions()
 * is called.
 *
 * @author John
 * @version 1.3
 * @since 1.3
 */
public class PlayerHasItemConditional extends Conditional {
  /**
   *
   */
  private static final long serialVersionUID = 1L;
  String f_item;

  /**
   * Public Constructor
   *
   * @param item
   *          The name of the item to check for
   * @param xmlTag
   *          The XML tag to use to identify this {@link XMLAttribute}
   */
  public PlayerHasItemConditional(String item, String xmlTag) {
    super(xmlTag, item);
    f_item = Objects.requireNonNull(item, "PlayerHasItemConditional item cannot be null");
  }

  @Override
  public boolean meetsConditions(Character player) {
    return player.getContainer().isPresent(player.getLocation().getWorld().getItem(f_item));
  }
}
