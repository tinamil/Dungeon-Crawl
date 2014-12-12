package pavlik.john.dungeoncrawl.exceptions;

/**
 * Wrapper class that extends Exception.
 *
 * Replaces IllegalStateExceptions, because IllegalStateExceptions extend RuntimeException, which
 * doesn't require being handled and can slip through cracks
 *
 * @author John
 */
public class PersistenceStateException extends Exception {

  private static final long serialVersionUID = 1L;

  /**
   * Public constructor
   *
   * @param message
   *          The error message
   */
  public PersistenceStateException(String message) {
    super(message);
  }

  /**
   * Public constructor
   *
   * @param message
   *          The error message
   * @param cause
   *          The root cause exception
   */
  public PersistenceStateException(String message, Throwable cause) {
    super(message, cause);
  }
}
