package pavlik.john.dungeoncrawl.persistence;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import pavlik.john.dungeoncrawl.exceptions.PersistenceStateException;
import pavlik.john.dungeoncrawl.model.Character;
import pavlik.john.dungeoncrawl.model.CharacterClass;
import pavlik.john.dungeoncrawl.model.Consumable;
import pavlik.john.dungeoncrawl.model.Container;
import pavlik.john.dungeoncrawl.model.Navigation;
import pavlik.john.dungeoncrawl.model.Item;
import pavlik.john.dungeoncrawl.model.NonPlayerCharacter;
import pavlik.john.dungeoncrawl.model.Place;
import pavlik.john.dungeoncrawl.model.Player;
import pavlik.john.dungeoncrawl.model.Weapon;
import pavlik.john.dungeoncrawl.model.Universe;
import pavlik.john.dungeoncrawl.model.Consumable.Effect;
import pavlik.john.dungeoncrawl.model.Consumable.EffectTarget;
import pavlik.john.dungeoncrawl.model.Consumable.EffectType;
import pavlik.john.dungeoncrawl.model.events.Action;
import pavlik.john.dungeoncrawl.model.events.Conditional;
import pavlik.john.dungeoncrawl.model.events.Event;
import pavlik.john.dungeoncrawl.model.events.State;
import pavlik.john.dungeoncrawl.model.events.Trigger;
import pavlik.john.dungeoncrawl.model.events.XMLAttribute;
import pavlik.john.dungeoncrawl.model.events.actions.AdjustMoneyAction;
import pavlik.john.dungeoncrawl.model.events.actions.AttackCharacterAction;
import pavlik.john.dungeoncrawl.model.events.actions.GiveItemAction;
import pavlik.john.dungeoncrawl.model.events.actions.MessageAction;
import pavlik.john.dungeoncrawl.model.events.actions.PlaySoundAction;
import pavlik.john.dungeoncrawl.model.events.actions.SetNPCLocationAction;
import pavlik.john.dungeoncrawl.model.events.actions.SetNPCStateAction;
import pavlik.john.dungeoncrawl.model.events.actions.TakeItemAction;
import pavlik.john.dungeoncrawl.model.events.conditionals.NPCHasItemConditional;
import pavlik.john.dungeoncrawl.model.events.conditionals.PlayerHasItemConditional;
import pavlik.john.dungeoncrawl.model.events.conditionals.PlayerHasMoneyConditional;
import pavlik.john.dungeoncrawl.model.events.triggers.AttackedTrigger;
import pavlik.john.dungeoncrawl.model.events.triggers.HealthTrigger;
import pavlik.john.dungeoncrawl.model.events.triggers.SayTrigger;
import pavlik.john.dungeoncrawl.model.events.triggers.SightTrigger;
import pavlik.john.dungeoncrawl.model.events.triggers.TimeTrigger;

/**
 * The persistence capability for the game. Uses DOM parsing. Separate layer, not part of MVC.
 * Called from Controller.
 *
 * @author John Pavlik
 *
 * @version 1.2
 */
public class GamePersistence {

  private static Set<Action> buildActions(Node eventElement, Universe world, String npcName, Container npcInventory) {
    final Set<Action> actions = new HashSet<>();
    {
      final String response = getAttributeValue(eventElement, RESPONSE_TAG);
      if (response != null) {
        actions.add(new MessageAction(response, npcName, RESPONSE_TAG));
      }
    }
    {
      final String takeItem = getAttributeValue(eventElement, TAKE_ITEM_TAG);
      if (takeItem != null) {
        final String[] items = takeItem.split(",");
        for (final String item : items) {
          actions.add(new TakeItemAction(npcName, item.trim(), TAKE_ITEM_TAG));
        }
      }
    }
    {
      final String giveItem = getAttributeValue(eventElement, GIVE_ITEM_TAG);
      if (giveItem != null) {
        final String[] items = giveItem.split(",");
        for (final String item : items) {
          actions.add(new GiveItemAction(npcName, item.trim(), GIVE_ITEM_TAG));
        }
      }
    }
    {
      final String payMoney = getAttributeValue(eventElement, PAY_MONEY_TAG);
      if (payMoney != null) {
        actions.add(new AdjustMoneyAction(payMoney, false, PAY_MONEY_TAG));
      }
    }
    {
      final String acceptMoney = getAttributeValue(eventElement, ACCEPT_MONEY_TAG);
      if (acceptMoney != null) {
        actions.add(new AdjustMoneyAction(acceptMoney, true, ACCEPT_MONEY_TAG));
      }
    }
    {
      final String location = getAttributeValue(eventElement, SET_NPC_LOCATION_TAG);
      if (location != null) {
        actions.add(new SetNPCLocationAction(world, npcName, location, SET_NPC_LOCATION_TAG));
      }
    }
    {
      final String attackCharacter = getAttributeValue(eventElement, ATTACK_CHARACTER_TAG);
      if (attackCharacter != null) {
        actions.add(new AttackCharacterAction(npcName, attackCharacter, ATTACK_CHARACTER_TAG));
      }
    }
    {
      final String sound = getAttributeValue(eventElement, SOUND_TAG);
      if (sound != null) {
        actions.add(new PlaySoundAction(world, sound, SOUND_TAG));
      }
    }
    {
      final String newState = eventElement.getTextContent();
      if (newState != null && newState.trim().length() > 0) {
        actions.add(new SetNPCStateAction(world, npcName, newState, null));
      }
    }
    return actions;
  }

  private static Set<Conditional> buildConditionals(Node eventElement, Universe world, String npcName) {
    final Set<Conditional> conditionals = new HashSet<>();
    {
      final String playerHasItem = getAttributeValue(eventElement, PLAYER_HAS_ITEM_TAG);
      if (playerHasItem != null) {
        final String[] items = playerHasItem.split(",");
        for (final String item : items) {
          conditionals.add(new PlayerHasItemConditional(item.trim(), PLAYER_HAS_ITEM_TAG));
        }
      }
    }
    {
      final String playerHasMoney = getAttributeValue(eventElement, PLAYER_HAS_MONEY_TAG);
      if (playerHasMoney != null) {
        conditionals.add(new PlayerHasMoneyConditional(Integer.parseInt(playerHasMoney), PLAYER_HAS_MONEY_TAG));
      }
    }
    {
      final String npcHasItem = getAttributeValue(eventElement, NPC_HAS_ITEM_TAG);
      if (npcHasItem != null) {
        final String[] items = npcHasItem.split(",");
        for (final String item : items) {
          conditionals.add(new NPCHasItemConditional(world, npcName, item.trim(), NPC_HAS_ITEM_TAG));
        }
      }
    }
    return conditionals;
  }

