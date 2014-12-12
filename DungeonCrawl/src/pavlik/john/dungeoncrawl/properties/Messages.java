package pavlik.john.dungeoncrawl.properties;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * String constants are stored here for internationalization / dynamic loading so that they can be
 * accessed by any class
 *
 * @author John
 * @version 1.1
 * @since 1.1
 *
 */
public class Messages {
  /**
   * Internationalization option. Allows for strings to be loaded dynamically rather than hardcoded
   * into the application
   *
   * @param language
   *          An ISO 639 alpha-2 or alpha-3 language code, or a language subtag up to 8 characters
   *          in length. See the Locale class description about valid language values. Use "en" for
   *          English.
   *
   * @param country
   *          An ISO 3166 alpha-2 country code or a UN M.49 numeric-3 area code. See the Locale
   *          class description about valid country values. Use "US" for United States.
   * @return a ResourceBundle containing the messages loaded from the
   *         MessagesBundle_language_country.properties file.
   *
   */
  public static ResourceBundle loadMessages(String language, String country) {
    final Locale locale = new Locale(language, country);
    final ResourceBundle messages = ResourceBundle.getBundle("pavlik/john/dungeoncrawl/properties/MessagesBundle",
        locale);
    return messages;
  }

  /**
   * Public splash screen
   */
  public static final String SPLASH                 = "\n"
      + "  _________              .__  .__  __      __            .__       .___\n"
      + " /   _____/ _____ _____  |  | |  |/  \\    /  \\___________|  |    __| _/\n"
      + " \\_____  \\ /     \\\\__  \\ |  | |  |\\   \\/\\/   /  _ \\_  __ \\  |   / __ | \n"
      + " /        \\  Y Y  \\/ __ \\|  |_|  |_\\        (  <_> )  | \\/  |__/ /_/ | \n"
      + "/_______  /__|_|  (____  /____/____/\\__/\\  / \\____/|__|  |____/\\____ | \n"
      + "        \\/      \\/     \\/                \\/                         \\/";
  /**
   * Welcome messages / title bar
   */
  public static final String TITLE                  = "title";
  /**
   * Message label to prompt the user
   */
  public static final String MESSAGE_LABEL          = "message_label";
  /**
   * Label for the Send Button for when the user uses a mouse instead of the enter key
   */
  public static final String SEND_BUTTON            = "send";
  /**
   * Label for the console output in the swing application
   */
  public static final String LAST_RESPONSE          = "last_response";
  /**
   * Message displayed when the user wins the game
   */
  public static final String VICTORY                = "victory";
  /**
   * Message displayed when the user quits the game
   */
  public static final String QUIT                   = "quit";

  /**
   * Message to thank the user for playing, displayed in the title of the pop-up box when the user
   * quits or wins the game.
   */
  public static final String THANKS                 = "thanks";
  /**
   * A comma separated list of commands that allow the player to move/go
   */
  public static final String GO_COMMAND             = "go_commands";
  /**
   * The help command. HELP
   */
  public static final String HELP_COMMAND           = "help_command";
  /**
   * LOAD
   */
  public static final String LOAD_COMMAND           = "load_command";
  /**
   * LOOK. Also compares the first character of the command, allowing L as valid input.
   */
  public static final String LOOK_COMMAND           = "look_command";
  /**
   * A comma separated list of commands that allow the player to quit. QUIT, EXIT, BYE
   */
  public static final String QUIT_COMMANDS          = "quit_commands";
  /**
   * SAVE
   */
  public static final String SAVE_COMMAND           = "save_command";
  /**
   * Please type "help" if you need more help.
   */
  public static final String SHORT_HELP             = "simple_help_message";
  /**
   * where? You must specify a direction!
   */
  public static final String WHERE                  = "where";
  /**
   * is not a direction I recognize.
   */
  public static final String UNRECOGNIZED_DIRECTION = "unrecognized_direction";
  /**
   * You must specify a file name to save your game.
   */
  public static final String SAVE_MESSAGE           = "save_message";
  /**
   * You must specify a file name to load a saved game.
   */
  public static final String LOAD_MESSAGE           = "load_message";
  /**
   * Sorry, I don't understand what that command means:
   */
  public static final String UNRECOGNIZED_COMMAND   = "unrecognized_command";
  /**
   * The LONG_HELP is very long and requires line breaks. In order to allow Java to determine the
   * type of line break at runtime, a marker character is put in where the line breaks should go.
   * That character is determined by checking this LONG_HELP_LINEBREAK value. Currently it is left
   * bracket right bracket
   */
  public static final String LONG_HELP_LINEBREAK    = "full_help_message_linebreak";

