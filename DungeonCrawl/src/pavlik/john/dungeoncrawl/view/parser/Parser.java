package pavlik.john.dungeoncrawl.view.parser;

import java.io.File;
import java.text.Collator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pavlik.john.dungeoncrawl.controller.Result;
import pavlik.john.dungeoncrawl.controller.Controller;
import pavlik.john.dungeoncrawl.exceptions.NetworkConnectionClosedException;
import pavlik.john.dungeoncrawl.exceptions.PersistenceStateException;
import pavlik.john.dungeoncrawl.model.Container;
import pavlik.john.dungeoncrawl.model.Navigation;
import pavlik.john.dungeoncrawl.model.IModelObserver;
import pavlik.john.dungeoncrawl.model.Item;
import pavlik.john.dungeoncrawl.model.NonPlayerCharacter;
import pavlik.john.dungeoncrawl.model.Place;
import pavlik.john.dungeoncrawl.model.Player;
import pavlik.john.dungeoncrawl.model.events.triggers.SayTrigger;
import pavlik.john.dungeoncrawl.persistence.MultiplayerServerThread;
import pavlik.john.dungeoncrawl.properties.Messages;
import pavlik.john.dungeoncrawl.view.NetworkClient;
import pavlik.john.dungeoncrawl.view.TextUtilities;

/**
 * Parses user commands and informs a specified {@link Controller} instance of its results. For
 * example, the String "go north" causes the {@link Controller#travel(Player, Navigation)} method to
 * be invoked.
 *
 * @author T.J. Halloran
 * @author Robert Graham
 * @author Brian Woolley
 * @author John Pavlik
 *
 * @version 1.3
 */
public final class Parser {

  List<SayTrigger>                currentEvents   = null;

  /**
   * The non-null world controller associated with this parser.
   */
  private final Controller        f_worldController;

  /**
   * An observer of this parser, recipient of certain UI-only commands and error messages.
   */
  private final IDisplayNotifier  f_parserObserver;
  private final IModelObserver    f_worldObserver;
  /**
   * Messages that were dynamically loaded by the View from the MessagesBundle properties file
   */
  private final ResourceBundle    f_messages;
  /**
   * The current player
   */
  private Player                  f_player;

  private MultiplayerServerThread f_multiplayer   = null;

  private final boolean           f_admin;

  private NetworkClient           f_networkClient = null;

  /**
   * Constructs a text command parser with the specified observer and Controller. The controller is
   * sent commands that affect the Universe (or could), while the observer is sent error messages
   * and output from commands that only query the world, in particular those expected to be unique
   * to a text-based interface.
   *
   * @param controller
   *          a non-null controller for this parser to invoke once it understands the user's game
   *          command.
   * @param worldObserver
   *          a non-null observer for this parser to send messages and query command output to
   * @param parserObserver
   *          a non-null observer for this parser to send messages and query command output to
   * @param messages
   *          a ResourceBundle dynamically loaded by the view for outputting to and comparing
   *          messages from the user
   * @param admin
   *          a boolean that represents whether this View has permissions to perform server side
   *          commands such as save/load/start server/stop server/etc.
   */
  public Parser(Controller controller, IModelObserver worldObserver, IDisplayNotifier parserObserver,
      ResourceBundle messages, boolean admin) {
    if (controller == null) {
      throw new NullPointerException("controller cannot be null");
    }
    if (worldObserver == null) {
      throw new NullPointerException("worldObserver cannot be null");
    }
    if (parserObserver == null) {
      throw new NullPointerException("parserObserver cannot be null");
    }
    if (messages == null) {
      throw new NullPointerException("messages cannot be null");
    }
    f_worldController = controller;
    f_parserObserver = parserObserver;
    f_worldObserver = worldObserver;
    f_messages = messages;
    f_admin = admin;
  }

  private void attack(String[] newCommand) {
    if (newCommand.length == 1) {
      f_parserObserver.display(f_messages.getString(Messages.ATTACK_CMD_HELP));
    }
    if (newCommand.length > 1) {
      final Result result = f_worldController.attack(f_player, newCommand[1]);
      switch (result) {
        case CHARACTER_NOT_FOUND:
          f_parserObserver.display(f_messages.getString(Messages.MISSING_CHARACTER).replace(Messages.CHARACTER_TAG,
              newCommand[1]));
          break;
        case CANT_USE_ITEM:
          f_parserObserver.display(f_messages.getString(Messages.NO_CLASS_MSG));
          break;
        case ALREADY_IN_COMBAT:
          f_parserObserver.display(f_messages.getString(Messages.NO_MORE_ATTACK));
          break;
        case UNCONSCIOUS_TARGET:
          f_parserObserver.display(f_messages.getString(Messages.UNCONSCIOUS_MSG).replace(Messages.CHARACTER_TAG,
              newCommand[1]));
          break;
        case SUCCESS:
          break;
        default:
          f_parserObserver.display(f_messages.getString(Messages.UNKNOWN_FAILURE));
      }
    }
  }

