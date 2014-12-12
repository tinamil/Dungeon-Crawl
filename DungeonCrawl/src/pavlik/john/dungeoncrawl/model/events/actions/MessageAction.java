package pavlik.john.dungeoncrawl.model.events.actions;

import java.util.Objects;

import pavlik.john.dungeoncrawl.model.Character;
import pavlik.john.dungeoncrawl.model.events.Action;
import pavlik.john.dungeoncrawl.model.events.XMLAttribute;

/**
 * Add the provided message to the world at the time performAction() is called.
 *
 * @author John
 * @version 1.3
 * @since 1.3
 */
public class MessageAction extends Action {
  /**
   *
   */
  private static final long serialVersionUID = 1L;
  final String f_response;
  final String f_name;

  /**
   * Public constructor
   *
   * @param response
   *          The response to add to the world
   * @param name
   *          The name of the character who is talking
   * @param tag
   *          The XML tag to use to identify this {@link XMLAttribute}
   */
  public MessageAction(String response, String name, String tag) {
    super(tag, response);
    f_response = Objects.requireNonNull(response, "MessageAction response must not be null");
    f_name = Objects.requireNonNull(name, "MessageAction npc must not be null");
  }

  @Override
  public boolean performAction(Character player) {
    player.getLocation().getWorld().broadcastMessage(player, f_name, f_response);
    return true;
  }
}