  /**
   * The full help dialog. Check the LONG_HELP_LINEBREAK to determine where to insert LINESEP2
   * characters to break this up into paragraphs.
   */
  public static final String LONG_HELP              = "full_help_message";

  /**
   * The command to let a user pick up an item. "Take something"
   */
  public static final String TAKE_COMMAND           = "take_command";
  /**
   * The command to let a user drop an item. "Drop something"
   */
  public static final String DROP_COMMAND           = "drop_command";

  /**
   * The message to display if the user attempts to drop or take an item that cannot be identified.
   */
  public static final String MISSING_ITEM           = "missing_item";

  /**
   * Lets the user list their inventory
   */
  public static final String INVENTORY_COMMAND      = "inventory_command";
  /**
   * You do not have anything
   */
  public static final String INVENTORY_EMPTY        = "inventory_empty";
  /**
   * You have {items}.
   */
  public static final String INVENTORY_NOTEMPTY     = "inventory_notempty";
  /**
   * You dropped {item}.
   */
  public static final String DROP_ITEM              = "drop_item";
  /**
   * You picked up {item}.
   */
  public static final String TAKE_ITEM              = "take_item";
  /**
   * and scored {score} points
   */
  public static final String SCORE_POINTS           = "score_points";

  /**
   * Here you see
   */
  public static final String SEE_ITEM               = "see_item";
  /**
   * File "{fileName}" loaded
   */
  public static final String FILE_LOADED            = "file_loaded";

  /**
   * File "{fileName}" failed to load. Keeping current world.
   */
  public static final String FILE_LOAD_FAILED       = "file_load_failed";
  /**
   * Save file "{fileName}" created.
   */
  public static final String FILE_SAVED             = "file_saved";

  /**
   * Save to file "{fileName}" FAILED. You will not be able to load this world
   */
  public static final String FILE_SAVE_FAILED       = "file_save_failed";

  /**
   * Sorry, you can't move {direction} from here.
   */
  public static final String MOVE_DIRECTION_FAILED  = "move_direction_failed";

  /**
   * Your current score is {score}.
   */
  public static final String SCORE_MESSAGE          = "score_message";

  /**
   * SCORE
   */
  public static final String SCORE_COMMAND          = "score_command";
  /**
   * To the {direction} you see {place}.
   */
  public static final String TRAVEL_DIRECTION       = "travel_direction";
  /**
   * talk to
   */
  public static final String TALK_COMMAND           = "talk_command";
  /**
   * Talk to who? I didn't recognize that name.
   */
  public static final String TALK_FAILED            = "talk_failed";
  /**
   * What do you say to {name}?
   */
  public static final String TALK_PROMPT            = "talk_prompt";
  /**
   * Used for replacing tag values in other strings.
   */
  public static final String DIRECTION_TAG          = "{direction}";
  /**
   * Used for replacing tag values in other strings.
   */
  public static final String SCORE_TAG              = "{score}";
  /**
   * Used for replacing tag values in other strings.
   */
  public static final String FILENAME_TAG           = "{fileName}";
  /**
   * Used for replacing direction tag values in other strings.
   */
  public static final String ITEM_TAG               = "{item}";
  /**
   * Used for replacing direction tag values in other strings.
   */
  public static final String PLACE_TAG              = "{place}";
  /**
   * Used for replacing direction tag values in other strings.
   */
  public static final String COMMAND_TAG            = "{command}";

