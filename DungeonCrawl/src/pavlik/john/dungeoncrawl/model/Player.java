package pavlik.john.dungeoncrawl.model;

/**
 * 
 * @author John Pavlik
 *
 * @version 1.3
 */
public class Player extends Character {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  /**
   * Number of points the player has accumulated
   *
   * @since 1.2
   */
  private Long              f_points         = 0L;

  private boolean           f_occupied       = false;

  /**
   * Constructs a new instance.
   *
   * @param world
   *          This player's world
   * @param place
   *          the {@link Place} instance this exists within.
   * @param name
   *          A unique player name
   * @param article
   *          The article to refer to this player, leave blank "" if unnecessary.
   * @param description
   *          The description of this player.
   * @param respawn
   *          The place where this player should respawn after death. Set to null for no respawn.
   */
  public Player(Universe world, Place place, String name, String article, String description, Place respawn) {
    super(world, name, article, description, new Container(), place, respawn);
  }

  /**
   * Call this to add points to the players count
   *
   * @param points
   *          The number of points to add to the player
   * @return The number of points the player has now
   * @since 1.2
   */
  public Long addPoints(Long points) {
    return f_points += points;
  }

  /**
   * Access the number of points the player has currently accumuluated.
   *
   * @return A Long of the player's current score
   * @since 1.2
   */
  public Long getScore() {
    return f_points;
  }

  /**
   * Check to see if this player is occupied
   *
   * @return true if occupied, false if claimed
   */
  public boolean isOccupied() {
    return f_occupied;
  }

  /**
   * Claims a player for use by a user if occupied = true, or releases the player for for use by
   * someone else if occupied = false
   *
   * @param occupied
   *          A boolean, set to true to set this player as occupied, and false to release the player
   */
  public void setOccupied(boolean occupied) {
    f_occupied = occupied;
  }

}
