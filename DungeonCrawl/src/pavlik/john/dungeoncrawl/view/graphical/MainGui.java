package pavlik.john.dungeoncrawl.view.graphical;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import pavlik.john.dungeoncrawl.controller.Controller;
import pavlik.john.dungeoncrawl.exceptions.PersistenceStateException;
import pavlik.john.dungeoncrawl.model.Character;
import pavlik.john.dungeoncrawl.model.Place;
import pavlik.john.dungeoncrawl.model.Player;
import pavlik.john.dungeoncrawl.model.Weapon;
import pavlik.john.dungeoncrawl.model.Universe;
import pavlik.john.dungeoncrawl.persistence.MultiplayerServerThread;
import pavlik.john.dungeoncrawl.persistence.GamePersistence;
import pavlik.john.dungeoncrawl.properties.Messages;
import pavlik.john.dungeoncrawl.view.GameObserver;
import pavlik.john.dungeoncrawl.view.TextUtilities;
import pavlik.john.dungeoncrawl.view.parser.Parser;

/**
 * The main program for MainConsole with a simple graphical user interface. It has a window that lets
 * the user type commands and see game output in a text box. Thus, it is not that much different
 * from a text-only user interface.
 * <p>
 * This application uses the Swing graphical user interface library (part of the Java standard
 * library).
 *
 * @author T.J. Halloran
 * @author Robert Graham
 * @author Brian Woolley
 * @author John Pavlik
 * @version 1.2
 */
public final class MainGui extends JFrame {

  private class GraphicalParserWorldObserver extends GameObserver {
    final JTextPane     f_textArea;
    Style               f_colorStyle;
    Pattern             colorPattern;
    // Retrieve the user preference node
    final Preferences   f_preferences;

    // Preference key name
    final static String FILE_PREFERENCE_KEY = "last_file_location";

    GraphicalParserWorldObserver(JTextPane area, ResourceBundle messages) {
      super(messages);
      f_textArea = area;
      f_colorStyle = f_textArea.addStyle("red", null);
      colorPattern = Pattern.compile("\\[color=(.+?)\\](.*?)\\[/color\\]");
      f_preferences = Preferences.userNodeForPackage(GraphicalParserWorldObserver.class);
      display(Messages.SPLASH);
    }

    @Override
    public void characterEquippedWeapon(Character character, Weapon weapon) {
      super.characterEquippedWeapon(character, weapon);
      updateWeaponDisplay(character);
    }

    @Override
    public void characterHeal(Character character, int heal) {
      super.characterHeal(character, heal);
      updateHealthDisplay(character, character.getCurrentHealth() + heal);
      updateTargetHealthDisplay(character, character.getCurrentHealth() + heal);
    }

    @Override
    public void characterHitsCharacterFor(Character attacker, String combatMsg, Character target, int damage) {
      super.characterHitsCharacterFor(attacker, combatMsg, target, damage);
      updateHealthDisplay(target, target.getCurrentHealth() - damage);
      updateTargetHealthDisplay(target, target.getCurrentHealth() - damage);
    }

    @Override
    public void characterRespawned(Character character, Place previousLocation, Place newLocation) {
      super.characterRespawned(character, previousLocation, newLocation);
      updateLocationDisplay(character);
    }

    @Override
    public void characterUnequippedWeapon(Character character) {
      super.characterUnequippedWeapon(character);
      updateWeaponDisplay(character);
    }

    @Override
    public void characterWonFight(Character attacker, Character target) {
      super.characterWonFight(attacker, target);
      if (f_player != null && f_player.getCurrentTarget() != null && f_player.getCurrentTarget().equals(target)) {
        updateTargetHealthDisplay(null, 0);
      }
    }

    @Override
    public synchronized void display(String message) {
      if (message == null) {
        return;
      }
      final StyledDocument doc = f_textArea.getStyledDocument();
      // final boolean scroll = isViewAtBottom();

      final Matcher matcher = colorPattern.matcher(message);
      while (matcher.find()) {
        try {
          final String preColor = message.substring(0, matcher.start());
          doc.insertString(doc.getLength(), preColor, null);
          Color color;
          try {
            final Field field = Color.class.getField(matcher.group(1));
            color = (Color) field.get(null);
          } catch (final Exception e) {
            color = null; // Not defined
          }
          if (color == null) {
            color = Color.BLACK;
          }
          StyleConstants.setForeground(f_colorStyle, color);
          doc.insertString(doc.getLength(), matcher.group(2), f_colorStyle);
          message = message.substring(matcher.end());
        } catch (final BadLocationException e) {
          e.printStackTrace();
        }
      }

      try {
        doc.insertString(doc.getLength(), message + TextUtilities.LINESEP, null);
      } catch (final BadLocationException e) {
        e.printStackTrace();
      }

      // f_textArea.append(message);
      // f_textArea.append(TextUtilities.LINESEP);
      // if (scroll) {
      // System.out.println("Scrolling");
      // scrollToBottom();
      // }
    }

