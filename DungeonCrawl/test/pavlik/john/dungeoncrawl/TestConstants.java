package pavlik.john.dungeoncrawl;

/**
 * @author T.J. Halloran
 * @author Brian Woolley
 */
public final class TestConstants {

  /**
   * The default temporary directory on the computer ended by the correct path separator for the
   * operating system.
   */
  public static final String TMP_PATH           = System.getProperties().getProperty("java.io.tmpdir", "C:\\")
      + System.getProperties().getProperty("file.separator");

  /**
   * A illegal or invalid filename. The value of this constant may not (fail to) work on all
   * configurations, so change it to one that does (fail) on your computer.
   */
  public static final String ILLEGALFILE        = "con";                                                      // reserved
  // on
  // Windows

  /**
   * A filename that is legal, but for which no file exists. The value of this constant may not
   * (fail to) work on all configurations, so change it to one that does (fail) on your computer.
   */
  public static final String MISSINGFILE        = TMP_PATH + "nosuch.xml";

  /**
   * A filename to save and load game saves to and from. The value of this constant may not work on
   * all configurations, so change it to one that works on your computer.
   */
  public static final String SAVEFILE           = TMP_PATH + "smallworld_test.xml";

  /**
   * A URL pointing to a small test world with known data. The path is relative to the root
   * directory, which is on the classpath.
   */
  public static final String TESTFILE           = "/pavlik/john/dungeoncrawlTestWorld.xml";
  /**
   * A URL pointing to a small test world with known data including NPCs. The path is relative to
   * the root directory, which is on the classpath.
   */
  public static final String v3TESTFILE         = "/pavlik/john/dungeoncrawlTestWorldv3.xml";
  /**
   * A URL pointing to a small test world with known data including NPCs. The path is relative to
   * the root directory, which is on the classpath.
   */
  public static final String v4TESTFILE         = "/pavlik/john/dungeoncrawlTestWorldFinal.xml";

  /**
   * A URL pointing to a second small test world with known data. The path is relative to the root
   * directory, which is on the classpath.
   */
  public static final String TESTFILE2          = "/pavlik/john/dungeoncrawlTestWorld2.xml";

  /**
   * A URL pointing to a small test world with known data that contains a valid win condition. The
   * path is relative to the root directory, which is on the classpath.
   */
  public static final String TESTWINFILE        = "/pavlik/john/dungeoncrawlTestArrivalWinsGameWorld.xml";

  /**
   * A URL pointing to a small test world with known data that contains one or more errors. The path
   * is relative to the root directory, which is on the classpath.
   */
  public static final String TESTFILEWITHERRORS = "/pavlik/john/dungeoncrawlTestWorldError.xml";

  /**
   * A URL pointing to the MessageBundle files that contain strings to be displayed dynamically
   * instead of hard coded. The path is relative to the root directory, which is on the classpath.
   */
  public static final String MESSAGEFILE        = "pavlik/john/dungeoncrawl/MessagesBundle";
}
