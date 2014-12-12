package pavlik.john.dungeoncrawl.model;

/**
 * Classes that have a container should implement this interface to notify other classes that this
 * contains a container.
 *
 * @author John
 * @since 1.3
 */
@FunctionalInterface
public interface HasContainer {

  /**
   * Return the container that this has.
   *
   * @return a {@link Container}
   */
  public Container getContainer();
}