  private void changePlayer(String command) {
    if (command.toUpperCase().startsWith(f_messages.getString(Messages.CHANGE_PLAYER_COMMAND))) {
      command = command.substring(f_messages.getString(Messages.CHANGE_PLAYER_COMMAND).length()).trim();
    }
    if (f_player != null) {
      f_worldController.releasePlayer(f_player);
    }
    f_worldController.getWorld().removeObserver(f_worldObserver);
    f_player = f_worldController.playAs(command);
    if (f_player == null) {
      displayAvailablePlayers();
    } else {
      f_parserObserver.setCurrentPlayer(f_player);
      f_worldController.getWorld().addObserver(f_worldObserver);
      f_worldObserver.playSound(f_player, f_player.getLocation().getSound());
      lookCommand();
    }
  }

  private boolean checkAdmin() {
    if (!f_admin) {
      f_parserObserver.display(f_messages.getString(Messages.NOT_ALLOWED));
      return false;
    } else {
      return true;
    }
  }

  /**
   * Clear the current player. Use when a new world has been loaded.
   */
  public void clearPlayer() {
    f_player = null;
  }

  private void combineItems(String command) {
    final String[] itemNames = command.split(",");
    final Set<String> itemNameSet = new HashSet<>(itemNames.length);
    for (final String name : itemNames) {
      itemNameSet.add(name.trim());
    }
    final Result result = f_worldController.synthesizeItems(f_player, itemNameSet);
    switch (result) {
      case SUCCESS:
        // do nothing, every player was notified through the worldObserver
        break;
      case ITEM_NOT_FOUND:
        f_parserObserver.display(f_messages.getString(Messages.ITEM_NOT_FOUND).replace(Messages.ITEM_TAG,
            result.getReason()));
        break;
      case NO_ITEM_CREATED:
        f_parserObserver.display(f_messages.getString(Messages.NO_ITEM_CREATED));
        break;
      default:
        // Other return states not valid
    }
  }

  private void connectMultiplayerClient(String substring) {
    final String[] splitCommand = substring.split(":");
    String address = null;
    int port = -1;
    if (splitCommand.length > 1) {
      try {
        port = Integer.parseInt(splitCommand[1]);
      } catch (final NumberFormatException e) {
        f_parserObserver.display(f_messages.getString(Messages.PORT_PROBLEM));
        return;
      }
    }
    if (splitCommand.length > 0) {
      address = splitCommand[0];
    }
    connectMultiplayerClient(address, port);
  }

  private void connectMultiplayerClient(String address, int port) {
    if (port < 0 && address == null) {
      f_networkClient = new NetworkClient(f_parserObserver, f_worldObserver);
    } else if (port < 0 && address != null) {
      f_networkClient = new NetworkClient(address, f_parserObserver, f_worldObserver);
    } else if (port >= 0 && address != null) {
      f_networkClient = new NetworkClient(address, port, f_parserObserver, f_worldObserver);
    } else { // port >= 0 && address == null
      throw new NullPointerException("address cannot be null if the port was defined");
    }
    f_networkClient.start();
  }

  /**
   * Notify the parserObserver to display a list of players to the user
   */
  public void displayAvailablePlayers() {
    final String players = f_worldController.getAvailablePlayers();
    if (players.trim().length() > 0) {
      f_parserObserver.display(f_messages.getString(Messages.CHOOSE_PLAYER));
      f_parserObserver.display(players);
    } else {
      f_parserObserver.display(f_messages.getString(Messages.NO_PLAYERS_AVAILABLE));
    }
  }

  private void displayLoadFailure(String fileName, String exceptionMessage) {
    f_parserObserver.display(f_messages.getString(Messages.FILE_LOAD_FAILED).replace(Messages.FILENAME_TAG,
        fileName == null ? "null file name" : fileName).replace(Messages.MESSAGE_TAG, exceptionMessage));
  }

  private void displayLoadSuccess(String fileName) {
    f_parserObserver.display(f_messages.getString(Messages.FILE_LOADED).replace(Messages.FILENAME_TAG, fileName));
    displayAvailablePlayers();
  }

