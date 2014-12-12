package pavlik.john.dungeoncrawl.exceptions;

/**
 * Wrapper around Exception for Network Exceptions
 *
 * @author John
 *
 */
public class NetworkConnectionClosedException extends Exception {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  /**
   * Wraps Exception and passes the string up to be stored for later display
   *
   * @param string
   *          The message
   */
  public NetworkConnectionClosedException(String string) {
    super(string);
  }

}