  private static Element createClassXML(CharacterClass cClass, Element combatElement, Document dom) {
    final Element classElement = dom.createElement(CLASS_TAG);
    classElement.setAttribute(NAME_TAG, cClass.getName());
    classElement.setAttribute(DEFAULT_WEAPON_NAME_TAG, cClass.getDefaultWeapon().getName());
    classElement.setAttribute(USE_MESSAGE_TAG, cClass.getDefaultWeapon().getUseString());
    classElement.setAttribute(EFFECT_TAG, (Integer.toString(cClass.getDefaultWeapon().getNumDice()) + "d" + Integer
        .toString(cClass.getDefaultWeapon().getNumSides())));
    classElement.setAttribute(HIT_TAG, Integer.toString(cClass.getDefaultWeapon().getHitChance()));
    classElement.setAttribute(KO_RECOVER_TAG, Integer.toString(cClass.getKORecovery()));
    classElement.setAttribute(TARGET_TAG, cClass.getDefaultWeapon().getEffectTarget().toString());
    classElement.setAttribute(COOLDOWN_TAG, Integer.toString(cClass.getDefaultWeapon().getCooldown()));
    if (cClass.getDefaultWeapon().getMissSound() != null) {
      classElement.setAttribute(MISS_SOUND_TAG, cClass.getDefaultWeapon().getMissSound());
    }
    if (cClass.getDefaultWeapon().getHitSound() != null) {
      classElement.setAttribute(HIT_SOUND_TAG, cClass.getDefaultWeapon().getHitSound());
    }
    combatElement.appendChild(classElement);
    final Element hpElement = dom.createElement(HP_TAG);
    hpElement.setAttribute(MAX_TAG, Integer.toString(cClass.getMaxHealth()));
    hpElement.setAttribute(REGEN_TAG, Integer.toString(cClass.getHealthRegen()));
    classElement.appendChild(hpElement);
    for (final String weapon : cClass.getWeaponTypes()) {
      final Element itemElement = dom.createElement(WEAPON_TAG);
      itemElement.setAttribute(TYPE_TAG, weapon);
      classElement.appendChild(itemElement);
    }
    return combatElement;
  }

  private static Element createItemXML(Universe world, Item item, String locationName, Document dom) {
    Element itemElement = null;
    if (item instanceof Weapon) {
      itemElement = dom.createElement(WEAPON_TAG);
    } else if (item instanceof Consumable) {
      itemElement = dom.createElement(CONSUMABLE_TAG);
    } else {
      itemElement = dom.createElement(ITEM_TAG);
    }
    itemElement.setAttribute(NAME_TAG, item.getName());
    itemElement.setAttribute(ARTICLE_TAG, item.getArticle());
    itemElement.setAttribute(DESCRIPTION_TAG, item.getDescription());
    if (locationName != null) {
      itemElement.setAttribute(LOCATION_TAG, locationName);
    }
    itemElement.setAttribute(TAKEPOINTS_TAG, item.getTakePoints().toString());
    itemElement.setAttribute(DROPPOINTS_TAG, item.getDropPoints().toString());
    itemElement.setAttribute(TAKEABLE_TAG, item.isTakeable() ? "Y" : "N");
    itemElement.setAttribute(CANTTAKE_TAG, item.cantTakeMessage());
    itemElement.setAttribute(IS_CONTAINER_TAG, item.getContainer() != null ? "Y" : "N");
    for (final Entry<Place, Long> entry : item.getDropPointsPlaces().entrySet()) {
      final Place place = entry.getKey();
      final Long points = entry.getValue();
      final Element dropPlace = dom.createElement(PLACE_TAG);
      itemElement.appendChild(dropPlace);
      dropPlace.setAttribute(DROPPOINTS_TAG, points.toString());
      dropPlace.setTextContent(place.getName());
    }
    for (final Entry<Place, String> entry : item.getPlaceBlockedMessages().entrySet()) {
      final Place place = entry.getKey();
      final String message = entry.getValue();
      final Element blockPlace = dom.createElement(PLACE_TAG);
      itemElement.appendChild(blockPlace);
      blockPlace.setAttribute(BLOCKEDMSG_TAG, message);
      blockPlace.setTextContent(place.getName());
    }

    if (item instanceof Consumable) {
      final Consumable consumable = (Consumable) item;
      itemElement.setAttribute(EFFECT_TAG, (Integer.toString(consumable.getNumDice()) + "d" + Integer
          .toString(consumable.getNumSides())));
      itemElement.setAttribute(EFFECTTYPE_TAG, consumable.getEffectType().toString());
      itemElement.setAttribute(COOLDOWN_TAG, Integer.toString(consumable.getCooldown()));
      itemElement.setAttribute(USES_TAG, consumable.getUsesRemaining().toString());
      itemElement.setAttribute(TYPE_TAG, consumable.getType());
      itemElement.setAttribute(USE_MESSAGE_TAG, consumable.getUseString());
      itemElement.setAttribute(TARGET_TAG, consumable.getEffectTarget().toString());
      itemElement.setAttribute(HIT_TAG, Integer.toString(consumable.getHitChance()));
      if (consumable.getHitSound() != null) {
        itemElement.setAttribute(HIT_SOUND_TAG, consumable.getHitSound());
      }
      if (consumable.getMissSound() != null) {
        itemElement.setAttribute(MISS_SOUND_TAG, consumable.getMissSound());
      }
      if (locationName != null) {
        final pavlik.john.dungeoncrawl.model.Character character = world.getCharacter(locationName);
        if (character != null && character.getCurrentWeapon() == item) {
          itemElement.setAttribute(EQUIP_TAG, "Y");
        }
      }
    }
    return itemElement;
  }

  /**
   * Creates an XML tree for a {@link NonPlayerCharacter}.
   *
   * @param npc
   *          the non player character
   * @return the constructed XML tree.
   */
  private static Element createNpcXML(NonPlayerCharacter npc, Document dom) {
    final Element npcElement = dom.createElement(NPC_TAG);
    npcElement.setAttribute(NAME_TAG, npc.getName());
    npcElement.setAttribute(ARTICLE_TAG, npc.getArticle());
    if (npc.getRespawnLocation() != null) {
      npcElement.setAttribute(RESPAWN_TAG, npc.getRespawnLocation().getName());
    }
    npcElement.setAttribute(LOCATION_TAG, npc.getLocation().getName());
    npcElement.setAttribute(DESCRIPTION_TAG, npc.getDescription());
    npcElement.setAttribute(MONEY_TAG, Integer.toString(npc.getMoney()));
    npcElement.setAttribute(STATE_TAG, npc.getCurrentState().getName());
    if (npc.getCharacterClass() != null) {
      npcElement.setAttribute(CLASS_TAG, npc.getCharacterClass().getName());
    }
    for (final State state : npc.getStates()) {
      final Element stateElement = dom.createElement(STATE_TAG);
      stateElement.setAttribute(NAME_TAG, state.getName());
      stateElement.setAttribute(DESCRIPTION_TAG, state.getDescription());
      npcElement.appendChild(stateElement);
      for (final Trigger event : state.getEventTriggers()) {
        final Element eventElement = dom.createElement(EVENT_TAG);
        stateElement.appendChild(eventElement);
        for (final XMLAttribute xml : event.getXMLAttributes()) {
          if (xml.getTag() == null) {
            eventElement.setTextContent(xml.getValue());
          } else {
            eventElement.setAttribute(xml.getTag(), xml.getValue());
          }
        }
      }
    }
    return npcElement;
  }

