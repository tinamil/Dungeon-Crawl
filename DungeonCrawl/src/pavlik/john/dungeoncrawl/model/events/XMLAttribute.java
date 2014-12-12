package pavlik.john.dungeoncrawl.model.events;

import java.io.Serializable;
import java.util.Objects;

/**
 * Abstract class for XML Attributes. Used primarily to save the value and tag of Conditional and
 * Action anonymous inner classes during load so that they can be saved back out.
 *
 * @author John
 * @version 1.3
 * @since 1.3
 */
public abstract class XMLAttribute implements Serializable {
  /**
   *
   */
  private static final long serialVersionUID = 1L;
  String f_value, f_tag;

  /**
   * Public constructor for XML attributes
   *
   * @param tag
   *          the XML attribute tag, or null if this is a textContent
   * @param value
   *          the value to insert into the attribute
   */
  public XMLAttribute(String tag, String value) {
    f_tag = tag;
    f_value = Objects.requireNonNull(value, "XMLAttribute " + tag + "'s value cannot be null");
  }

  /**
   * Get the tag for this XML, may be null if this is a text content instead of an attribute.
   *
   * @return a {@link String} or null.
   */
  public String getTag() {
    return f_tag;
  }

  /**
   * Get the value of this XML
   *
   * @return a {@link String}
   */
  public String getValue() {
    return f_value;
  }
}
