package pavlik.john.dungeoncrawl.persistence;

import java.nio.file.Paths;

import pavlik.john.dungeoncrawl.TestConstants;
import pavlik.john.dungeoncrawl.exceptions.PersistenceStateException;
import pavlik.john.dungeoncrawl.model.Universe;
import pavlik.john.dungeoncrawl.persistence.GamePersistence;
import junit.framework.TestCase;

/**
 * @author T.J. Halloran
 * @author Brian Woolley
 * @see GamePersistence
 */
public class PersistenceSaveLoadTest extends TestCase {

  /**
   * Test for pavlik.john.dungeoncrawl.persistence.GamePersistence.loadWorld with an invalid
   * file
   */
  public void testLoadFileWithErrors() {
    try {
      GamePersistence.loadWorld(TestConstants.TESTFILEWITHERRORS);
      fail();
    } catch (final PersistenceStateException e) {
    }
  }

  /**
   * Test for pavlik.john.dungeoncrawl.persistence.GamePersistence.loadWorld with an invalid
   * file
   */
  public void testLoadIllegalFile() {
    try {
      GamePersistence.loadWorld(TestConstants.ILLEGALFILE);
      fail();
    } catch (final PersistenceStateException e) {
    }
  }

  /**
   * Test for pavlik.john.dungeoncrawl.persistence.GamePersistence.loadWorld with a missing file
   */
  public void testLoadMissingFile() {
    try {
      GamePersistence.loadWorld(TestConstants.MISSINGFILE);
      fail();
    } catch (final PersistenceStateException e) {
    }
  }

  /**
   * Test for pavlik.john.dungeoncrawl.persistence.GamePersistence.saveWorld and loadWorld
   */
  public void testSaveLoad() {
    Universe w = null;
    try {
      w = GamePersistence.loadWorld(TestConstants.v3TESTFILE);
    } catch (final PersistenceStateException e1) {
      fail(e1.getMessage());
    }
    try {
      GamePersistence.saveWorld(w, Paths.get(TestConstants.SAVEFILE));
    } catch (final Exception e) {
      fail();
    }
    if (w == null) {
      fail();
    }
    w = new Universe();
    w.createPlayer("defaultPlayer", w.getNowherePlace(), "", "", null);
    try {
      GamePersistence.saveWorld(w, Paths.get(TestConstants.SAVEFILE));
    } catch (final Exception e) {
      fail();
    }
    try {
      w = GamePersistence.loadWorld(Paths.get(TestConstants.SAVEFILE));
    } catch (final PersistenceStateException e) {
      fail(e.getMessage());
    }
    if (w == null) {
      fail();
    }
  }
}