    @Override
    public void gameOver(Universe world) {
      String message = f_messages.getString(world.isGameWon() ? Messages.VICTORY : Messages.QUIT);
      for (final Player player : world.getPlayers()) {
        message += TextUtilities.LINESEP
            + f_messages.getString(Messages.SCORE_MESSAGE).replace(Messages.SCORE_TAG, player.getScore().toString())
            .replace(Messages.NAME_TAG, player.getName());
      }
      JOptionPane.showMessageDialog(MainGui.this, message, f_messages.getString(Messages.THANKS),
          JOptionPane.INFORMATION_MESSAGE);
      f_parser.shutdown();
      dispose();
      System.exit(0);
    }

    /*
     * (non-Javadoc)
     *
     * @see pavlik.john.dungeoncrawl.view.parser.IDisplayNotifier#load()
     */
    @Override
    public Optional<File> load() {
      final String lastFile = f_preferences.get(FILE_PREFERENCE_KEY, null);
      final JFileChooser fc = new JFileChooser(lastFile);
      if (fc.showOpenDialog(MainGui.this) == JFileChooser.APPROVE_OPTION) {
        f_preferences.put(FILE_PREFERENCE_KEY, fc.getSelectedFile().toString());
        return Optional.of(fc.getSelectedFile());
      } else {
        return Optional.empty();
      }
    }

    @Override
    public void playerMoved(Player player, Place startLocation, Place finishLocation) {
      super.playerMoved(player, startLocation, finishLocation);
      updateLocationDisplay(player);
      updateTargetHealthDisplay(null, 0);
    }

    @Override
    public void quit() {
      gameOver(f_worldController.getWorld());
    }

    /*
     * (non-Javadoc)
     *
     * @see pavlik.john.dungeoncrawl.view.parser.IDisplayNotifier#save()
     */
    @Override
    public Optional<File> save() {
      final String lastFile = f_preferences.get(FILE_PREFERENCE_KEY, null);
      final JFileChooser fc = new JFileChooser(lastFile);
      if (fc.showSaveDialog(MainGui.this) == JFileChooser.APPROVE_OPTION) {
        f_preferences.put(FILE_PREFERENCE_KEY, fc.getSelectedFile().toString());
        return Optional.of(fc.getSelectedFile());
      } else {
        return Optional.empty();
      }
    }

    @Override
    public void setCurrentPlayer(Player player) {
      super.setCurrentPlayer(player);
      if (player == null) {
        return;
      }
      f_playerName.setText(player.getName());
      f_location.setText(player.getLocation().getName());
      if (player.getCharacterClass() != null) {
        f_health.setText(Integer.toString(player.getCurrentHealth()));
        f_health.setVisible(true);
        f_weapon.setText(player.getCurrentWeapon().getName());
        f_weapon.setVisible(true);
        f_target.setVisible(true);
        f_targetHealth.setVisible(true);
        if (player.getCurrentTarget() != null) {
          updateTargetHealthDisplay(player.getCurrentTarget(), player.getCurrentTarget().getCurrentHealth());
        }
      } else {
        f_health.setVisible(false);
        f_weapon.setVisible(false);
        f_target.setVisible(false);
        f_targetHealth.setVisible(false);
      }
    }

    @Override
    public void setSound(boolean enabled) {
      super.setSound(enabled);
      String icon;
      if (enabled) {
        icon = onIcon;
      } else {
        icon = offIcon;
      }
      BufferedImage image;
      try {
        image = ImageIO.read(getClass().getResource(icon));

        if (image != null) {
          f_audioButton.setIcon(new ImageIcon(image));
        }
      } catch (final IOException e) {
        e.printStackTrace();
      }
    }

    private void updateHealthDisplay(Character target, int health) {
      if (f_player != null && f_player.equals(target) && f_player.getCharacterClass() != null) {
        f_health.setText(Integer.toString(health));
      }
    }

    private void updateLocationDisplay(Character target) {
      if (f_player != null && f_player.equals(target)) {
        f_location.setText(f_player.getLocation().getName());
      }
    }

