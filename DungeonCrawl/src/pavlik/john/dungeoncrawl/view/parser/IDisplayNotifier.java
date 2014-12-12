package pavlik.john.dungeoncrawl.view.parser;

import java.io.File;
import java.util.Optional;

import pavlik.john.dungeoncrawl.model.Player;

/**
 * For user interfaces that rely on a text parser, this interface provides a means for error
 * messages and command output to come from the parser directly to the UI. Some commands, such as
 * 'help' and 'look', are executed entirely in the UI (they may query the world, but don't affect
 * it).
 *
 * @author Robert Graham
 */
public interface IDisplayNotifier {

  /**
   * Displays a message to the user
   *
   * @param message
   *          The message to display
   * @throws NullPointerException
   *           if message is null
   */
  void display(String message);

  /**
   * Display a JFileChooser if available and return the File if the user selected one.
   *
   * @return an Optional generic with type File that will either contain a file if the user chose
   *         where to load one or will be empty if no file was chosen or it was not possible to
   *         display a JFileChooser or something similar
   */
  public default Optional<File> load() {
    return Optional.empty();
  }

  /**
   * The user quit the game
   */
  void quit();

  /**
   * Display a JFileChooser if available and return the File if the user selected one.
   *
   * @return an Optional generic with type File that will either contain a file if the user chose
   *         where to save it or will be empty if no file was chosen or it was not possible to
   *         display a JFileChooser or something similar
   */
  public default Optional<File> save() {
    return Optional.empty();
  }

  /**
   * Set the current player
   *
   * @param player
   *          The current player of the game.
   */
  void setCurrentPlayer(Player player);

  /**
   * Enable/Disable sound
   *
   * @param enable
   *          Whether to enable or disable
   */
  void setSound(boolean enable);

  /**
   * Toggle sound between enabled or disabled
   */
  void toggleSound();
}