  /**
   * Creates an XML tree for a game place.
   *
   * @param place
   *          the game place.
   * @return the constructed XML tree.
   */
  private static Element createPlaceXML(Place place, Document dom) {
    final Element placeElement = dom.createElement(PLACE_TAG);

    placeElement.setAttribute(NAME_TAG, place.getName());
    placeElement.setAttribute(ARTICLE_TAG, place.getArticle());
    if (place.getSound() != null) {
      placeElement.setAttribute(SOUND_TAG, place.getSound());
    }
    if (place.getWinCondition()) {
      placeElement.setAttribute(WIN_TAG, "Y");
    }
    final Element description = dom.createElement(DESCRIPTION_TAG);
    description.setTextContent(place.getDescription());
    placeElement.appendChild(description);

    for (final Navigation possibleDirection : Navigation.values()) {
      if (place.isTravelAllowedToward(possibleDirection)) {
        final Element neighbor = dom.createElement(TRAVEL_TAG);
        placeElement.appendChild(neighbor);
        neighbor.setAttribute(DIRECTION_TAG, possibleDirection.getAbbreviation());
        neighbor.setTextContent(place.getTravelDestinationToward(possibleDirection).getName());
      }
    }
    return placeElement;
  }

  /**
   * Creates an XML tree for the player.
   *
   * @param player
   *          the player.
   * @return the constructed XML tree.
   */
  private static Element createPlayerXML(Player player, Document dom) {
    final Element playerElement = dom.createElement(PLAYER_TAG);
    playerElement.setAttribute(NAME_TAG, player.getName());
    playerElement.setAttribute(LOCATION_TAG, player.getLocation().getName());
    if (player.getRespawnLocation() != null) {
      playerElement.setAttribute(RESPAWN_TAG, player.getRespawnLocation().getName());
    }
    playerElement.setAttribute(SCORE_TAG, player.getScore().toString());
    playerElement.setAttribute(MONEY_TAG, Integer.toString(player.getMoney()));
    playerElement.setAttribute(ARTICLE_TAG, player.getArticle());
    playerElement.setAttribute(DESCRIPTION_TAG, player.getDescription());
    if (player.getCharacterClass() != null) {
      playerElement.setAttribute(CLASS_TAG, player.getCharacterClass().getName());
    }
    return playerElement;
  }

  /**
   * @param node
   *          The node to check
   * @param tag
   *          The attribute value to check
   * @return A string containing the text content of the attribute identified by the tag within the
   *         node or <code>null</code> if the attribute is not found.
   */
  private static String getAttributeValue(Node node, String tag) {
    final Node attribute = node.getAttributes().getNamedItem(tag);
    if (attribute != null) {
      return attribute.getTextContent();
    } else {
      return null;
    }
  }

  private static Node getChild(Node node, String tag) {
    Node child = node.getFirstChild();
    while (child != null) {
      if (child.getNodeName().equalsIgnoreCase(tag)) {
        return child;
      }
      child = child.getNextSibling();
    }
    return null;
  }

  private static List<Node> getChildren(Node root, String tag) {
    final List<Node> children = new ArrayList<Node>();
    Node currNode = root.getFirstChild();
    while (currNode != null) {
      if (currNode.getNodeName().equalsIgnoreCase(tag)) {
        children.add(currNode);
      }
      currNode = currNode.getNextSibling();
    }
    return children;
  }

