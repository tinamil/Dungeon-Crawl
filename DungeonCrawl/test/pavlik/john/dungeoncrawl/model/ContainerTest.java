package pavlik.john.dungeoncrawl.model;

import java.util.HashMap;
import java.util.ResourceBundle;

import pavlik.john.dungeoncrawl.model.Container;
import pavlik.john.dungeoncrawl.model.HasContainer;
import pavlik.john.dungeoncrawl.model.Item;
import pavlik.john.dungeoncrawl.model.Place;
import pavlik.john.dungeoncrawl.properties.Messages;
import pavlik.john.dungeoncrawl.view.TextUtilities;
import junit.framework.TestCase;

/**
 * @author John Pavlik
 * @see Place
 */
public class ContainerTest extends TestCase {

  Container      container;
  Item           item1, item2, item3;
  ResourceBundle messages;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    container = new Container();
    item1 = new Item("Name1", "article", "description", 0, 0, new HashMap<>(), false, "Can't", new HashMap<>(), null);
    item2 = new Item("Name2", "article", "description2", 0, 0, new HashMap<>(), true, "", new HashMap<>(), null);
    item3 = new Item("Name3", "article", "description3", 0, 0, new HashMap<>(), false, "Can't", new HashMap<>(), null);
    messages = Messages.loadMessages("en", "US");
  }

  /**
   * Test method for pavlik.john.dungeoncrawl.model.Container.removeItems() and
   * pavlik.john.dungeoncrawl.model.Container.AddItems()
   */
  public void testAddRemoveItems() {
    assertEquals(container.getItems().size(), 0);
    container.addItem(item1);
    container.addItem(item2);
    assertTrue(container.getItems().contains(item1));
    assertTrue(container.getItems().contains(item2));
    assertEquals(item1, container.removeItem(item1));
    assertFalse(container.getItems().contains(item1));
    try {
      container.addItem(null);
      fail();
    } catch (final NullPointerException e) {
    }
    assertEquals(null, container.removeItem(item1));
  }

  /**
   * Test method for pavlik.john.dungeoncrawl.model.Container constructor
   */
  public void testConstructor() {
    container = new Container();
  }

  /**
   * Test method for pavlik.john.dungeoncrawl.model.Container.describeItems()
   */
  public void testDescribeItems() {
    assertEquals("", TextUtilities.describeItems(container.getItems(), messages));
    container.addItem(item1);
    assertEquals("article Name1", TextUtilities.describeItems(container.getItems(), messages));
    container.addItem(item2);
    assertEquals("article Name1 and article Name2", TextUtilities.describeItems(container.getItems(), messages));
    container.addItem(item3);
    assertEquals("article Name1, article Name2, and article Name3", TextUtilities.describeItems(container.getItems(),
        messages));
  }

  /**
   * Test method for 'pavlik.john.dungeoncrawl.model.HasContainer.getContainer'
   */
  public void testFunctionalHasContainerInterface() {
    final HasContainer c = () -> {
      return container;
    };
    assertEquals(container, c.getContainer());
  }

  /**
   * Test method for pavlik.john.dungeoncrawl.model.Container.getItems
   */
  public void testGetItems() {
    assertNotNull(container.getItems());
    assertTrue(container.getItems().isEmpty());
    container.addItem(item1);
    assertTrue(container.getItems().contains(item1));
  }

  /**
   * Test method for pavlik.john.dungeoncrawl.model.Container.isEmpty
   */
  public void testIsEmpty() {
    assertTrue(container.isEmpty());
    container.addItem(item1);
    assertFalse(container.isEmpty());
  }

  /**
   * Test method for pavlik.john.dungeoncrawl.model.Container.IsPresent(Item item)
   */
  public void testIsPresent() {
    assertEquals(container.getItems().size(), 0);
    container.addItem(item1);
    container.addItem(item2);
    assertTrue(container.isPresent(item1));
    assertTrue(container.isPresent(item2));
    assertFalse(container.isPresent(item3));
    assertFalse(container.isPresent(null));
  }

  /**
   * Test method for 'pavlik.john.dungeoncrawl.model.Container.moveItem(Container, item)'
   */
  public void testMoveItem() {
    final Container con2 = new Container();
    container.addItem(item1);
    assertTrue(container.moveItem(con2, item1));
    assertTrue(container.isEmpty());
    assertTrue(con2.isPresent(item1));
    assertFalse(container.moveItem(con2, item2));
  }
}
