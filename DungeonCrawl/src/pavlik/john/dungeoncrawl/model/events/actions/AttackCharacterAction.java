package pavlik.john.dungeoncrawl.model.events.actions;

import java.util.Objects;

import pavlik.john.dungeoncrawl.model.Character;
import pavlik.john.dungeoncrawl.model.NonPlayerCharacter;
import pavlik.john.dungeoncrawl.model.Universe;
import pavlik.john.dungeoncrawl.model.events.Action;
import pavlik.john.dungeoncrawl.model.events.XMLAttribute;

/**
 * Adjust the money available to the player upon execution of this action
 *
 * @author John
 * @version 1.3
 * @since 1.3
 */
public class AttackCharacterAction extends Action {

  /**
   *
   */
  private static final long serialVersionUID = 1L;
  String f_attackCharacter, f_npcName;

  /**
   * Public constructor
   *
   * @param npcName
   *          The NPC performing the attack
   *
   * @param attackCharacterName
   *          The character to attack, or null if it is the current player who triggers the Action
   * @param tag
   *          The XML tag to use to identify this {@link XMLAttribute}
   */
  public AttackCharacterAction(String npcName, String attackCharacterName, String tag) {
    super(tag, attackCharacterName);
    f_attackCharacter = Objects.requireNonNull(attackCharacterName,
        "attackCharactername in AttackCharacterAction cannot be null");
    f_npcName = Objects.requireNonNull(npcName, "npcName in AttackCharacterAction cannot be null");
  }

  @Override
  public boolean performAction(Character player) {
    final Universe world = player.getLocation().getWorld();
    final NonPlayerCharacter npc = world.getNonPlayerCharacter(f_npcName);
    if (npc == null) {
      throw new IllegalStateException("Unable to find npc by name of " + f_npcName);
    }
    Character defender = null;
    if (f_attackCharacter.equalsIgnoreCase("player")) {
      defender = player;
    } else {
      defender = world.getCharacter(f_attackCharacter);
      if (defender == null) {
        throw new IllegalStateException("Unable to find character by name of " + f_attackCharacter);
      }
    }
    npc.startAttack(defender);
    return true;
  }

}
