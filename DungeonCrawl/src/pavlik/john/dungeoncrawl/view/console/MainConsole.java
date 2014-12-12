package pavlik.john.dungeoncrawl.view.console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ResourceBundle;

import pavlik.john.dungeoncrawl.controller.Controller;
import pavlik.john.dungeoncrawl.exceptions.PersistenceStateException;
import pavlik.john.dungeoncrawl.model.Character;
import pavlik.john.dungeoncrawl.model.Player;
import pavlik.john.dungeoncrawl.model.Universe;
import pavlik.john.dungeoncrawl.properties.Messages;
import pavlik.john.dungeoncrawl.view.GameObserver;
import pavlik.john.dungeoncrawl.view.parser.Parser;

/**
 * The main program for MainConsole with a text user interface.
 *
 * @author T.J. Halloran
 * @author Robert Graham
 * @author John Pavlik
 *
 * @version 1.2
 */
public final class MainConsole {

  /**
   * Private class extending the {@link GameObserver} and implementing the abstract functions
   * for a console version of the MainConsole game.
   *
   * @author John Pavlik
   *
   * @version 1.2
   */
  private class TextualParserWorldObserver extends GameObserver {

    public TextualParserWorldObserver(ResourceBundle messages) {
      super(messages);
      display(Messages.SPLASH);
    }

    @Override
    public void characterHeal(Character character, int heal) {
      super.characterHeal(character, heal);
      if (f_player == character) {
        displayHealth(f_player.getCurrentHealth() + heal);
      }
    }

    @Override
    public void characterHitsCharacterFor(Character attacker, String combatMsg, Character target, int damage) {
      super.characterHitsCharacterFor(attacker, combatMsg, target, damage);
      if (f_player == target) {
        displayHealth(f_player.getCurrentHealth() - damage);
      }
    }

    @Override
    public void display(String message) {
      System.out.println(message.replaceAll("\\[color=.+?\\]", "").replace("[/color]", ""));
    }

    private void displayHealth(int health) {
      display(f_messages.getString(Messages.HEALTH_PROMPT).replace(Messages.HEALTH_TAG, Integer.toString(health)));
    }

    @Override
    public void gameOver(Universe world) {
      f_continuePlaying = false;
      display(f_messages.getString(world.isGameWon() ? Messages.VICTORY : Messages.QUIT));
      for (final Player player : world.getPlayers()) {
        display(f_messages.getString(Messages.SCORE_MESSAGE).replace(Messages.SCORE_TAG, player.getScore().toString())
            .replace(Messages.NAME_TAG, player.getName()));
      }
      f_parser.shutdown();
      System.exit(0);
    }

    @Override
    public void quit() {
      gameOver(f_worldController.getWorld());
    }

    @Override
    public void worldLoaded(Universe world, String fileName) {
      f_player = null;
      if (f_parser != null) {
        f_parser.clearPlayer();
      }
    }
  }

  /**
   * The main program for MainConsole with a text user interface.
   *
   * @param args
   *          command-line arguments (ignored by this program).
   */
  public static void main(String[] args) {
    try {
      final MainConsole game = new MainConsole();
      System.out.println(f_messages.getString(Messages.TITLE));
      System.out.println();

      // Update the display
      game.f_parser.displayAvailablePlayers();

      /*
       * Enter the main loop (input-response) for the game.
       */
      game.playGame();
    } catch (final PersistenceStateException e) {
      e.printStackTrace();
      System.out.println("Unable to load default world due to error: " + e.getMessage());
    }

  }

  /**
   * The controller this user interface interacts with. The controller initially uses a default
   * world, however this can be subsequently changed by calling
   * {@link Controller#setWorld(Universe)}.
   */
  private final Controller            f_worldController;

  /**
   * A listener that receives commands and messages from the parser
   */
  private final TextualParserWorldObserver f_textParserWorldObserver;

  /**
   * A ResourceBundle that contains all of the messages input from and displayed to the user.
   */
  private static ResourceBundle            f_messages;
  /**
   * A text parser for game commands. This parser aggregates our world controller and invokes game
   * logic on the controller when it understands player commands to the game.
   */
  private final Parser          f_parser;

  /**
   * A IO stream used to input the user's commands from the console.
   */
  private static final BufferedReader      f_in = new BufferedReader(new InputStreamReader(System.in));

  /**
   * Flag to indicate if the user has requested the game to end. A value of <code>true</code>
   * indicates the game should continue. A value of <code>false</code> indicates the game should
   * terminate as soon as possible.
   */
  private boolean                          f_continuePlaying;

  /**
   * Public constructor for MainConsole class
   *
   * @throws PersistenceStateException
   *           if anything goes wrong while loading the default world.
   */
  public MainConsole() throws PersistenceStateException {
    f_messages = Messages.loadMessages("en", "US");
    f_worldController = new Controller();
    f_textParserWorldObserver = new TextualParserWorldObserver(f_messages);
    f_continuePlaying = true;
    f_parser = new Parser(f_worldController, f_textParserWorldObserver, f_textParserWorldObserver,
        f_messages, true);
  }

  /**
   * The main loop for the user interface. This method repeatedly prompts the user for commands and
   * processes the user's commands. It does not, by design, output the results of the command on the
   * game because this is done via a callback to {@link #update(Universe)}. Typically, this will occur
   * as a result of each user command, so the user will see some output describing of the impact of
   * his or her command on the game.
   */
  private void playGame() {
    try {
      while (f_continuePlaying) {
        final String command = readLineFromConsole();
        if (command != null) {
          f_parser.parse(command);
        }
      }
    } catch (final IOException e) {
      /*
       * Our ability to input from the console has failed for some reason. Print out a stack trace
       * to the console.
       */
      e.printStackTrace();
    }
  }

  /**
   * Reads a single line of text from the console. This is the users's next command for the game.
   *
   * @return the user's command (a single line of text).
   * @throws IOException
   *           if our attempt to input from the console has failed for some reason. This should
   *           typically not happen.
   */
  private String readLineFromConsole() throws IOException {
    return f_in.readLine();
  }
}
