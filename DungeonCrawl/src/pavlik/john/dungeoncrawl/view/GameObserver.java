package pavlik.john.dungeoncrawl.view;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;

import pavlik.john.dungeoncrawl.model.Character;
import pavlik.john.dungeoncrawl.model.IModelObserver;
import pavlik.john.dungeoncrawl.model.Item;
import pavlik.john.dungeoncrawl.model.NonPlayerCharacter;
import pavlik.john.dungeoncrawl.model.Place;
import pavlik.john.dungeoncrawl.model.Player;
import pavlik.john.dungeoncrawl.model.Weapon;
import pavlik.john.dungeoncrawl.model.events.triggers.SayTrigger;
import pavlik.john.dungeoncrawl.properties.Messages;
import pavlik.john.dungeoncrawl.view.parser.IDisplayNotifier;
import pavlik.john.dungeoncrawl.view.parser.Parser;

/**
 * An partial implementation of IDisplayNotifier and IModelObserver that factors out the common code
 * between graphical and command-line text-based interfaces. Subclasses "fill in the blanks" by
 * implementing the abstract methods.
 *
 * @author Robert Graham
 * @author John Pavlik
 *
 * @version 1.2
 */
public abstract class GameObserver implements IDisplayNotifier, IModelObserver {
  ResourceBundle      f_messages;
  protected Player    f_player;
  Parser   f_parser;

  Map<String, Clip>   f_cachedSoundEffects = new HashMap<>();
  boolean             f_soundEnabled       = true;
  // Retrieve the user preference node
  final Preferences   f_preferences;

  // Preference key name
  final static String SOUND_PREFERENCE_KEY = "play_sound";

  /**
   * Creates a new instance of GameObserver
   *
   * @param messages
   *          The ResourceBundle of messages instantiated by the View class
   */
  public GameObserver(ResourceBundle messages) {
    f_messages = Objects.requireNonNull(messages, "GameObserver must not receive a null ResourceBundle");
    f_preferences = Preferences.userNodeForPackage(GameObserver.class);
    loadSoundPreference();
  }

  @Override
  public void broadcastMessage(Character player, String speaker, String command) {
    if (player == null || player.getName().equals("WORLD")
        || (f_player.sameLocationAs(player) && (f_player.isConscious()))) {
      display(f_messages.getString(Messages.BROADCAST).replace(Messages.CHARACTER_TAG, speaker).replace(
          Messages.MESSAGE_TAG, command));
    }
  }

  @Override
  public void characterEquippedWeapon(Character character, Weapon weapon) {
    if (f_player.sameLocationAs(character) && weapon != null) {
      display(f_messages.getString(Messages.EQUIP_MSG).replace(Messages.ITEM_TAG, weapon.getName()).replace(
          Messages.PLAYER_TAG, character.toString()));
    }
  }

  @Override
  public void characterHeal(Character character, int heal) {
    if ((f_player.getLocation().equals(character.getLocation())) && (f_player.isConscious())) {
      display(f_messages.getString(Messages.HEAL_MSG).replace(Messages.CHARACTER_TAG, character.getName()).replace(
          Messages.EFFECT_TAG, Integer.toString(heal)));
    }
  }

  @Override
  public void characterHitsCharacterFor(Character attacker, String combatMsg, Character target, int damage) {
    if ((f_player.sameLocationAs(attacker)) && (f_player.isConscious())) {
      display(f_messages.getString(Messages.ATTACK_MSG).replace(Messages.ATTACKER_TAG, attacker.getName()).replace(
          Messages.TARGET_TAG, target.getName()).replace(Messages.COMBAT_MSG_TAG, combatMsg).replace(
              Messages.EFFECT_TAG, Integer.toString(damage)));
      playSound(attacker.getCurrentWeapon().getHitSound());
    }
  }

  @Override
  public void characterMissedCharacter(Character attacker, String combatMsg, Character target) {
    if ((f_player.sameLocationAs(attacker)) && (f_player.isConscious())) {
      display(f_messages.getString(Messages.MISS_MSG).replace(Messages.ATTACKER_TAG, attacker.getName()).replace(
          Messages.TARGET_TAG, target.getName()).replace(Messages.COMBAT_MSG_TAG, combatMsg));
      playSound(attacker.getCurrentWeapon().getMissSound());
    }
  }

  // private String f_gamePath;

  @Override
  public void characterMoneyChanged(Character fromCharacter, int money, String moneyName) {
    if ((f_player.sameLocationAs(fromCharacter)) && (f_player.isConscious())) {
      if (money >= 0) {
        display(f_messages.getString(Messages.GAIN_ITEM).replace(Messages.ITEM_TAG,
            Integer.toString(money) + " " + f_player.getLocation().getWorld().getMoneyName()).replace(
                Messages.PLAYER_TAG, fromCharacter.toString()));
      } else {
        display(f_messages.getString(Messages.LOSE_ITEM).replace(Messages.ITEM_TAG,
            Integer.toString(-money) + " " + f_player.getLocation().getWorld().getMoneyName()).replace(
                Messages.PLAYER_TAG, fromCharacter.toString()));
      }
    }
  }