  private void displaySaveFailure(String fileName, String exceptionMessage) {
    f_parserObserver.display(f_messages.getString(Messages.FILE_SAVE_FAILED).replace(Messages.FILENAME_TAG,
        fileName == null ? "null file name" : fileName).replace(Messages.MESSAGE_TAG, exceptionMessage));
  }

  private void displaySaveSuccess(String fileName) {
    f_parserObserver.display(f_messages.getString(Messages.FILE_SAVED).replace(Messages.FILENAME_TAG, fileName));
  }

  private void dropItem(String[] newCommand) {
    if (newCommand.length > 1) {
      final Place location = f_player.getLocation();
      final Container destination = location.getContainer();
      final Container source = f_player.getContainer();
      final String itemName = newCommand[1];
      final Result result = f_worldController.swapItem(f_player, destination, source, itemName, Item.Action.DROP);
      switch (result) {
        case ITEM_NOT_FOUND:
          f_parserObserver.display(f_messages.getString(Messages.ITEM_NOT_FOUND).replace(Messages.ITEM_TAG, itemName));
          break;
        case UNKNOWN_FAILURE:
          f_parserObserver.display(f_messages.getString(Messages.UNKNOWN_FAILURE));
          break;
        default:
          // Do nothing
      }
    } else {
      f_parserObserver.display(f_messages.getString(Messages.MISSING_ITEM));
    }
  }

  private void equip(String[] newCommand) {
    if (f_player.getCharacterClass() == null) {
      f_parserObserver.display(f_messages.getString(Messages.CANT_EQUIP_MSG));
      return;
    }
    if (newCommand.length == 1) {
      f_parserObserver.display(f_messages.getString(Messages.MISSING_ITEM));
    } else if (newCommand.length > 1) {
      final Result result = f_worldController.equip(f_player, newCommand[1]);
      switch (result) {
        case CANT_USE_ITEM:
          f_parserObserver.display(f_messages.getString(Messages.CLASS_CANT_EQUIP_MSG).replace(Messages.CLASS_TAG,
              f_player.getCharacterClass().getName()));
          break;
        case ITEM_NOT_FOUND:
          f_parserObserver.display(f_messages.getString(Messages.MISSING_ITEM));
        default:
          break;
      }
    }
  }

  /**
   * Constructs a message containing some (hopefully useful) help to the user about what the game
   * commands do.
   *
   * @return A String of the Help Message
   */
  public String getHelpMessage() {
    final String helpMessageLineBreak = f_messages.getString(Messages.LONG_HELP_LINEBREAK);
    final String longHelp = f_messages.getString(Messages.LONG_HELP);
    return longHelp.replaceAll(Matcher.quoteReplacement(helpMessageLineBreak), TextUtilities.LINESEP2);
  }

  /**
   * Access the current player
   *
   * @return the current Player, or null if no player has been selected
   */
  public Player getPlayer() {
    return f_player;
  }

  private void giveItem(String item, String toPlayer) {
    final Result result = f_worldController.giveItem(item, f_player, toPlayer);
    switch (result) {
      case ITEM_NOT_FOUND:
        f_parserObserver.display(f_messages.getString(Messages.ITEM_NOT_FOUND).replace(Messages.ITEM_TAG,
            result.getReason()));
        break;
      case CHARACTER_NOT_FOUND:
        f_parserObserver.display(f_messages.getString(Messages.TALK_FAILED));
        break;
      case SUCCESS:
        break;
      default:
        f_parserObserver.display(f_messages.getString(Messages.UNKNOWN_FAILURE));
        break;
    }
  }

  private void giveMoney(String money, String toPlayer) {
    final Result result = f_worldController.giveMoney(money, f_player, toPlayer);
    switch (result) {
      case INSUFFICIENT_MONEY:
        f_parserObserver.display(f_messages.getString(Messages.INSUFFICIENT_MONEY));
        break;
      case CHARACTER_NOT_FOUND:
        f_parserObserver.display(f_messages.getString(Messages.TALK_FAILED));
        break;
      case SUCCESS:
        break;
      default:
        f_parserObserver.display(f_messages.getString(Messages.UNKNOWN_FAILURE));
        break;
    }
  }