  private static void loadCharacterXML(Element root, Universe world) throws PersistenceStateException {
    final List<Node> characters = getChildren(root, NPC_TAG);
    characters.addAll(getChildren(root, PLAYER_TAG));
    for (final Node characterElement : characters) {
      final String name = getAttributeValue(characterElement, NAME_TAG);
      final String charClass = getAttributeValue(characterElement, CLASS_TAG);
      final String article = getAttributeValue(characterElement, ARTICLE_TAG);
      final String moneyString = getAttributeValue(characterElement, MONEY_TAG);
      String description = getAttributeValue(characterElement, DESCRIPTION_TAG);
      if (description == null) {
        description = ""; // Allow for no description
      }
      final String locationString = getAttributeValue(characterElement, LOCATION_TAG);
      final String respawnString = getAttributeValue(characterElement, RESPAWN_TAG);

      final Container inventory = new Container();
      Place respawn = null;
      if (respawnString != null) {
        respawn = world.getPlace(respawnString);
        if (respawn == null) {
          throw new PersistenceStateException("Unable to find place for " + name + " to respawn at: " + respawnString);
        }
      }
      pavlik.john.dungeoncrawl.model.Character character = null;
      if (locationString == null) {
        throw new PersistenceStateException(name + " must have a location string defined");
      }
      final Place location = world.getPlace(locationString);
      if (location == null) {
        throw new PersistenceStateException("Unable to find the location " + locationString + " for " + name);
      }
      switch (characterElement.getNodeName()) {
        case PLAYER_TAG:
          final String score = getAttributeValue(characterElement, SCORE_TAG);
          final Player player = world.createPlayer(name, location, article, description, respawn);
          if (score != null) {
            try {
              player.addPoints(Long.parseLong(score));
            } catch (final NumberFormatException e) {
              throw new PersistenceStateException(name + "'s score must be an integer number");
            }
          }
          character = player;
          break;
        case NPC_TAG:
          final String currentStateString = getAttributeValue(characterElement, STATE_TAG);
          State currentState = null;
          final Set<State> states = new HashSet<>();
          for (final Node stateElement : getChildren(characterElement, STATE_TAG)) {
            final String stateName = getAttributeValue(stateElement, NAME_TAG);
            String stateDescription = getAttributeValue(stateElement, DESCRIPTION_TAG);
            if (stateDescription == null) {
              stateDescription = "";
            }
            final State state = new State(stateName, stateDescription);
            if (currentStateString.equalsIgnoreCase(stateName)) {
              currentState = state;
            }
            if (!states.add(state)) {
              throw new PersistenceStateException("Duplicate state name detected within single NPC");
            }

            for (final Node eventElement : getChildren(stateElement, EVENT_TAG)) {
              final String onSay = getAttributeValue(eventElement, SAY_TAG);
              final String onAttacked = getAttributeValue(eventElement, ON_ATTACKED_TAG);
              final String onHealthString = getAttributeValue(eventElement, ON_HP_TAG);
              final String onSightString = getAttributeValue(eventElement, ON_SIGHT_TAG);
              final String onTimeString = getAttributeValue(eventElement, ON_TIME_TAG);
              Trigger trigger;
              if (onSay != null) {
                trigger = new SayTrigger(new Event(buildActions(eventElement, world, name, inventory)), world, onSay,
                    SAY_TAG, buildConditionals(eventElement, world, name));
              } else if (onAttacked != null && onAttacked.toUpperCase().startsWith("Y")) {
                trigger = new AttackedTrigger(new Event(buildActions(eventElement, world, name, inventory)),
                    ON_ATTACKED_TAG, buildConditionals(eventElement, world, name));
              } else if (onHealthString != null) {
                try {
                  final int health = Integer.parseInt(onHealthString);
                  if (health < 0) {
                    throw new NumberFormatException();
                  }
                  trigger = new HealthTrigger(health, new Event(buildActions(eventElement, world, name, inventory)),
                      ON_HP_TAG, buildConditionals(eventElement, world, name));
                } catch (final NumberFormatException e) {
                  throw new PersistenceStateException("The " + ON_HP_TAG
                      + " must have an integer value greater or equal to 0: " + onHealthString);

                }
              } else if (onSightString != null) {
                trigger = new SightTrigger(new Event(buildActions(eventElement, world, name, inventory)), ON_SIGHT_TAG,
                    onSightString, buildConditionals(eventElement, world, name));
              } else if (onTimeString != null) {
                try {
                  final int time = Integer.parseInt(onTimeString);
                  if (time <= 0) {
                    throw new NumberFormatException();
                  }
                  trigger = new TimeTrigger(new Event(buildActions(eventElement, world, name, inventory)), ON_TIME_TAG,
                      time, buildConditionals(eventElement, world, name));
                } catch (final NumberFormatException e) {
                  throw new PersistenceStateException("The onTime value must be an integer greater or equal to 1.");
                }
              } else {
                throw new PersistenceStateException("At least one trigger must be added to each event, either "
                    + SAY_TAG + ", " + ON_ATTACKED_TAG + ", " + ON_HP_TAG + ", " + ON_SIGHT_TAG + ", or " + ON_TIME_TAG);
              }
              state.addEventTrigger(trigger);
            }
          }
          character = world.createNPC(name, article, description, inventory, location, states, currentState, respawn);
          break;
      }

      if (moneyString != null) {
        try {
          character.changeMoney(Integer.parseInt(moneyString));
        } catch (final NumberFormatException e) {
          throw new PersistenceStateException(name + "'s money must an integer number");
        }
      }
      if (charClass != null) {
        final CharacterClass checkClass = world.getCharacterClass(charClass);
        if (checkClass != null) {
          character.setCharacterClass(checkClass);
        } else {
          throw new PersistenceStateException(name + "'s class was not defined in the combat section");
        }
      }
    }
    if (world.getPlayers().length == 0) {
      throw new PersistenceStateException("Unable to locate any player elements in the world XML file");
    }
  }

  private static void loadClassXML(Element root, Universe world) throws PersistenceStateException {
    if (getChild(root, COMBAT_TAG) != null) {
      world.setAllowCombat(true);
      for (final Node combatElement : getChildren(root, COMBAT_TAG)) {
        for (final Node classElement : getChildren(combatElement, CLASS_TAG)) {

          final String name = getAttributeValue(classElement, NAME_TAG);
          final Node hpElement = getChild(classElement, HP_TAG);
          final String hpMax = getAttributeValue(hpElement, MAX_TAG);
          final String hpRegen = getAttributeValue(hpElement, REGEN_TAG);
          final List<String> classWeapons = new ArrayList<String>();
          final String attackMsg = getAttributeValue(classElement, USE_MESSAGE_TAG);
          final String KORecovery = getAttributeValue(classElement, KO_RECOVER_TAG);
          final String attackDmg = getAttributeValue(classElement, EFFECT_TAG);
          final String hitChance = getAttributeValue(classElement, HIT_TAG);
          final String cooldownString = getAttributeValue(classElement, COOLDOWN_TAG);
          final String targetString = getAttributeValue(classElement, TARGET_TAG);
          final String defaultWeaponName = getAttributeValue(classElement, DEFAULT_WEAPON_NAME_TAG);
          final String onHitSound = getAttributeValue(classElement, HIT_SOUND_TAG);
          final String onMissSound = getAttributeValue(classElement, MISS_SOUND_TAG);
          String damageStrings[] = new String[2];
          if (attackDmg != null) {
            if (attackDmg.contains("d")) {
              damageStrings = attackDmg.split("d");
            } else {
              damageStrings[0] = attackDmg;
              damageStrings[1] = "1";
            }
          }
          for (final Node itemElement : getChildren(classElement, USES_TAG)) {
            classWeapons.add(getAttributeValue(itemElement, TYPE_TAG));
          }
          if (name == null) {
            throw new PersistenceStateException("Every class must have a name attribute defined");
          }
          if (hpElement == null || hpMax == null || hpRegen == null) {
            throw new PersistenceStateException(name
                + " class must have a hp element defining the max health and regen rate.");
          }
          if (attackMsg == null || KORecovery == null || attackDmg == null || hitChance == null
              || cooldownString == null || targetString == null || defaultWeaponName == null) {
            throw new PersistenceStateException(name
                + " class must have a default attack option defined, including the " + DEFAULT_WEAPON_NAME_TAG + ", "
                + USE_MESSAGE_TAG + ", " + KO_RECOVER_TAG + ", " + COOLDOWN_TAG + ", " + EFFECT_TAG + "," + TARGET_TAG
                + ", and " + HIT_TAG);
          }

          world.createCharacterClass(name, Integer.parseInt(hpMax), Integer.parseInt(hpRegen), classWeapons, attackMsg,
              Integer.parseInt(KORecovery), Integer.parseInt(damageStrings[0]), Integer.parseInt(damageStrings[1]),
              Integer.parseInt(hitChance), Integer.parseInt(cooldownString), EffectTarget.valueOf(targetString
                  .toUpperCase()), onHitSound, onMissSound, defaultWeaponName);
        }
      }
    }
  }