  @Override
  public void characterPutItemOnGround(Character character, Item item, Long pointsAdded) {
    if ((f_player.sameLocationAs(character)) && (f_player.isConscious())) {
      display(f_messages.getString(Messages.DROP_ITEM).replace(Messages.CHARACTER_TAG, character.getName()).replace(
          Messages.ITEM_TAG, item.getName()));
    }
    if (f_player.equals(character)) {
      displayPoints(f_player, pointsAdded);
    }
  }

  @Override
  public void characterRespawnCountdown(Character character, int ticksRemaining) {
    if (f_player.equals(character) && !f_player.isConscious()) {
      display(Integer.toString(ticksRemaining));
    }
  }

  @Override
  public void characterRespawned(Character character, Place previousLocation, Place newLocation) {
    if (previousLocation.equals(newLocation)) {
      return;
    }
    if (f_player.equals(character)) {
      display(f_messages.getString(Messages.RESPAWN).replace(Messages.PLACE_TAG, newLocation.toString()));
    } else if (f_player.getLocation().equals(previousLocation)) {
      display(f_messages.getString(Messages.CHARACTER_DISAPPEARED)
          .replace(Messages.CHARACTER_TAG, character.toString()));
    } else if (f_player.getLocation().equals(newLocation)) {
      display(f_messages.getString(Messages.CHARACTER_APPEARED).replace(Messages.CHARACTER_TAG, character.toString()));
    }
  }

  @Override
  public void characterTookMoney(Character fromCharacter, Character toCharacter, int money, String moneyName) {
    characterMoneyChanged(fromCharacter, -money, moneyName);
    characterMoneyChanged(toCharacter, money, moneyName);
  }

  @Override
  public void characterUnequippedWeapon(Character character) {
    // Do nothing
  }

  @Override
  public void characterWokeUp(Character character) {
    if ((f_player.sameLocationAs(character)) && (f_player.isConscious())) {
      display(f_messages.getString(Messages.WAKEUP_MSG).replace(Messages.CHARACTER_TAG, character.getName()));
    }
  }

  @Override
  public void characterWonFight(Character attacker, Character target) {
    if ((f_player.equals(target)) || ((f_player.sameLocationAs(attacker)) && (f_player.isConscious()))) {
      display(f_messages.getString(Messages.KNOCKOUT_MSG).replace(Messages.ATTACKER_TAG, attacker.getName()).replace(
          Messages.TARGET_TAG, target.getName()));
    }
  }

  private void displayPoints(Player player, Long pointsAdded) {
    if (pointsAdded != 0) {
      display(f_messages.getString(Messages.SCORE_POINTS).replace(Messages.SCORE_TAG, pointsAdded.toString()).replace(
          Messages.NAME_TAG, player.toString()));
    }
  }

  /**
   * Load the sound preference from last user choice
   */
  public void loadSoundPreference() {
    // Get the value of the preference;
    // default value is returned if the preference does not exist
    f_soundEnabled = f_preferences.getBoolean(SOUND_PREFERENCE_KEY, true); // "a string"
    setSound(f_soundEnabled);
  }

  @Override
  public void playerGainsItem(Character player, Item item) {
    if ((f_player.sameLocationAs(player)) && (f_player.isConscious())) {
      display(f_messages.getString(Messages.GAIN_ITEM).replace(Messages.ITEM_TAG, item.toString()).replace(
          Messages.PLAYER_TAG, player.toString()));
    }
  }

  @Override
  public void playerJoined(Player player) {
    if ((f_player != null && f_player.sameLocationAs(player)) && (f_player.isConscious())) {
      display(f_messages.getString(Messages.PLAYER_JOINED).replace(Messages.PLAYER_TAG, player.toString()));
    }
  }

  @Override
  public void playerLosesItem(Character player, Item item) {
    if ((f_player.sameLocationAs(player)) && (f_player.isConscious())) {
      display(f_messages.getString(Messages.LOSE_ITEM).replace(Messages.ITEM_TAG, item.toString()).replace(
          Messages.PLAYER_TAG, player.toString()));
    }
  }

  @Override
  public void playerMoved(Player player, Place startLocation, Place finishLocation) {
    if ((f_player.equals(player)) && (f_player.isConscious())) {
      display(finishLocation.getFullLocationDescription(f_player, f_messages));
      playSound(finishLocation.getSound());
    } else if (f_player.sameLocationAs(player)) {
      display(f_messages.getString(Messages.PLAYER_ARRIVED).replace(Messages.PLAYER_TAG, player.toString()).replace(
          Messages.PLACE_TAG, startLocation.toString()));
    } else if (f_player.getLocation().equals(startLocation)) {
      display(f_messages.getString(Messages.PLAYER_DEPARTED).replace(Messages.PLAYER_TAG, player.toString()).replace(
          Messages.PLACE_TAG, finishLocation.toString()));
    }
  }