  /**
   * "GO <direction>" command This direction section has been update since lab 3. We no longer
   * double parse a shortcut command. To implement the shortcut check if the one word command is a
   * direction and adjust for where the direction is in the array based on the number of words.
   *
   * @param words
   *          A string split on whitespace of the command the user typed
   */
  private void goCommand(String[] words) {
    if (words.length == 1 && (!Navigation.getInstance(words[0]).isPresent())) {
      f_parserObserver.display(f_messages.getString(Messages.WHERE).replace(Messages.COMMAND_TAG, words[0]) + " "
          + f_messages.getString(Messages.SHORT_HELP));
    } else {
      final Optional<Navigation> opt = Navigation.getInstance(words[words.length - 1]);

      if (opt.isPresent()) {
        if (f_worldController.canTravel(f_player, opt.get())) {
          f_worldController.travel(f_player, opt.get());
        } else if (f_worldController.isMissingItems(f_player, opt.get())) {
          // User was missing items and cannot enter
          final StringBuffer buffer = new StringBuffer();
          final Place newPlayerLocation = f_player.getLocation().getTravelDestinationToward(opt.get());
          final Set<Item> missingItems = newPlayerLocation.missingItems(f_player.getContainer());
          for (final Item item : missingItems) {
            buffer.append(item.getPlaceBlockedMessage(newPlayerLocation));
          }
          f_parserObserver.display(buffer.toString());
        } else {
          /*
           * Travel is not allowed from the player's location in the specified direction.
           */
          f_parserObserver.display(f_messages.getString(Messages.MOVE_DIRECTION_FAILED).replace(Messages.DIRECTION_TAG,
              opt.get().toString()));
        }
        currentEvents = null;
      } else {
        f_parserObserver.display(f_messages.getString(Messages.UNRECOGNIZED_DIRECTION).replace(Messages.DIRECTION_TAG,
            words[1])
            + " " + f_messages.getString(Messages.SHORT_HELP));
      }
    }
  }

  private void loadCommand(String fileName) {
    if (!checkAdmin()) {
      return;
    }
    try {
      if (fileName.length() == 0) {
        /**
         * Let the View know that we didn't get a fileName and give it a chance to prompt the user
         * with a JFileChooser or whatever is appropriate for the view
         */
        final Optional<File> file = f_parserObserver.load();
        if (file.isPresent()) {
          fileName = file.get().getName();
          f_worldController.loadWorld(file.get().getAbsolutePath());
          displayLoadSuccess(fileName);
        } else {
          f_parserObserver.display(f_messages.getString(Messages.LOAD_MESSAGE) + "  "
              + f_messages.getString(Messages.SHORT_HELP));
        }
      } else {
        f_worldController.loadWorld(fileName);
        displayLoadSuccess(fileName);
      }
    } catch (final PersistenceStateException e) {
      displayLoadFailure(fileName, e.getMessage());
    }
  }

  private void lookCommand() {
    f_parserObserver.display(f_player.getLocation().getFullLocationDescription(f_player, f_messages));
  }

  /**
   * Parses the user's command and sending appropriate messages to the {@link Controller}. Once the
   * users command is understood (i.e., parsed) a specific method is invoked on the controller. If
   * the parser couldn't understand the user's command an error message is sent.
   *
   * Will include the line separator by default
   *
   * @param command
   *          the users's command to the game.
   */
  public void parse(String command) {
    parse(command, true);
  }

  /**
   * Parses the user's command and sending appropriate messages to the {@link Controller}. Once the
   * users command is understood (i.e., parsed) a specific method is invoked on the controller. If
   * the parser couldn't understand the user's command an error message is sent.
   *
   * @param command
   *          the users's command to the game.
   * @param includeLineSep
   *          Set to true to put a space and line in the user display. Set to false for GUI
   *          interactions.
   */
  public void parse(String command, boolean includeLineSep) {
    if (f_networkClient == null) {
      try {
        parseLocal(command, includeLineSep);
      } catch (final IllegalStateException e) {
        f_worldController.getWorld().notifyException(e.toString());
      }
    } else {
      try {
        f_networkClient.parseCommand(command);
      } catch (final NetworkConnectionClosedException e) {
        f_networkClient = null;
        f_parserObserver.display(e.getMessage());
      }
    }
  }