  private static void loadCraftingXML(Element root, Universe world) throws PersistenceStateException {
    final Node craftingNode = getChild(root, CRAFTING_TAG);
    if (craftingNode == null) {
      return;
    }
    loadItemXML(craftingNode, world, false);
    final List<Node> newItemList = getChildren(craftingNode, ITEM_TAG);
    newItemList.addAll(getChildren(craftingNode, WEAPON_TAG));
    newItemList.addAll(getChildren(craftingNode, CONSUMABLE_TAG));

    for (final Node newItemNode : newItemList) {
      final List<Node> requiredItemNodes = getChildren(newItemNode, ITEM_TAG);
      requiredItemNodes.addAll(getChildren(newItemNode, WEAPON_TAG));
      requiredItemNodes.addAll(getChildren(newItemNode, CONSUMABLE_TAG));
      if (requiredItemNodes.isEmpty()) {
        throw new PersistenceStateException("At least one item must be required to create this new item");
      }
      final Set<Item> requiredItemSet = new HashSet<>();
      for (final Node requiredItemNode : requiredItemNodes) {
        final String requiredItemName = getAttributeValue(requiredItemNode, NAME_TAG);
        if (requiredItemName == null) {
          throw new PersistenceStateException("Must list the name of a required item");
        }
        final Item requiredItem = world.getItem(requiredItemName);
        if (requiredItem == null) {
          throw new PersistenceStateException("Unable to identify item: " + requiredItemName);
        }
        requiredItemSet.add(requiredItem);
      }
      final Item newItem = world.getItem(getAttributeValue(newItemNode, NAME_TAG));
      if (newItem == null) {
        throw new PersistenceStateException("Unable to find an item named \""
            + getAttributeValue(newItemNode, NAME_TAG) + "\" during the second pass through the file..."
            + "did the file change while we were reading it?");
      }

      world.addItemSynthesis(requiredItemSet, newItem);
    }
  }

  private static void loadItemXML(Node root, Universe world, boolean requiresLocation) throws PersistenceStateException {
    final List<Node> itemList = getChildren(root, ITEM_TAG);
    itemList.addAll(getChildren(root, WEAPON_TAG));
    itemList.addAll(getChildren(root, CONSUMABLE_TAG));
    /*
     * First Pass: We need to be careful on creating the map of items because of the possibility
     * that an item is located inside another item. Hence, we do this in two steps. The first step
     * is to load in the descriptive information about all the items and instantiate them in the
     * world.
     */
    for (final Node itemElement : itemList) {
      final String name = getAttributeValue(itemElement, NAME_TAG);
      if (name.contains(",")) {
        throw new PersistenceStateException("Item names cannot contain a comma: " + name);
      }
      final String article = getAttributeValue(itemElement, ARTICLE_TAG);
      String description = getAttributeValue(itemElement, DESCRIPTION_TAG);
      String takePointsString = getAttributeValue(itemElement, TAKEPOINTS_TAG);
      String dropPointsString = getAttributeValue(itemElement, DROPPOINTS_TAG);
      String takeableString = getAttributeValue(itemElement, TAKEABLE_TAG);
      final String isContainerString = getAttributeValue(itemElement, IS_CONTAINER_TAG);

      if (description == null) {
        description = "";
      }
      /**
       * Only get the cantTakeMessage if the item is not takeable
       */
      String cantTakeMessage;
      if (takeableString != null && !takeableString.toUpperCase().startsWith("Y")) {
        cantTakeMessage = getAttributeValue(itemElement, CANTTAKE_TAG);
      } else {
        takeableString = "Y";
        cantTakeMessage = "";
      }

      if (dropPointsString == null) {
        dropPointsString = "0";
      }
      if (takePointsString == null) {
        takePointsString = "0";
      }

      final Map<Place, Long> dropPointsMap = new HashMap<>();
      final Map<Place, String> blockedPlaces = new HashMap<>();

      if (name == null || article == null || cantTakeMessage == null) {
        throw new PersistenceStateException(
            "Name and article must be defined.  CantTakeMessage must be defined if item is takeable.");
      }
      for (final Node place : getChildren(itemElement, PLACE_TAG)) {
        final String locationName = place.getTextContent();
        final Place location = world.getPlace(locationName);
        if (location == null) {
          throw new PersistenceStateException("Unable to identify place: " + locationName + " for item: " + name);
        }
        final String dropPointsPlace = getAttributeValue(place, DROPPOINTS_TAG);
        final String blockMessage = getAttributeValue(place, BLOCKEDMSG_TAG);
        if (dropPointsPlace != null) {
          dropPointsMap.put(location, Long.parseLong(dropPointsPlace));
        }
        if (blockMessage != null) {
          blockedPlaces.put(location, blockMessage);
        }
      }

      final Long takePoints = Long.parseLong(takePointsString);
      final Long dropPoints = Long.parseLong(dropPointsString);
      final Boolean takeable = takeableString.toUpperCase().startsWith("Y");

      // If isContainer tag is not present, then assume item is not a container
      final Boolean isContainer = isContainerString == null ? false : isContainerString.toUpperCase().startsWith("Y");

      Item item = null;
      if (itemElement.getNodeName().equalsIgnoreCase(ITEM_TAG)) {
        item = world.createItem(name.trim(), article, description, takePoints, dropPoints, dropPointsMap, takeable,
            cantTakeMessage, blockedPlaces, isContainer);
      } else {
        final String typeString = getAttributeValue(itemElement, TYPE_TAG);
        final String effectString = getAttributeValue(itemElement, EFFECT_TAG);
        final String effectTypeString = getAttributeValue(itemElement, EFFECTTYPE_TAG);
        final String targetString = getAttributeValue(itemElement, TARGET_TAG);
        final String hitString = getAttributeValue(itemElement, HIT_TAG);
        final String usesString = getAttributeValue(itemElement, USES_TAG);
        final String cooldownString = getAttributeValue(itemElement, COOLDOWN_TAG);
        final String combatMsgString = getAttributeValue(itemElement, USE_MESSAGE_TAG);
        final String equippedString = getAttributeValue(itemElement, EQUIP_TAG);
        final String locationString = getAttributeValue(itemElement, LOCATION_TAG);
        final String onHitSound = getAttributeValue(itemElement, HIT_SOUND_TAG);
        final String onMissSound = getAttributeValue(itemElement, MISS_SOUND_TAG);

        Objects.requireNonNull(typeString, name + " " + TYPE_TAG + " must be defined");
        Objects.requireNonNull(effectString, name + " " + EFFECT_TAG + " must be defined");
        if (itemElement.getNodeName().equalsIgnoreCase(CONSUMABLE_TAG)) {
          Objects.requireNonNull(effectTypeString, name + " " + EFFECTTYPE_TAG + " must be defined");
          Objects.requireNonNull(usesString, name + " " + USES_TAG + " must be defined");
        }
        Objects.requireNonNull(targetString, name + " " + TARGET_TAG + " must be defined");
        Objects.requireNonNull(hitString, name + " " + HIT_TAG + " must be defined");
        Objects.requireNonNull(cooldownString, name + " " + COOLDOWN_TAG + " must be defined");
        Objects.requireNonNull(combatMsgString, name + " " + USE_MESSAGE_TAG + " must be defined");
        if (requiresLocation) {
          Objects.requireNonNull(locationString, name + " " + LOCATION_TAG + " must be defined");
        }
        Effect effect = null;
        if (effectString != null) {
          if (effectString.contains("d")) {
            final String[] damageStrings = effectString.split("d");
            effect = new Effect(Integer.parseInt(damageStrings[0]), Integer.parseInt(damageStrings[1]));
          } else {
            effect = new Effect(Integer.parseInt(effectString), 1);
          }
        }
        if (itemElement.getNodeName().equalsIgnoreCase(CONSUMABLE_TAG)) {
          item = world.createConsumable(name.trim(), article, description, takePoints, dropPoints, dropPointsMap,
              takeable, cantTakeMessage, blockedPlaces, isContainer ? new Container() : null, effect, EffectType
                  .valueOf(effectTypeString.toUpperCase()), Integer.parseInt(cooldownString), Integer
                  .parseInt(usesString), typeString, combatMsgString, Integer.parseInt(hitString), EffectTarget
                  .valueOf(targetString.toUpperCase()), onHitSound, onMissSound);
        } else {
          item = world.createWeapon(name.trim(), article, description, takePoints, dropPoints, dropPointsMap,
              blockedPlaces, isContainer ? new Container() : null, typeString, effect,
              Integer.parseInt(cooldownString), combatMsgString, Integer.parseInt(hitString), EffectTarget
                  .valueOf(targetString.toUpperCase()), onHitSound, onMissSound);

          if ((equippedString != null) && (locationString != null) && equippedString.toUpperCase().startsWith("Y")) {
            final Character character = world.getCharacter(locationString);
            if (character != null) {
              character.setCurrentWeapon((Weapon) item);
            }
          }
        }
      }
      for (final Place p : blockedPlaces.keySet()) {
        p.addItemRequired(item);
      }
    }
    /*
     * Second Pass
     */
    for (final Node itemElement : itemList) {
      Item current;
      try {
        current = world.getItem(getAttributeValue(itemElement, NAME_TAG));
      } catch (final NullPointerException e) {
        throw new PersistenceStateException("Unable to find an item named \""
            + getAttributeValue(itemElement, NAME_TAG) + "\" during the second pass through the file..."
            + "did the file change while we were reading it?", e);
      }

      final String locationString = getAttributeValue(itemElement, LOCATION_TAG);
      Container storage;
      if (locationString == null && !requiresLocation) {
        storage = world.getNowherePlace().getContainer();
      } else if (locationString == null && requiresLocation) {
        throw new PersistenceStateException("You didn't define a location for item: " + current.toString());
      } else {
        storage = world.getContainer(locationString);
      }
      if (storage != null) {
        storage.addItem(current);
      } else {
        throw new PersistenceStateException("Unable to store item because can't find location matching: "
            + locationString);
      }
    }
  }