  @Override
  public void playerPutItemInItem(Player player, Item itemMoved, Item itemContainer) {
    if ((f_player.sameLocationAs(player)) && (f_player.isConscious())) {
      display(f_messages.getString(Messages.MOVE_ITEM_SUCCEEDED).replace(Messages.SOURCE_TAG,
          f_messages.getString(Messages.PLAYER_INVENTORY)).replace(Messages.DESTINATION_TAG, itemContainer.toString())
          .replace(Messages.ITEM_TAG, itemMoved.toString()).replace(Messages.PLAYER_TAG, player.toString()));
    }
  }

  @Override
  public void playerQuit(Player player) {
    if (f_player.equals(player)) {
      // Do nothing, their view already knows
    } else if ((f_player.sameLocationAs(player)) && (f_player.isConscious())) {
      display(f_messages.getString(Messages.PLAYER_QUIT).replace(Messages.PLAYER_TAG, player.toString()));
    }
  }

  @Override
  public void playerTalkedToNPC(Player player, NonPlayerCharacter npc, List<SayTrigger> validSayTriggers) {
    if (f_player.equals(player)) {
      final StringBuilder dialog = new StringBuilder();
      if (validSayTriggers.isEmpty()) {
        dialog.append(f_messages.getString(Messages.TALK_INCAPABLE).replace(Messages.NAME_TAG, npc.toString()));
      } else {
        dialog.append(f_messages.getString(Messages.TALK_PROMPT).replace(Messages.NAME_TAG, npc.toString())
            + TextUtilities.LINESEP);
        int count = 1;
        for (final SayTrigger trigger : validSayTriggers) {
          if (trigger.meetsConditions(f_player)) {
            dialog.append(count++ + "." + trigger.getMessage() + TextUtilities.LINESEP);
          }
        }
      }
      display(dialog.toString());
    } else if ((f_player.sameLocationAs(player)) && (f_player.isConscious())) {
      display(f_messages.getString(Messages.OTHER_PLAYER_TALKING).replace(Messages.PLAYER_TAG, player.toString())
          .replace(Messages.NPC_TAG, npc.toString()));
    }
  }

  @Override
  public void playerTookItemFromGround(Player player, Item item, Long pointsAdded) {
    if ((f_player.sameLocationAs(player)) && (f_player.isConscious())) {
      display(f_messages.getString(Messages.TAKE_ITEM).replace(Messages.ITEM_TAG, item.toString()).replace(
          Messages.PLAYER_TAG, player.toString()));
    }
    if (f_player.equals(player)) {
      displayPoints(player, pointsAdded);
    }
  }

  @Override
  public void playSound(Character cause, String soundPath) {
    if ((f_player.sameLocationAs(cause)) && (f_player.isConscious())) {
      playSound(soundPath);
    }
  }

  private void playSound(String file) {
    if (!f_soundEnabled || file == null) {
      return;
    }
    try {
      Clip clip = f_cachedSoundEffects.get(file);
      if (clip == null) {
        final Path yourFile = FileSystems.getDefault().getPath(file);
        AudioInputStream stream;
        if (!Files.exists(yourFile)) {
          stream = AudioSystem.getAudioInputStream(this.getClass().getResource(file));
        } else {
          stream = AudioSystem.getAudioInputStream(yourFile.toFile());
        }
        final AudioFormat format = stream.getFormat();
        final DataLine.Info info = new DataLine.Info(Clip.class, format);
        clip = (Clip) AudioSystem.getLine(info);
        clip.open(stream);
        f_cachedSoundEffects.put(file, clip);
      }
      clip.setFramePosition(0);
      clip.start();
    } catch (final Exception e) {
      display("Unable to play sound file: " + file);
    }
  }

  /**
   * Set the current player for the user
   *
   * @param player
   *          The current player
   */
  @Override
  public void setCurrentPlayer(Player player) {
    f_player = player;
  }

  /**
   * Enable or Disable sound effects
   *
   * @param enabled
   *          true if sound effects should play
   */
  @Override
  public void setSound(boolean enabled) {
    // Set the value of the preference
    f_preferences.putBoolean(SOUND_PREFERENCE_KEY, enabled);
    f_soundEnabled = enabled;

    if (!enabled) {
      for (final Clip clip : f_cachedSoundEffects.values()) {
        if (clip.isActive()) {
          clip.stop();
        }
      }
    }
  }

  @Override
  public void takeitemFromItem(Player player, Item itemMoved, Item itemContainer) {
    if ((f_player.sameLocationAs(player)) && (f_player.isConscious())) {
      display(f_messages.getString(Messages.MOVE_ITEM_SUCCEEDED).replace(Messages.SOURCE_TAG, itemContainer.toString())
          .replace(Messages.DESTINATION_TAG, f_messages.getString(Messages.PLAYER_INVENTORY)).replace(
              Messages.ITEM_TAG, itemMoved.toString()).replace(Messages.PLAYER_TAG, player.toString()));
    }
  }

  /**
   * Toggle sound off or on
   */
  @Override
  public void toggleSound() {
    setSound(!f_soundEnabled);
  }

}
