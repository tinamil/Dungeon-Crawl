package pavlik.john.dungeoncrawl.view;

import java.text.Collator;
import java.util.Collection;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.Set;

import pavlik.john.dungeoncrawl.model.Item;
import pavlik.john.dungeoncrawl.properties.Messages;

/**
 * @author John Pavlik
 *
 * @version 1.3
 */
public class TextUtilities {

  /**
   * Will build a string describing all of the items in this container in the format of
   * "item1.toString, item2.toString(), and item3.toString()"
   *
   * @param items
   *          The collection of things to describe
   * @param messages
   *          The {@link ResourceBundle} that contains the word for "and"
   *
   * @return an {@link String} of {@link Item}s described by each item's {@link Item#toString()}
   *         method, if there are items, or an empty String if there are no items.
   */
  public static String describeItems(Collection<? extends Object> items, ResourceBundle messages) {
    final StringBuilder buffer = new StringBuilder();
    final Iterator<? extends Object> iterator = items.iterator();
    final int count = items.size();
    if (iterator.hasNext()) {
      buffer.append(iterator.next().toString());
    }
    while (iterator.hasNext()) {
      final Object currentItem = iterator.next();
      if (iterator.hasNext()) {
        buffer.append(", " + currentItem.toString());
      } else { // Last item in the iterator, time to finalize the string
        buffer.append((count > 2 ? ", " : " ") + messages.getString(Messages.AND) + " " + currentItem.toString());
      }
    }
    return buffer.toString();
  }

  /**
   * Checks to see if the input parameter is equal to any value in the commandList parameter using a
   * collator in the current user's default locale.
   *
   * @param commandList
   *          an array of strings that will be iterated through in order
   * @param input
   *          a value to check against the commandList parameter
   * @return true if the input parameter is found in the commandList, false otherwise
   */
  public static boolean match(String[] commandList, String input) {
    final Collator myDefaultCollator = Collator.getInstance();
    for (final String s : commandList) {
      if (myDefaultCollator.equals(s, input)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Trim any valid articles from the front of the itemName string.
   *
   * @param itemName
   *          {@link String} to check for valid articles at the front
   * @param validArticles
   *          A Set of valid article strings
   * @return the trimmed {@link String}
   */
  public static String trimArticles(String itemName, Set<String> validArticles) {
    for (final String article : validArticles) {
      if (itemName.split("\\s")[0].equalsIgnoreCase(article)) {
        return itemName.substring(article.length()).trim();
      }
    }
    return itemName;
  }

  static ResourceBundle      f_messages = Messages.loadMessages("en", "us");

  /**
   * The operating system-specific line separation character.
   */
  public static final String LINESEP    = System.getProperty("line.separator");

  /**
   * Convenience constant for adding two line separators at once.
   */
  public static final String LINESEP2   = LINESEP + LINESEP;

  /**
   * Disable default constructor
   */
  private TextUtilities() {
  }

}
