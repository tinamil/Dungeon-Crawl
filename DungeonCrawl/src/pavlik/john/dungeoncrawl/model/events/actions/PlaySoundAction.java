package pavlik.john.dungeoncrawl.model.events.actions;

import java.util.Objects;

import pavlik.john.dungeoncrawl.model.Character;
import pavlik.john.dungeoncrawl.model.Universe;
import pavlik.john.dungeoncrawl.model.events.Action;

/**
 * A sound playing action
 *
 * @author John
 *
 */
public class PlaySoundAction extends Action {

  /**
   *
   */
  private static final long serialVersionUID = 1L;
  String f_soundPath;
  Universe  f_world;

  /**
   * Play a sound file to everyone in the region of the activating character when performing the
   * action
   *
   * @param world
   *          The current world
   * @param value
   *          The string path to the sound file
   * @param tag
   *          the XML tag this was built from
   */
  public PlaySoundAction(Universe world, String value, String tag) {
    super(tag, value);
    f_soundPath = Objects.requireNonNull(value, "value in PlaySoundAction cannot be null");
    f_world = Objects.requireNonNull(world, "world in PlaySoundAction cannot be null");
  }

  @Override
  public boolean performAction(Character cause) {
    f_world.playSound(cause, f_soundPath);
    return true;
  }

}