  private void parseLocal(String command, boolean includeLineSep) {
    if (includeLineSep) {
      f_parserObserver.display(TextUtilities.LINESEP + "--------------------" + TextUtilities.LINESEP);
    }
    final String[] words = command.trim().toUpperCase().split("\\s");

    final String[] goCommands = f_messages.getString(Messages.GO_COMMAND).split(",");
    /*
     * The Collator for the current default locale. The default locale is determined by
     * java.util.Locale.getDefault.
     */
    final Collator myDefaultCollator = Collator.getInstance();

    /*
     * Several regular expressions for parsing commands, compiled before checking
     */
    final Pattern takeFromItemPattern = Pattern.compile(f_messages.getString(Messages.TAKE_FROM_ITEM_COMMAND)
        .toUpperCase(), Pattern.CASE_INSENSITIVE);
    final Matcher takeFromItemMatcher = takeFromItemPattern.matcher(command.toUpperCase());

    final Pattern putInItemPattern = Pattern.compile(f_messages.getString(Messages.PUT_IN_ITEM_COMMAND).toUpperCase(),
        Pattern.CASE_INSENSITIVE);
    final Matcher putInItemMatcher = putInItemPattern.matcher(command.toUpperCase());

    final Pattern giveItemToPattern = Pattern.compile(f_messages.getString(Messages.GIVE_ITEM_COMMAND),
        Pattern.CASE_INSENSITIVE);
    final Matcher giveItemToMatcher = giveItemToPattern.matcher(command.toUpperCase());

    final Pattern giveMoneyPattern = Pattern.compile(f_messages.getString(Messages.GIVE_MONEY_COMMAND).replace(
        Messages.XML_MONEY_NAME, f_worldController.getWorld().getMoneyName()), Pattern.CASE_INSENSITIVE);
    final Matcher giveMoneyMatcher = giveMoneyPattern.matcher(command.toUpperCase());

    /*
     * Only commands that are valid when no player is selected should be put above the if(f_player
     * == null) check
     */
    if (myDefaultCollator.equals(f_messages.getString(Messages.HELP_COMMAND), words[0])) {
      /*
       * "HELP" command
       */
      f_parserObserver.display(getHelpMessage());
    } else if (myDefaultCollator.equals(f_messages.getString(Messages.LOAD_COMMAND), words[0])) {
      /*
       * "LOAD <file>" command
       */
      final String fileName = command.substring(words[0].length()).trim();
      loadCommand(fileName);

    } else if (f_messages.getString(Messages.SOUND_OFF_COMMAND).equalsIgnoreCase(command)) {
      f_parserObserver.setSound(false);
    } else if (f_messages.getString(Messages.SOUND_ON_COMMAND).equalsIgnoreCase(command)) {
      f_parserObserver.setSound(true);
    } else if (f_messages.getString(Messages.SOUND_TOGGLE_COMMAND).equalsIgnoreCase(command)) {
      f_parserObserver.toggleSound();
    } else if (TextUtilities.match(f_messages.getString(Messages.QUIT_COMMANDS).split(","), words[0])) {
      /*
       * "QUIT" or "EXIT" or "BYE" command
       */
      if (f_player != null) {
        f_worldController.releasePlayer(f_player);
      }
      f_parserObserver.quit();
    } else if (command.toUpperCase().startsWith(f_messages.getString(Messages.CONNECT_COMMAND).toUpperCase())) {
      connectMultiplayerClient(command.substring(f_messages.getString(Messages.CONNECT_COMMAND).length()).trim());
    } else if (f_admin && myDefaultCollator.equals(f_messages.getString(Messages.SAVE_COMMAND), words[0])) {
      /*
       * "SAVE <file>" command
       */
      final String fileName = command.substring(words[0].length()).trim();
      saveCommand(fileName);
    } else if (myDefaultCollator.equals(f_messages.getString(Messages.LIST_PLAYERS_COMMAND), words[0])) {
      displayAvailablePlayers();
    } else if (command.toUpperCase().startsWith(f_messages.getString(Messages.START_SERVER_COMMAND).toUpperCase())) {
      startServer(command.substring(f_messages.getString(Messages.START_SERVER_COMMAND).length()).trim());
    } else if (command.toUpperCase().startsWith(f_messages.getString(Messages.STOP_SERVER_COMMAND).toUpperCase())) {
      stopServer();
    } else if (f_player == null
        || command.toUpperCase().startsWith(f_messages.getString(Messages.CHANGE_PLAYER_COMMAND).toUpperCase())) {
      /*
       * force the player to choose a player if they have not specified one and they didn't match
       * any valid command prior to this
       */
      changePlayer(command);
    } else if (!f_player.isConscious()) {
      f_parserObserver.display(f_messages.getString(Messages.KNOCKOUT_LOCK_MSG));
    } else if (myDefaultCollator.equals(f_messages.getString(Messages.ATTACK_CMD), words[0])) {
      if (f_worldController.getWorld().isCombatAllowed()) {
        /*
         * ATTACK <character>
         */
        final String[] newCommand = command.split("\\s", 2); // Re-split the command preserving
        // all
        // the spaces after the first word
        attack(newCommand);
      } else {
        f_worldController.getWorld().broadcastMessage(f_player, f_player.toString(), command);
      }
    } else if (myDefaultCollator.equals(f_messages.getString(Messages.EQUIP_CMD), words[0])) {
      if (f_worldController.getWorld().isCombatAllowed()) {
        /*
         * EQUIP <item>
         */
        final String[] newCommand = command.split("\\s", 2); // Re-split the command preserving
        // all
        // the spaces after the first word
        equip(newCommand);
      } else {
        f_worldController.getWorld().broadcastMessage(f_player, f_player.toString(), command);
      }
    } else if (myDefaultCollator.equals(f_messages.getString(Messages.REMOVE_CMD), words[0])) {

      remove();
    } else if (myDefaultCollator.equals(f_messages.getString(Messages.USE_CMD), words[0])) {
      final String[] newCommand = command.split("\\s", 2); // Re-split the command preserving all
      // the spaces after the first word
      use(newCommand);
    } else if (TextUtilities.match(goCommands, words[0])
        || (Navigation.getInstance(words[0]).isPresent() && words.length == 1)) {
      goCommand(words);
    }
    /**
     * Check to see if the input command equals the LOOK_COMMAND or just the first character
     */
    else if (myDefaultCollator.equals(f_messages.getString(Messages.LOOK_COMMAND), words[0])
        || myDefaultCollator.equals(f_messages.getString(Messages.LOOK_COMMAND).substring(0, 1), words[0])) {
      lookCommand();
    } else if (putInItemMatcher.matches()) {
      final String itemToPut = putInItemMatcher.group(1);
      final String itemContainerString = putInItemMatcher.group(2);
      if (!f_worldController.putItemInItemContainer(f_player, itemToPut, itemContainerString)) {
        f_parserObserver.display(f_messages.getString(Messages.MOVE_ITEM_FAILED));
      }
    }/*
      * Must put the takeFromItem check before the more simple take <item> command that is next
      * because take from item has similar but more complicated syntax and would match the next case
      * too
      */else if (takeFromItemMatcher.matches()) {
      final String itemToTake = takeFromItemMatcher.group(1);
      final String itemContainerString = takeFromItemMatcher.group(2);
      if (!f_worldController.takeItemFromItemContainer(f_player, itemToTake, itemContainerString)) {
        f_parserObserver.display(f_messages.getString(Messages.MOVE_ITEM_FAILED));
      }
    } else if (myDefaultCollator.equals(f_messages.getString(Messages.TAKE_COMMAND), words[0])) {
      /*
       * TAKE <item>
       */
      final String[] newCommand = command.split("\\s", 2); // Re-split the command preserving all
      // the spaces after the first word
      takeItem(newCommand);
    } else if (myDefaultCollator.equals(f_messages.getString(Messages.DROP_COMMAND), words[0])) {
      /*
       * DROP <item>
       */
      final String[] newCommand = command.split("\\s", 2); // Re-split the command preserving all
      // the spaces after the first word
      dropItem(newCommand);
    } else if (myDefaultCollator.equals(f_messages.getString(Messages.INVENTORY_COMMAND), words[0])) {
      f_parserObserver.display(f_player.getInventory(f_messages));
    } else if (myDefaultCollator.equals(f_messages.getString(Messages.SCORE_COMMAND), words[0])) {
      f_parserObserver.display(f_messages.getString(Messages.SCORE_MESSAGE).replace(Messages.SCORE_TAG,
          Long.toString(f_player.getScore())).replace(Messages.NAME_TAG, f_player.toString()));
    } else if (command.toUpperCase().startsWith(f_messages.getString(Messages.TALK_COMMAND).toUpperCase())) {
      final String name = command.substring(f_messages.getString(Messages.TALK_COMMAND).length()).trim();
      talkToCommand(name);
    } else if (currentEvents != null && words[0].matches("\\d+")) {
      try {
        final SayTrigger trigger = currentEvents.get(Integer.parseInt(words[0]) - 1);
        if (trigger.meetsConditions(f_player)) {
          try {
            trigger.execute(f_player);
          } catch (final IllegalStateException e) {
            f_parserObserver
                .display("There has been a fault detected in the state of the world.  "
                    + "Something was inconsistent in the world.xml file." + TextUtilities.LINESEP
                    + e.getLocalizedMessage());
          }
        } else {
          f_parserObserver.display(f_messages.getString(Messages.EVENT_CONDITION_FAILED) + " "
              + f_messages.getString(Messages.SHORT_HELP));
        }
        currentEvents = null;
      } catch (NumberFormatException | IndexOutOfBoundsException e) {
        f_parserObserver.display(f_messages.getString(Messages.UNRECOGNIZED_COMMAND).replace(Messages.COMMAND_TAG,
            command)
            + " " + f_messages.getString(Messages.SHORT_HELP));
      }
    } else if (myDefaultCollator.equals(f_messages.getString(Messages.INSPECT_COMMAND), words[0])) {
      final String name = command.substring(words[0].length()).trim();
      final pavlik.john.dungeoncrawl.model.Character character = f_player.getLocation().getCharacter(name);
      if (character != null) {
        f_parserObserver.display(character.getFullDescription(f_messages));
      } else {
        final Optional<Item> item = f_worldController.getItemForPlayer(f_player, name, false);
        if (item.isPresent()) {
          f_parserObserver.display(item.get().getFullDescription());
        } else {
          f_parserObserver.display(f_messages.getString(Messages.INSPECT_FAILED));
        }
      }
    } else if (myDefaultCollator.equals(f_messages.getString(Messages.OPEN_COMMAND), words[0])) {
      final String itemName = command.substring(words[0].length()).trim();
      final Optional<Item> item = f_worldController.getItemForPlayer(f_player, itemName, false);
      if (item.isPresent() && item.get().getContainer() != null) {
        f_parserObserver.display(f_messages.getString(Messages.ITEM_CONTAINS_MESSAGE).replace(Messages.ITEM_TAG,
            TextUtilities.describeItems(item.get().getContainer().getItems(), f_messages)));
      } else {
        f_parserObserver.display(f_messages.getString(Messages.OPEN_FAILED).replace(Messages.ITEM_TAG, itemName));
      }
    } else if (command.toUpperCase().startsWith(f_messages.getString(Messages.CREATE_ITEM_COMMAND).toUpperCase())) {
      combineItems(command.substring(f_messages.getString(Messages.CREATE_ITEM_COMMAND).length()).trim());
    } // Must check for money before item, as money is more specific and item matches both cases
    else if (giveMoneyMatcher.matches()) {
      giveMoney(giveMoneyMatcher.group(1), giveMoneyMatcher.group(2));
    } else if (giveItemToMatcher.matches()) {
      giveItem(giveItemToMatcher.group(1), giveItemToMatcher.group(2));
    } else {
      f_worldController.getWorld().broadcastMessage(f_player, f_player.toString(), command);
    }

  }

