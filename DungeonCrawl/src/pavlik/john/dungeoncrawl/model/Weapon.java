package pavlik.john.dungeoncrawl.model;

import java.util.Map;

/**
 * Weapon class that extends Item
 *
 * @author John
 */
public class Weapon extends Consumable {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  Weapon(String name, String article, String description, long takePoints, long dropPointsNowhere,
      Map<Place, Long> dropPointsMap, Map<Place, String> blockMessages, Container container, String type,
      Effect effect, int cooldown, String useString, int hitChance, EffectTarget targets, String onHitSound,
      String onMissSound) {
    super(name, article, description, takePoints, dropPointsNowhere, dropPointsMap, true, "", blockMessages, container,
        effect, Consumable.EffectType.DAMAGE, cooldown, -1, type, useString, hitChance, targets, onHitSound,
        onMissSound);
  }

}