  /**
   * Loads all places found within the root XML element into the world under construction.
   *
   * @param root
   *          the root of the save file's XML tree.
   * @param world
   *          the game world under construction.
   * @throws PersistenceStateException
   *           if anything goes wrong
   */
  private static void loadPlaceXML(Element root, Universe world) throws PersistenceStateException {
    final List<Node> placeList = getChildren(root, PLACE_TAG);

    /*
     * First Pass: We need to be careful on creating the map of places because the interconnections
     * require the places to exist in the world (a chicken and the egg type problem). Hence, we do
     * this in two steps. The first step is to load in the descriptive information about all the
     * places and create them all within the world under construction.
     */
    for (final Node placeElement : placeList) {
      final String name = getAttributeValue(placeElement, NAME_TAG);
      final String article = getAttributeValue(placeElement, ARTICLE_TAG);
      final String description = getChild(placeElement, DESCRIPTION_TAG).getTextContent();
      final String winConditionString = getAttributeValue(placeElement, WIN_TAG);
      final String sound = getAttributeValue(placeElement, SOUND_TAG);
      /*
       * winCondition is set to true only when the WIN_TAG is present (not null) and is a string
       * that begins with "Y", which allows the user to type "Y", "YES", "YOU BET", or anything else
       * that would indicate an affirmative answer.
       */
      final Boolean winCondition = winConditionString != null && winConditionString.toUpperCase().startsWith("Y");
      if (name == null || article == null || description == null) {
        throw new PersistenceStateException("Name, article, and description must be defined for every place");
      }

      world.createPlace(name, article, description, winCondition, sound);
    }
    /*
     * Second Pass: Next, we need to connect the places into a map as specified by the "travel"
     * elements in the XML.
     */
    for (final Node placeElement : placeList) {
      Place current, destination;
      try {
        current = world.getPlace(getAttributeValue(placeElement, NAME_TAG));
      } catch (final NullPointerException e) {
        throw new PersistenceStateException("Unable to find a place named \""
            + getAttributeValue(placeElement, NAME_TAG) + "\" during the second pass through the file..."
            + "did the file change while we were reading it?", e);

      }
      final List<Node> travelList = getChildren(placeElement, TRAVEL_TAG);
      for (final Node t : travelList) {
        Navigation direction;
        final Optional<Navigation> option = Navigation.getInstance(getAttributeValue(t, DIRECTION_TAG));
        if (option.isPresent()) {
          direction = option.get();
        } else {
          throw new IllegalStateException("\"" + getAttributeValue(t, DIRECTION_TAG)
              + "\" is not a valid direction for travel from " + "the place named \"" + current.getName() + "\"");
        }

        try {
          destination = world.getPlace(t.getTextContent());
        } catch (final NullPointerException e) {
          throw new PersistenceStateException("Unable to find a place named \"" + t.getTextContent()
              + "\" as the destination when traveling " + direction + " from the place named \"" + current.getName()
              + "\"", e);
        }

        current.setTravelDestination(direction, destination);
      }
    }
  }