  private void remove() {
    if (f_player.getCurrentWeapon() != null) {
      f_worldController.equip(f_player, null);
      f_parserObserver.display(f_messages.getString(Messages.REMOVE_MSG));
    } else {
      f_parserObserver.display(f_messages.getString(Messages.NOTHING_TO_REMOVE_MSG));
    }
  }

  private void saveCommand(String fileName) {
    if (!checkAdmin()) {
      return;
    }
    try {
      if (fileName.length() == 0) {
        /**
         * Let the View know that we didn't get a fileName and give it a chance to prompt the user
         * with a JFileChooser or whatever is appropriate for the view
         */
        final Optional<File> file = f_parserObserver.save();
        if (file.isPresent()) {
          fileName = file.get().getName();
          f_worldController.saveWorld(file.get().getAbsolutePath());
          displaySaveSuccess(file.get().getAbsolutePath());
        } else {
          f_parserObserver.display(f_messages.getString(Messages.SAVE_MESSAGE) + "  "
              + f_messages.getString(Messages.SHORT_HELP));
        }
      } else {
        f_worldController.saveWorld(fileName);
        displaySaveSuccess(fileName);
      }
    } catch (final PersistenceStateException e) {
      displaySaveFailure(fileName, e.getMessage());
    }
  }

  /**
   * Shutdown all threads gracefully
   */
  public void shutdown() {
    f_worldController.stopWorld();
    if (f_multiplayer != null) {
      stopServer();
    }
    try {
      if (f_networkClient != null) {
        f_networkClient.finish();
        f_networkClient.join(1500);
      }
    } catch (final InterruptedException e) {
      e.printStackTrace();
    }
  }