  /**
   * Used for replacing direction tag values in other strings.
   */
  public static final String NAME_TAG               = "{name}";
  /**
   * and. Used for building lists of things, e.g. item1 and item2.
   */
  public static final String AND                    = "and";
  /**
   * Sorry, you no longer meet the requirements to say that.
   */
  public static final String EVENT_CONDITION_FAILED = "event_condition_failed";
  /**
   * A command for inspecting an NPC
   */
  public static final String INSPECT_COMMAND        = "inspect_command";
  /**
   * Inspect who? I don't recognize that name.
   */
  public static final String INSPECT_FAILED         = "inspect_failed";
  /**
   * Use to replace {npc} tags in message strings.
   */
  public static final String NPC_TAG                = "{npc}";
  /**
   * There is {npc} here too.
   */
  public static final String SEE_NPC                = "see_npc";
  /**
   * Replace {money} tags
   */
  public static final String MONEY_TAG              = "{money}";
  /**
   * You have {money} {xml_money_name}.
   */
  public static final String MONEY                  = "money";
  /**
   * {name} is not responding to anything you say.
   */
  public static final String TALK_INCAPABLE         = "talk_incapable";
  /**
   * Used after opening an item. Provides: You see {item}.
   */
  public static final String ITEM_CONTAINS_MESSAGE  = "item_contains_message";
  /**
   * OPEN {item}
   */
  public static final String OPEN_COMMAND           = "open_command";
  /**
   * TAKE {item} from {item}. Built as a regular expression
   */
  public static final String TAKE_FROM_ITEM_COMMAND = "take_from_item_command";
  /**
   * PUT {item} in {item}. Built as a regular expression.
   */
  public static final String PUT_IN_ITEM_COMMAND    = "put_in_item_command";
  /**
   * What {item}? There is nothing here by that name.
   */
  public static final String OPEN_FAILED            = "open_failed";
  /**
   * {player} moved {item} from {source} to {destination}.
   */
  public static final String MOVE_ITEM_SUCCEEDED    = "move_item_succeeded";
  /**
   * That's not possible.
   */
  public static final String MOVE_ITEM_FAILED       = "move_item_failed";
  /**
   * Use for replacing {source} tags in other messages
   */
  public static final String SOURCE_TAG             = "{source}";
  /**
   * Use for replacing {destination} tags in other messages
   */
  public static final String DESTINATION_TAG        = "{destination}";
  /**
   * your inventory
   */
  public static final String PLAYER_INVENTORY       = "player_inventory";
  /**
   * You can talk to yourself if you want, but you don't need MainConsole to do it.
   */
  public static final String TALK_TO_SELF           = "talk_to_self";
  /**
   * Any thing you type that is not a valid command will be displayed to other players at this
   * location.
   */
  public static final String BROADCAST_PLAYERS      = "broadcast_players";
  /**
   * PLAYERS
   */
  public static final String LIST_PLAYERS_COMMAND   = "list_players_command";
  /**
   * PLAY AS
   */
  public static final String CHANGE_PLAYER_COMMAND  = "change_player_command";
  /**
   * Choose a player with the command "play as &lt;name&gt;":
   */
  public static final String CHOOSE_PLAYER          = "choose_player";
  /**
   * START SERVER &lt;port# optional&gt;
   */
  public static final String START_SERVER_COMMAND   = "start_server_command";
  /**
   * STOP SERVER
   */
  public static final String STOP_SERVER_COMMAND    = "stop_server_command";
  /**
   * Server running on port {port}.
   */
  public static final String SERVER_STARTED         = "server_started";
  /**
   * There are no player characters currently available.
   */
  public static final String NO_PLAYERS_AVAILABLE   = "no_players_available";
  /**
   * {port}
   */
  public static final String PORT_TAG               = "{port}";
  /**
   * {player}
   */
  public static final String PLAYER_TAG             = "{player}";
  /**
   * Sorry, but you do not have permission to do that.
   */
  public static final String NOT_ALLOWED            = "not_allowed";
  /**
   * {player} is talking to {npc}.
   */
  public static final String OTHER_PLAYER_TALKING   = "other_player_npc";
  /**
   * {player} just walked up coming from {place}.
   */
  public static final String PLAYER_ARRIVED         = "player_arrived";
  /**
   * {player} just walked off towards {place}.
   */
  public static final String PLAYER_DEPARTED        = "player_departed";
  /**
   * You lose {item}.
   */
  public static final String LOSE_ITEM              = "lose_item";
  /**
   * You gain {item}.
   */
  public static final String GAIN_ITEM              = "gain_item";
  /**
   * {player} stops moving and becomes very still.
   */
  public static final String PLAYER_QUIT            = "player_quit";
  /**
   * {player} starts moving around.
   */
  public static final String PLAYER_JOINED          = "player_joined";
  /**
   * {character} says "{message}".
   */
  public static final String BROADCAST              = "broadcast";
  /**
   * {character}
   */
  public static final String CHARACTER_TAG          = "{character}";
  /**
   * {message}
   */
  public static final String MESSAGE_TAG            = "{message}";
  /**
   * CONNECT
   */
  public static final String CONNECT_COMMAND        = "connect_command";
  /**
   * Unable to identify port number
   */
  public static final String PORT_PROBLEM           = "unable_to_identify_port";
  /**
   * The connection has been lost to the server
   */
  public static final String CONNECTION_CLOSED      = "connection_closed";
  /**
   * Server stopped.
   */
  public static final String SERVER_STOPPED         = "server_stopped";
  /**
   * COMBINE
   */
  public static final String CREATE_ITEM_COMMAND    = "create_item_command";
  /**
   * You do not have access to {item} or it does not exist.
   */
  public static final String ITEM_NOT_FOUND         = "item_not_found";
  /**
   * You were not able to combine those items.
   */
  public static final String NO_ITEM_CREATED        = "no_item_created";
  /**
   * GIVE {item} TO {player}
   */
  public static final String GIVE_ITEM_COMMAND      = "give_item_command";
  /**
   * GIVE {int+} {xml_money_name} TO {player}
   */
  public static final String GIVE_MONEY_COMMAND     = "give_money_command";
  /**
   * You weren't able to do that.
   */
  public static final String UNKNOWN_FAILURE        = "unknown_failure";
  /**
   * {XML_MONEY_NAME}
   */
  public static final String XML_MONEY_NAME         = "{xml_money_name}";
  /**
   * You don't have enough money to do that.
   */
  public static final String INSUFFICIENT_MONEY     = "insufficient_money";