  /**
   * Loads the game state from the specified {@link java.io.InputStream} and creates a
   * {@link Universe} usable Universe instance.
   *
   * @param in
   *          the non-null stream to read the game state from.
   * @return a game world.
   * @throws PersistenceStateException
   *           if anything goes wrong
   */
  protected static Universe loadWorld(InputStream in) throws PersistenceStateException {
    if (in == null) {
      throw new NullPointerException("InputStream was null, unable to load world");
    }

    final Universe world = new Universe();
    final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

    try {
      // Using factory get an instance of document builder
      final DocumentBuilder db = dbf.newDocumentBuilder();

      // parse using builder to get DOM representation of the XML file
      final Document dom = db.parse(in);

      final Element root = dom.getDocumentElement();

      final String tickRate = getAttributeValue(root, TICK_TAG);
      if (tickRate != null) {
        world.setTickRate(Long.parseLong(tickRate));
      }

      final String moneyName = getAttributeValue(root, MONEY_TAG);
      if (moneyName != null) {
        world.setMoneyName(moneyName);
      } else {
        world.setMoneyName("money");
      }

      loadPlaceXML(root, world);
      loadClassXML(root, world);
      loadCharacterXML(root, world);

      loadItemXML(root, world, true);

      loadCraftingXML(root, world);
    } catch (final IOException e) {
      throw new PersistenceStateException("A system error occurred while reading world file:", e);
    } catch (SAXException | ParserConfigurationException e) {
      throw new PersistenceStateException("A parsing error occured while reading the XML file:", e);
    }
    return world;
  }

  /**
   * Loads the game state from the specified filename on the Java classpath of the running program
   * and creates a usable Universe instance.
   *
   * @param file
   *          the {@link Path} representing the full location, on the Java classpath, of the desired
   *          world file.
   * @return a game {@link Universe}.
   * @throws PersistenceStateException
   *           if anything goes wrong
   */
  protected static Universe loadWorld(Path file) throws PersistenceStateException {
    if (file == null) {
      throw new NullPointerException("Unable to load world file: location was not specified");
    }
    try (InputStream in = new BufferedInputStream(Files.newInputStream(file))) {
      return loadWorld(in);
    } catch (final IOException e) {
      throw new PersistenceStateException("Unable to open world file", e);
    }
  }

  /**
   * Loads the game state from the specified string version of a file on the Java classpath of the
   * running program and creates a usable Universe instance.
   *
   * @param fileString
   *          the non-null {@link String} representing the full location, on the Java classpath, of
   *          the desired world file.
   * @return a game {@link Universe}.
   * @throws PersistenceStateException
   *           if anything goes wrong
   */
  public static Universe loadWorld(String fileString) throws PersistenceStateException {
    if (fileString == null) {
      throw new NullPointerException("fileString cannot be null");
    }
    try {
      Path file = Paths.get(fileString).normalize();
      if (!Files.exists(file)) {
        URL url = GamePersistence.class.getResource(fileString);
        if (url == null) {
          url = GamePersistence.class.getClassLoader().getResource(fileString);
        }
        try {
          file = Paths.get(url.toURI());
        } catch (NullPointerException | FileSystemNotFoundException e) {
          final InputStream stream = fileString.getClass().getResourceAsStream(fileString);
          if (stream != null) {
            return loadWorld(stream);
          } else {
            throw new PersistenceStateException("Unable to locate or load file identified as: " + fileString);
          }
        }
      }
      return loadWorld(file);
    } catch (InvalidPathException | IllegalStateException | URISyntaxException | NullPointerException e) {
      throw new PersistenceStateException("Unable to locate or load file identified as: " + fileString
          + " due to reason: " + e.getMessage(), e);
    }
  }

  /**
   * Saves the state of the specified {@link Universe} into the specified {@link Path} in XML
   * format. It is suggested calls to this surround the call with a try-catch block if recovery from
   * a save problem is desired.
   *
   * @param world
   *          the game state to save.
   * @param file
   *          the file to save the game state to.
   * @throws PersistenceStateException
   *           if something goes wrong.
   */
  protected static void saveWorld(Universe world, Path file) throws PersistenceStateException {
    final DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
    try {
      DocumentBuilder builder;
      builder = domFactory.newDocumentBuilder();

      final Document dom = builder.newDocument();

      final Element worldElement = dom.createElement(GAME_TAG);

      dom.appendChild(worldElement);

      worldElement.setAttribute(VERSION_TAG, SAVEFILE_VERSION);
      worldElement.setAttribute(MONEY_TAG, world.getMoneyName());
      worldElement.setAttribute(TICK_TAG, Long.toString(world.getTickRate()));

      /*
       * Create XML for Places
       */
      for (final Place place : world.getPlaces()) {
        /*
         * We don't save the nowhere place to the save file. This place always exists in every world
         * so its inclusion in the save file XML will cause an attempt on loading a save file into a
         * model to try and create it again (resulting in an exception).
         */
        if (place != world.getNowherePlace()) {
          worldElement.appendChild(createPlaceXML(place, dom));
        }
      }

      /*
       * Create XML for the Players and each players' items
       */
      for (final Player player : world.getPlayers()) {
        worldElement.appendChild(createPlayerXML(player, dom));
        for (final Item item : player.getContainer().getItems()) {
          worldElement.appendChild(createItemXML(world, item, player.getName(), dom));
        }
      }

      /*
       * Build XML for the items
       */

      for (final Place place : world.getPlaces()) {
        if (place == world.getNowherePlace()) {
          continue;
        }
        for (final Item item : place.getContainer().getItems()) {
          worldElement.appendChild(createItemXML(world, item,
              place == world.getNowherePlace() ? null : place.getName(), dom));
        }
      }
      for (final NonPlayerCharacter npc : world.getNonPlayerCharacters()) {
        worldElement.appendChild(createNpcXML(npc, dom));
        for (final Item item : npc.getContainer().getItems()) {
          worldElement.appendChild(createItemXML(world, item, npc.getName(), dom));
        }
      }
      for (final Item container_item : world.getItems()) {
        if (container_item.getContainer() != null) {
          for (final Item inside_item : container_item.getContainer().getItems()) {
            worldElement.appendChild(createItemXML(world, inside_item, container_item.getName(), dom));
          }
        }
      }
      if (!world.getCharacterClasses().isEmpty()) {
        final Element combatElement = dom.createElement(COMBAT_TAG);
        for (final CharacterClass cClass : world.getCharacterClasses()) {
          createClassXML(cClass, combatElement, dom);
        }
        worldElement.appendChild(combatElement);
      }

      final Set<Entry<Set<Item>, Item>> craftingSet = world.getCraftingObjects();
      if (!craftingSet.isEmpty()) {
        final Element craftingElement = dom.createElement(CRAFTING_TAG);
        worldElement.appendChild(craftingElement);
        for (final Entry<Set<Item>, Item> entry : world.getCraftingObjects()) {
          final Element craftedItemElement = createItemXML(world, entry.getValue(), null, dom);
          craftingElement.appendChild(craftedItemElement);
          for (final Item requiredItem : entry.getKey()) {
            final Element requiredItemElement = dom.createElement(ITEM_TAG);
            requiredItemElement.setAttribute(NAME_TAG, requiredItem.getName());
            craftedItemElement.appendChild(requiredItemElement);
          }
        }
      }

      // Prepare the DOM document for writing
      final Source source = new DOMSource(dom);

      final TransformerFactory tFactory = TransformerFactory.newInstance();
      tFactory.setAttribute("indent-number", 2);
      final Transformer xformer = tFactory.newTransformer();
      xformer.setOutputProperty(OutputKeys.INDENT, "yes");

      // Prepare the file as an output stream
      // Moved the FileOutputSteam and OutputStreamWriter instantiation inside of an automatic
      // resource management block (Java 7 feature), in order to guarantee the file handles
      // are closed properly.
      try (OutputStreamWriter osw = new OutputStreamWriter(new BufferedOutputStream(Files.newOutputStream(file)),
          "UTF-8")) {
        // Write the DOM document to the file
        final Result result = new StreamResult(osw);
        xformer.transform(source, result);
      } catch (final IOException e) { // Can be thrown by automatic OutputStream.close() call
        throw new PersistenceStateException("Unable to complete the output of the DOM to the OutputStream", e);
      }

    } catch (final TransformerConfigurationException e) {
      // something went wrong
      throw new PersistenceStateException("Unable to build DOM source from the DOM document.", e);
    } catch (final TransformerFactoryConfigurationError e) {
      // something went wrong
      throw new PersistenceStateException("Unable to configure the TransformerFactory.", e);
    } catch (final TransformerException e) {
      // something went wrong
      throw new PersistenceStateException("Unable to transform DOM source into a file.", e);
    } catch (final ParserConfigurationException e) {
      // something went wrong
      throw new PersistenceStateException("Unable to configure the DOM document builder for Universe.", e);
    }
  }