  private void startServer(String substring) {
    if (!checkAdmin()) {
      return;
    }
    if (f_multiplayer == null) {
      try {
        final int port = Integer.parseInt(substring);
        f_multiplayer = new MultiplayerServerThread(port, f_worldController, f_messages);
      } catch (final NumberFormatException e) {
        f_multiplayer = new MultiplayerServerThread(f_worldController, f_messages);
      }

      f_multiplayer.start();
      f_parserObserver.display(f_messages.getString(Messages.SERVER_STARTED).replace(Messages.PORT_TAG,
          Integer.toString(f_multiplayer.getPort())));
    }
  }

  private void stopServer() {
    if (!checkAdmin()) {
      return;
    }
    if (f_multiplayer != null) {
      f_multiplayer.finish();
      try {
        f_multiplayer.join();
      } catch (final InterruptedException e) {
        e.printStackTrace();
      }
      f_multiplayer = null;
      f_parserObserver.display(f_messages.getString(Messages.SERVER_STOPPED));
    }
  }

  private void takeItem(String[] newCommand) {
    if (newCommand.length > 1) {
      final Container source = f_player.getLocation().getContainer();
      final Container destination = f_player.getContainer();
      final String itemName = newCommand[1];
      final Result result = f_worldController.swapItem(f_player, destination, source, itemName, Item.Action.TAKE);
      switch (result) {
        case ITEM_NOT_TAKEABLE:
          f_parserObserver.display(result.getReason());
          break;
        case ITEM_NOT_FOUND:
          f_parserObserver.display(f_messages.getString(Messages.ITEM_NOT_FOUND).replace(Messages.ITEM_TAG, itemName));
          break;
        case UNKNOWN_FAILURE:
          f_parserObserver.display(f_messages.getString(Messages.UNKNOWN_FAILURE));
          break;
        default:
          // Do nothing
      }
    } else {
      f_parserObserver.display(f_messages.getString(Messages.MISSING_ITEM));
    }
  }