    private void updateTargetHealthDisplay(Character target, int health) {
      if (target == null) {
        f_target.setText("");
        f_targetHealth.setText("");
      } else if (f_player != null && f_player.getCurrentTarget() != null && f_player.getCurrentTarget().equals(target)) {
        f_targetHealth.setText(Integer.toString(health));
        f_target.setText(target.getName());
      }
    }

    private void updateWeaponDisplay(Character target) {
      if (f_player != null && f_player.equals(target) && f_player.getCharacterClass() != null) {
        f_weapon.setText(f_player.getCurrentWeapon().toString());
      }
    }

    @Override
    public void worldLoaded(Universe world, String fileName) {
      f_player = null;
      if (f_parser != null) {
        f_parser.clearPlayer();
      }
    }
  }

  /**
   * The main program for MainConsole with a text user interface.
   *
   * @param args
   *          command-line arguments (ignored by this program).
   */
  public static void main(String[] args) {
    setSystemLookAndFeel();
    // Setup a multi-threaded environment for the Swing GUI to render correctly
    SwingUtilities.invokeLater(() -> new MainGui());
  }

  /**
   * Sets the Swing GUI to use the system default look and feel as opposed to the cross-platform
   * that is used when no look and feel is specified.
   */
  private static void setSystemLookAndFeel() {
    try {
      // Set System L&F
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException e) {
      // Fail silently and let the application continue to load with the default look and feel
      e.printStackTrace();
    }
  }

  /**
   * A ResourceBundle that contains all of the messages input from and displayed to the user.
   */
  private static ResourceBundle f_messages       = Messages.loadMessages("en", "US");

  JScrollPane                   f_outputScrollPane;
  private JButton               f_audioButton;
  private final String          onIcon           = "/icons/flaticon/speaker111.png";
  private final String          offIcon          = "/icons/flaticon/speaker113.png";

  private final JLabel          f_playerName, f_location;
  private final JLabel          f_health, f_weapon, f_target, f_targetHealth;

  private Parser     f_parser;
  private Controller       f_worldController;
  final JTextPane               f_serverResponsesTextArea;
  /**
   * Needed to make the compiler happy (JFrame is serializable). Just ignore this field.
   */
  private static final long     serialVersionUID = 1L;

  private final Deque<String>   previousCommands = new ArrayDeque<String>();