  /**
   * Tag for replacing health messages with the actual value
   */
  public static final String HEALTH_TAG             = "{health}";

  /**
   * &lt;|HP:[color=blue]{health}[/color]|&gt;
   */
  public static final String HEALTH_PROMPT          = "health_prompt";

  /**
   * ATTACK
   */
  public static final String ATTACK_CMD             = "attack_command";

  /**
   * The correct syntax is: attack &lt;target&gt;
   */
  public static final String ATTACK_CMD_HELP        = "attack_command_help";

  /**
   * You're already attacking.
   */
  public static final String NO_MORE_ATTACK         = "no_more_attack";

  /**
   * {attacker}'s {combatmsg} hits {target} for [color=red]{effect}[/color] damage.
   */
  public static final String ATTACK_MSG             = "attack_message";

  /**
   * Used for replacing {attacker} strings
   */
  public static final String ATTACKER_TAG           = "{attacker}";

  /**
   * Used to replace {target} strings
   */
  public static final String TARGET_TAG             = "{target}";

  /**
   * used to replace {effect} strings
   */
  public static final String EFFECT_TAG             = "{effect}";

  /**
   * Used to replace {combatmsg} strings
   */
  public static final String COMBAT_MSG_TAG         = "{combatmsg}";

  /**
   * {character} is not here.
   */
  public static final String MISSING_CHARACTER      = "missing_character";

