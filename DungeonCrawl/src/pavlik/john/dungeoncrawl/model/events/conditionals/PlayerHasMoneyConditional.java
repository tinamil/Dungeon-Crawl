package pavlik.john.dungeoncrawl.model.events.conditionals;

import java.util.Objects;

import pavlik.john.dungeoncrawl.model.Character;
import pavlik.john.dungeoncrawl.model.events.Conditional;
import pavlik.john.dungeoncrawl.model.events.XMLAttribute;

/**
 * PlayerHasMoneyConditional checks to see if the player has at least as much as the specified
 * amount of money.
 *
 * @author John
 * @version 1.3
 * @since 1.3
 */
public class PlayerHasMoneyConditional extends Conditional {
  /**
   *
   */
  private static final long serialVersionUID = 1L;
  Integer f_money;

  /**
   * Public Constructor
   *
   * @param money
   *          The integer amount of money to check for.
   * @param tag
   *          The XML tag to use to identify this {@link XMLAttribute}
   */
  public PlayerHasMoneyConditional(Integer money, String tag) {
    super(tag, money.toString());
    f_money = Objects.requireNonNull(money, "PlayerHasMoneyConditional money cannot be null");
  }

  @Override
  public boolean meetsConditions(Character player) {
    return player.getMoney() >= f_money;
  }

}