  /**
   * Saves the state of the specified {@link Universe} into the specified {@link String}
   * representing a file in XML format. It is suggested calls to this surround the call with a
   * try-catch block if recovery from a save problem is desired.
   *
   * @param world
   *          the game state to save.
   * @param fileString
   *          the file to save the game state to.
   * @throws PersistenceStateException
   *           if something goes wrong.
   */
  public static void saveWorld(Universe world, String fileString) throws PersistenceStateException {
    if (fileString == null) {
      throw new NullPointerException("fileString cannot be null");
    }
    final Path file = Paths.get(fileString).normalize();
    saveWorld(world, file);
  }

  /**
   * The version of the game as defined by the XML save file format.
   */
  public static final String  SAVEFILE_VERSION        = "1.4";

  /**
   * The full location, on the Java classpath, of the default world file.
   */
  public static final String  DEFAULT_WORLD           = "/pavlik/john/dungeoncrawl/persistence/DemoWorld.xml";

  private static final String ARTICLE_TAG             = "article";
  private static final String DESCRIPTION_TAG         = "description";
  private static final String DIRECTION_TAG           = "direction";
  private static final String LOCATION_TAG            = "location";
  private static final String NAME_TAG                = "name";
  private static final String PLACE_TAG               = "place";
  private static final String PLAYER_TAG              = "player";
  private static final String GAME_TAG                = "dungeoncrawl";
  private static final String TRAVEL_TAG              = "travel";
  private static final String VERSION_TAG             = "version";
  private static final String WIN_TAG                 = "arrivalWinsGame";
  private static final String ITEM_TAG                = "item";
  private static final String TAKEPOINTS_TAG          = "takePoints";
  private static final String DROPPOINTS_TAG          = "dropPoints";
  private static final String TAKEABLE_TAG            = "takeable";
  private static final String CANTTAKE_TAG            = "cantTakeMsg";
  private static final String BLOCKEDMSG_TAG          = "blockedMsg";
  private static final String SCORE_TAG               = "score";
  private static final String NPC_TAG                 = "npc";
  private static final String STATE_TAG               = "state";
  private static final String EVENT_TAG               = "event";
  private static final String SAY_TAG                 = "onSay";
  private static final String RESPONSE_TAG            = "response";
  private static final String TAKE_ITEM_TAG           = "takeItem";
  private static final String GIVE_ITEM_TAG           = "giveItem";
  private static final String PAY_MONEY_TAG           = "payMoney";
  private static final String ACCEPT_MONEY_TAG        = "acceptMoney";
  private static final String SET_NPC_LOCATION_TAG    = "setNpcLocation";
  private static final String PLAYER_HAS_ITEM_TAG     = "playerHasItem";
  private static final String PLAYER_HAS_MONEY_TAG    = "playerHasMoney";
  private static final String NPC_HAS_ITEM_TAG        = "npcHasItem";
  private static final String MONEY_TAG               = "money";
  private static final String IS_CONTAINER_TAG        = "isContainer";
  private static final String CRAFTING_TAG            = "crafting";
  private static final String CLASS_TAG               = "class";
  private static final String HP_TAG                  = "hp";
  private static final String MAX_TAG                 = "max";
  private static final String REGEN_TAG               = "regenRate";
  private static final String TYPE_TAG                = "type";
  private static final String COMBAT_TAG              = "combat";
  private static final String EFFECT_TAG              = "effect";
  private static final String HIT_TAG                 = "hitChance";
  private static final String COOLDOWN_TAG            = "cooldown";
  private static final String USE_MESSAGE_TAG         = "useMessage";
  private static final String EQUIP_TAG               = "equipped";
  private static final String TICK_TAG                = "tickRate";
  private static final String CONSUMABLE_TAG          = "consumable";
  private static final String KO_RECOVER_TAG          = "KORecovery";
  private static final String EFFECTTYPE_TAG          = "effectType";
  private static final String TARGET_TAG              = "target";
  private static final String USES_TAG                = "uses";
  private static final String WEAPON_TAG              = "weapon";
  private static final String SOUND_TAG               = "sound";
  private static final String RESPAWN_TAG             = "respawn";
  private static final String HIT_SOUND_TAG           = "onHitSound";
  private static final String MISS_SOUND_TAG          = "onMissSound";
  private static final String DEFAULT_WEAPON_NAME_TAG = "defaultWeaponName";
  private static final String ON_ATTACKED_TAG         = "onAttacked";
  private static final String ON_HP_TAG               = "onHealth";
  private static final String ATTACK_CHARACTER_TAG    = "attackCharacter";
  private static final String ON_SIGHT_TAG            = "onSight";
  private static final String ON_TIME_TAG             = "onTime";
}