  /**
   * {attacker} knocked {target} out!
   */
  public static final String KNOCKOUT_MSG           = "knockout_message";

  /**
   * [color=blue]{character} woke up[/color].
   */
  public static final String WAKEUP_MSG             = "wakeup_message";

  /**
   * You can't do that, you're out cold!
   */
  public static final String KNOCKOUT_LOCK_MSG      = "knockout_lock_message";

  /**
   * EQUIP
   */
  public static final String EQUIP_CMD              = "equip_command";

  /**
   * {player} equips the {item}.
   */
  public static final String EQUIP_MSG              = "equip_message";

  /**
   * You don't have that weapon.
   */
  public static final String MISSING_WEAPON         = "missing_weapon";

  /**
   * You can't equip that.
   */
  public static final String CANT_EQUIP_MSG         = "cant_equip_message";

  /**
   * {class} can't equip that type of weapon.
   */
  public static final String CLASS_CANT_EQUIP_MSG   = "class_cant_equip_message";

  /**
   * Used to replace class tags
   */
  public static final String CLASS_TAG              = "{class}";

  /**
   * {attacker}'s {combatmsg} missed {target}.
   */
  public static final String MISS_MSG               = "miss_message";

  /**
   * You can't do that. {character} is out cold!
   */
  public static final String UNCONSCIOUS_MSG        = "unconscious_message";

  /**
   * You'll have to wait. {character} is busy trying to survive!
   */
  public static final String IN_COMBAT_MSG          = "in_combat_message";

  /**
   * USE
   */
  public static final String USE_CMD                = "use_command";

  /**
   * You can't use that item.
   */
  public static final String CANT_USE_MSG           = "cant_use_message";

  /**
   * You don't have that item.
   */
  public static final String MISSING_USE_ITEM       = "missing_use_item";

  /**
   * {character} heals for [color=green]{effect}[/color].
   */
  public static final String HEAL_MSG               = "heal_message";

  /**
   * You can't do that yet.
   */
  public static final String ITEM_TIMER_MSG         = "item_timer_message";

  /**
   * That character cannot be attacked.
   */
  public static final String NO_CLASS_MSG           = "no_class_message";

  /**
   * UNEQUIP
   */
  public static final String REMOVE_CMD             = "remove_command";

  /**
   * You remove your current weapon
   */
  public static final String REMOVE_MSG             = "remove_message";

  /**
   * You don't have a weapon equipped
   */
  public static final String NOTHING_TO_REMOVE_MSG  = "nothing_to_remove_message";
  /**
   * CLEAR
   */
  public static final String CLEAR_COMMAND          = "clear_command";
  /**
   * SOUND OFF
   */
  public static final String SOUND_OFF_COMMAND      = "sound_off_command";
  /**
   * SOUND ON
   */
  public static final String SOUND_ON_COMMAND       = "sound_on_command";
  /**
   * Time tag
   */
  public static final String TIME_TAG               = "{time}";
  /**
   * That item has been expended and can no longer be used.
   */
  public static final String ITEM_CONSUMED          = "item_consumed";
  /**
   * You have been transported back to your respawn point at {place}.
   */
  public static final String RESPAWN                = "respawn";

  /**
   * {character} has disappeared.
   */
  public static final String CHARACTER_DISAPPEARED  = "character_disappeared";

  /**
   * {character} has appeared.
   */
  public static final String CHARACTER_APPEARED     = "character_appeared";
  /**
   * SOUND
   */
  public static final String SOUND_TOGGLE_COMMAND   = "sound_toggle_command";
  /**
   * {item} is currently equipped as your primary weapon.
   */
  public static final String CURRENT_EQUIPPED       = "current_equipped";

}
