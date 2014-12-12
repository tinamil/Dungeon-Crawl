package pavlik.john.dungeoncrawl.model.events.actions;

import java.util.Objects;

import pavlik.john.dungeoncrawl.model.Character;
import pavlik.john.dungeoncrawl.model.events.Action;
import pavlik.john.dungeoncrawl.model.events.XMLAttribute;

/**
 * Adjust the money available to the player upon execution of this action
 *
 * @author John
 * @version 1.3
 * @since 1.3
 */
public class AdjustMoneyAction extends Action {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  boolean f_giveMoney;

  String  f_moneyString;

  /**
   * Public constructor
   *
   * @param money
   *          The {@link String} amount of money to give. Must be readable with Integer.parseInt
   * @param giveMoney
   *          true if giving money to the player, false if taking money from the player. If false
   *          will multiply money value by -1.
   * @param tag
   *          The XML tag to use to identify this {@link XMLAttribute}
   */
  public AdjustMoneyAction(String money, boolean giveMoney, String tag) {
    super(tag, money);
    f_moneyString = Objects.requireNonNull(money, "AdjustMoneyAction money cannot be null");
    f_giveMoney = giveMoney;
    try {
      Integer.parseInt(money);
    } catch (final NumberFormatException e) {
      throw new IllegalStateException("Unable to parse money value.  Money must be an integer: " + money);
    }
  }

  @Override
  public boolean performAction(Character player) {
    final int money = Integer.parseInt((f_giveMoney ? "" : "-") + f_moneyString);
    player.changeMoney(money);
    player.getLocation().getWorld().moneyChanged(player, money);
    return true;
  }

}