  /**
   * This constructor sets up the user interface for our application. This includes registering all
   * listeners (observers of user interface actions, e.g., clicking on a button).
   */
  public MainGui() {
    super();

    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

    /**
     * Adding window listener to allow controlled shutdown of the swing application
     */
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        super.windowClosing(e);
        f_parser.shutdown();
        dispose();
        System.exit(0);
      }
    });

    setSize(700, 500);

    setTitle(f_messages.getString(Messages.TITLE));

    // we'll use a fixed font for dialog entry and messages
    final Font fixed = new Font("Courier", Font.PLAIN, 14);

    /*
     * SWING (user interface) variables
     */
    final JLabel f_messageLabel = new JLabel(f_messages.getString(Messages.MESSAGE_LABEL));
    final JTextField f_messageField = new JTextField();
    final JButton f_sendButton = new JButton(f_messages.getString(Messages.SEND_BUTTON));

    f_serverResponsesTextArea = new JTextPane();

    final FocusListener f_selectOnFocus = new FocusListener() {
      @Override
      public void focusGained(FocusEvent e) {
        ((JTextComponent) e.getSource()).selectAll();
      }

      @Override
      public void focusLost(FocusEvent e) {
      }
    };

    // setup the main application panel
    final JPanel p1 = new JPanel();
    p1.setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.gridx = c.gridy = 0;
    c.insets = new Insets(10, 5, 10, 5);
    c.anchor = GridBagConstraints.CENTER;
    // 1st row: Enter message: [____] [Send]
    c.weightx = 0;
    p1.add(f_messageLabel, c);
    c.gridx = 1;
    c.weightx = 1;
    c.gridwidth = 3;
    c.fill = GridBagConstraints.HORIZONTAL;
    p1.add(f_messageField, c);
    f_messageField.setFont(fixed);
    f_messageField.addFocusListener(f_selectOnFocus);
    c.gridx = 4;
    c.weightx = 0;
    c.fill = GridBagConstraints.NONE;
    p1.add(f_sendButton, c);

    getContentPane().add(p1, BorderLayout.NORTH);

    final JPanel p2 = new JPanel();
    p2.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), f_messages
        .getString(Messages.LAST_RESPONSE)));
    p2.setLayout(new GridBagLayout());
    c = new GridBagConstraints();
    c.weightx = c.weighty = 1;
    c.fill = GridBagConstraints.BOTH;
    f_serverResponsesTextArea.setEditable(false);
    f_serverResponsesTextArea.setFont(fixed);
    f_outputScrollPane = new JScrollPane(f_serverResponsesTextArea);
    new SmartScroller(f_outputScrollPane);
    p2.add(f_outputScrollPane, c);
    getContentPane().add(p2, BorderLayout.CENTER);

    final JPanel statusPanel = new JPanel(new BorderLayout());
    statusPanel.setBorder(BorderFactory.createTitledBorder("Current Status:"));

    f_audioButton = new JButton();
    f_audioButton.setPreferredSize(new Dimension(24, 24));
    f_audioButton.addActionListener(l -> {
      f_parser.parse(f_messages.getString(Messages.SOUND_TOGGLE_COMMAND), false);
    });
    statusPanel.add(f_audioButton, BorderLayout.EAST);

    final JPanel statusBar = new JPanel(new GridLayout(1, 6));

    f_playerName = new JLabel();
    f_playerName.setBorder(BorderFactory.createTitledBorder("Name:"));
    statusBar.add(f_playerName);

    f_location = new JLabel();
    f_location.setBorder(BorderFactory.createTitledBorder("Location:"));
    statusBar.add(f_location);

    f_health = new JLabel();
    f_health.setBorder(BorderFactory.createTitledBorder("Your HP:"));
    statusBar.add(f_health);

    f_weapon = new JLabel();
    f_weapon.setBorder(BorderFactory.createTitledBorder("Weapon:"));
    statusBar.add(f_weapon);

    f_target = new JLabel();
    f_target.setBorder(BorderFactory.createTitledBorder("Attacking:"));
    statusBar.add(f_target);

    f_targetHealth = new JLabel();
    f_targetHealth.setBorder(BorderFactory.createTitledBorder("Target HP:"));
    statusBar.add(f_targetHealth);

    statusPanel.add(statusBar, BorderLayout.CENTER);
    getContentPane().add(statusPanel, BorderLayout.SOUTH);
    getRootPane().setDefaultButton(f_sendButton);

    /**
     * Setup menu bar
     */
    final JMenuBar menuBar = new JMenuBar();
    setJMenuBar(menuBar);

    final JMenu fileMenu = new JMenu("File");
    fileMenu.setMnemonic(KeyEvent.VK_F);
    menuBar.add(fileMenu);

    final JMenuItem defaultWorldItem = new JMenuItem("New Default Universe");
    defaultWorldItem.setMnemonic(KeyEvent.VK_D);
    defaultWorldItem.addActionListener(l -> {
      f_parser.parse(f_messages.getString(Messages.LOAD_COMMAND) + " " + GamePersistence.DEFAULT_WORLD);
    });
    fileMenu.add(defaultWorldItem);

    final JMenuItem loadItem = new JMenuItem("Load Universe");
    loadItem.setMnemonic(KeyEvent.VK_L);
    loadItem.addActionListener(l -> {
      f_parser.parse(f_messages.getString(Messages.LOAD_COMMAND));
    });
    fileMenu.add(loadItem);

    final JMenuItem saveItem = new JMenuItem("Save Universe");
    saveItem.setMnemonic(KeyEvent.VK_S);
    saveItem.addActionListener(l -> {
      f_parser.parse(f_messages.getString(Messages.SAVE_COMMAND));
    });
    fileMenu.add(saveItem);

    fileMenu.addSeparator();

    final JMenuItem quitItem = new JMenuItem("Quit");
    quitItem.setMnemonic(KeyEvent.VK_X);
    quitItem.addActionListener(l -> {
      f_parser.parse(f_messages.getString(Messages.QUIT_COMMANDS).split(",")[0]);
    });
    fileMenu.add(quitItem);

    final JMenu multiplayerMenu = new JMenu("Multiplayer");
    multiplayerMenu.setMnemonic(KeyEvent.VK_M);
    menuBar.add(multiplayerMenu);

    final JMenuItem startServer = new JMenuItem("Start Server");
    startServer.addActionListener(l -> {
      final String input = (String) JOptionPane.showInputDialog(this,
          "Please choose a port number, the default is recommended.", "Start Server", JOptionPane.OK_CANCEL_OPTION,
          null, null, MultiplayerServerThread.getDefaultPort());
      if (input == null) {
        return;
      }
      try {
        final int port = Integer.parseInt(input);
        if (port < 1 || port > 65535) {
          throw new NumberFormatException();
        }
        f_parser.parse(f_messages.getString(Messages.START_SERVER_COMMAND) + " " + port);
      } catch (final NumberFormatException e) {
        JOptionPane.showMessageDialog(this,
            "Sorry, you did not input a valid number.  It must be an integer in the range of 1 through 65535", "Error",
            JOptionPane.ERROR_MESSAGE);
      }
    });
    multiplayerMenu.add(startServer);

    final JMenuItem stopServer = new JMenuItem("Stop Server");
    stopServer.addActionListener(l -> {
      f_parser.parse(f_messages.getString(Messages.STOP_SERVER_COMMAND));
    });
    multiplayerMenu.add(stopServer);

    final JMenuItem joinServer = new JMenuItem("Join Server");
    joinServer
    .addActionListener(l -> {
      final String input = JOptionPane
          .showInputDialog(
              this,
              "Please input the address of the server you are connecting to, e.g. 192.168.1.50.\nIf the server is not using the default port then input <address>:<port>",
              "Join Server", JOptionPane.OK_CANCEL_OPTION);
      if (input == null) {
        return;
      }
      f_parser.parse(f_messages.getString(Messages.CONNECT_COMMAND) + " " + input);
    });
    multiplayerMenu.add(joinServer);

    final JMenu helpMenu = new JMenu("Help");
    helpMenu.setMnemonic(KeyEvent.VK_H);
    menuBar.add(helpMenu);

    final JMenuItem showHelpItem = new JMenuItem("Display Help Messages");
    showHelpItem.addActionListener(l -> {
      final JTextArea helpText = new JTextArea(f_parser.getHelpMessage());
      helpText.setLineWrap(true);
      helpText.setWrapStyleWord(true);
      final JScrollPane helpScrollPane = new JScrollPane(helpText);
      helpScrollPane.setPreferredSize(getPreferredSize());
      final JOptionPane pane = new JOptionPane(helpScrollPane);
      final JDialog helpDialog = pane.createDialog(this, "Help");
      helpDialog.setModal(false);
      helpDialog.setResizable(true);
      helpDialog.setVisible(true);
    });
    helpMenu.add(showHelpItem);

    /**
     * The controller this user interface interacts with. The controller initially uses a default
     * world, however this can be subsequently changed by calling
     * {@link Controller#setWorld(Universe)}.
     */
    try {
      f_worldController = new Controller();
      final GraphicalParserWorldObserver graphicalParserWorldObserver = new GraphicalParserWorldObserver(
          f_serverResponsesTextArea, f_messages);

      /**
       * A text parser for game commands. This parser aggregates our world controller and invokes
       * game logic on the controller when it understands player commands to the game.
       */
      f_parser = new Parser(f_worldController, graphicalParserWorldObserver, graphicalParserWorldObserver,
          f_messages, true);
      // Update the display so the user can see the player's initial location
      f_parser.displayAvailablePlayers();

      f_sendButton.addActionListener(e -> {
        final String messageForServer = f_messageField.getText();
        previousCommands.push(messageForServer);
        f_messageField.setText(""); // clear input field on the screen
        if (messageForServer.equalsIgnoreCase(f_messages.getString(Messages.CLEAR_COMMAND))) {
          try {
            f_serverResponsesTextArea.getDocument().remove(0, f_serverResponsesTextArea.getDocument().getLength());
          } catch (final Exception e1) {
            e1.printStackTrace();
          }
        } else {
          f_parser.parse(messageForServer);
        }
      });

      f_messageField.addKeyListener(new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
          if (e.getKeyCode() == KeyEvent.VK_UP) {
            previousCommands.addLast(f_messageField.getText());
            f_messageField.setText(previousCommands.removeFirst());
          } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            previousCommands.addFirst(f_messageField.getText());
            f_messageField.setText(previousCommands.removeLast());
          }
        }
      });

      final Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
      this.setLocation(dim.width / 2 - this.getSize().width / 2, dim.height / 2 - this.getSize().height / 2);
      // make the user interface visible on the screen
      setVisible(true);

    } catch (final PersistenceStateException e) {
      e.printStackTrace();
      JOptionPane.showMessageDialog(this, "Error during loading, unable to continue: " + e.getMessage(), "ERROR",
          JOptionPane.ERROR_MESSAGE);
    }

  }

  // private boolean isViewAtBottom() {
  // final JScrollBar sb = f_outputScrollPane.getVerticalScrollBar();
  // final int min = sb.getValue() + sb.getVisibleAmount();
  // final int max = sb.getMaximum();
  // return min == max;
  // }

}
