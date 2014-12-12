package pavlik.john.dungeoncrawl.controller;

import pavlik.john.dungeoncrawl.TestConstants;
import pavlik.john.dungeoncrawl.controller.Controller;
import pavlik.john.dungeoncrawl.controller.ControllerTimer;
import pavlik.john.dungeoncrawl.exceptions.PersistenceStateException;
import junit.framework.TestCase;

/**
 * @author John
 *
 */
public class WorldTimerTest extends TestCase {

  ControllerTimer      timer;
  Controller wc;

  @Override
  public void setUp() {
    try {
      wc = new Controller(TestConstants.v4TESTFILE);
      timer = new ControllerTimer(wc);
    } catch (final PersistenceStateException e) {
      fail(e.getMessage());
    }
  }

  /**
   * Test method for 'pavlik.john.dungeoncrawl.controller.ControllerTimer.run()'
   */
  public void testRun() {
    assertTrue(0 == wc.getWorld().getCurrentTick());
    try {
      Thread.sleep(75);
    } catch (final InterruptedException e) {
      fail(e.getMessage());
    }
    assertTrue(wc.getWorld().getCurrentTick() > 0);
  }
}