  /**
   * Talk to an NPC
   *
   * @param name
   *          The name of the NPC
   */
  private void talkToCommand(String name) {
    final pavlik.john.dungeoncrawl.model.Character character = f_player.getLocation().getCharacter(name);
    final StringBuilder dialog = new StringBuilder();
    if (character == null) {
      dialog.append(f_messages.getString(Messages.TALK_FAILED));
    } else if (character instanceof Player && !character.equals(f_player)) {
      dialog.append(f_messages.getString(Messages.BROADCAST_PLAYERS));
    } else if (character instanceof Player) {
      dialog.append(f_messages.getString(Messages.TALK_TO_SELF));
    } else if (!character.isConscious()) {
      f_parserObserver.display(f_messages.getString(Messages.UNCONSCIOUS_MSG).replace(Messages.CHARACTER_TAG,
          character.getName()));
    } else if (character.inCombat()) {
      f_parserObserver.display(f_messages.getString(Messages.IN_COMBAT_MSG).replace(Messages.CHARACTER_TAG,
          character.getName()));
    } else if (character instanceof NonPlayerCharacter) /* then NPC is defined and pc is not */{
      currentEvents = f_worldController.talkToNPC(f_player, (NonPlayerCharacter) character);
    }
    if (dialog.length() > 0) {
      f_parserObserver.display(dialog.toString());
    }
  }

  private void use(String[] newCommand) {
    if (newCommand.length == 1) {
      f_parserObserver.display(f_messages.getString(Messages.MISSING_ITEM));
    } else {
      Result result = f_worldController.useItem(f_player, newCommand[1], f_player.getName());
      if (!result.equals(Result.SUCCESS)) {
        final Matcher m = Pattern.compile("(.+) on (.+)").matcher(newCommand[1].trim());
        while (m.find() && result.equals(Result.ITEM_NOT_FOUND)) {
          result = f_worldController.useItem(f_player, m.group(1), m.group(2));
        }
      }
      switch (result) {
        case CANT_USE_ITEM:
          f_parserObserver.display(f_messages.getString(Messages.CANT_USE_MSG));
          break;
        case ALREADY_IN_COMBAT:
          f_parserObserver.display(f_messages.getString(Messages.ITEM_TIMER_MSG).replace(Messages.TIME_TAG,
              result.getReason()));
          break;
        case ITEM_NOT_FOUND:
          f_parserObserver.display(f_messages.getString(Messages.MISSING_USE_ITEM));
          break;
        case SUCCESS:
          break;
        case ITEM_CONSUMED:
          f_parserObserver.display(f_messages.getString(Messages.ITEM_CONSUMED));
          break;
        default:
          f_parserObserver.display(f_messages.getString(Messages.UNKNOWN_FAILURE));
          break;
      }
    }
  }
}
