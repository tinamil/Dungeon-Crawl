package pavlik.john.dungeoncrawl.controller;

/**
 * Used to notify the results of a synthesis action.
 *
 * @author John
 *
 */
public enum Result {
  /**
   * The player provided the name of an item that player did not possess
   */
  ITEM_NOT_FOUND,
  /**
   * The set was not a valid combination
   */
  NO_ITEM_CREATED,
  /**
   * Method was successful
   */
  SUCCESS,
  /**
   * Failed but we don't know why
   */
  UNKNOWN_FAILURE,
  /**
   * Unable to find the player provided
   */
  CHARACTER_NOT_FOUND,
  /**
   * Not enough money to complete the transaction
   */
  INSUFFICIENT_MONEY,
  /**
   * Used when a player tries to move an item that can't be moved
   */
  ITEM_NOT_TAKEABLE,
  /**
   * If the item is not appropriate for that player / class
   */
  CANT_USE_ITEM,
  /**
   * Used if a player tries to attack the same target twice
   */
  ALREADY_IN_COMBAT,
  /**
   * Used if a player tries to attack an unconscious target
   */
  UNCONSCIOUS_TARGET,
  /**
   * Used if an item is attempted to be used that has a limited amount of uses
   */
  ITEM_CONSUMED;
  String itemFailure = null;

  /**
   * Get the item that the synthesis failed on
   *
   * @return the name of the item
   */
  public String getReason() {
    return itemFailure;
  }

  /**
   * Set the name of the item the synthesis failed on
   *
   * @param name
   *          The name of the item
   * @return this SynthesisFailed enum
   */
  public Result setReason(String name) {
    itemFailure = name;
    return this;
  }
}
