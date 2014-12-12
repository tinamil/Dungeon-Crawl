package pavlik.john.dungeoncrawl.controller;

import java.util.TimerTask;

import pavlik.john.dungeoncrawl.model.Universe;

/**
 * This class is used to incorporate timed actions in a {@link Universe}.
 *
 * @author Jay Giametta
 * @author John Pavlik
 *
 * @version 1.4
 * @see Universe
 */
public class ControllerTimer extends TimerTask {

  private final Controller f_controller;

  private long             f_numTicks;

  /**
   * Creates a new instance of <code>ControllerTimer</code> for the default world.
   *
   * @param controller
   *          the {@link Controller} associated with this timer
   */
  ControllerTimer(Controller controller) {
    f_controller = controller;
  }

  /**
   * Increments the timer's tick counter
   */
  @Override
  public void run() {
    f_numTicks++;
    f_controller.tick(f_numTicks);
  }

}
