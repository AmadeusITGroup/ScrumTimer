/*
 */
package com.amadeus.tpe.src.sca.scrumtimer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.prefs.Preferences;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author hiran.chaudhuri
 */
public class TimerPanel extends JPanel implements MouseListener, MouseMotionListener, KeyListener {
    private static final Logger log = LogManager.getLogger();

    /** the time when we want to autostart. */
    private Instant scheduledStart;
    // the total configured duration
    private Duration total = Duration.ofSeconds(1);
    // the instant when the timer was started
    private Instant started;
    // the instant when the timer will have to stop again
    private Instant stops;

    private boolean playAudio = false;
    private Timer timer;
    
    private JEditorPane gist;

    public TimerPanel() {
        reset();
        addMouseListener(this);
        addMouseMotionListener(this);
        addKeyListener(this);
        addAncestorListener(new AncestorListener() {
            @Override
            public void ancestorAdded(AncestorEvent event) {
                //System.out.println("ancestorAdded "+event);
                loadPreferences();
            }

            @Override
            public void ancestorRemoved(AncestorEvent event) {
                //System.out.println("ancestorRemoved "+event);
                //loadPreferences();
            }

            @Override
            public void ancestorMoved(AncestorEvent event) {
                //System.out.println("ancestorMoved "+event);
                //loadPreferences();
            }
        });
        setOpaque(true);
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Instant now = Instant.now();
                if (scheduledStart != null) {
                    if (now.isBefore(scheduledStart) && !isRunning()) {
                        //log.debug("Will start in {}", Duration.between(scheduledStart, now));
                    } else if ((!now.isBefore(scheduledStart)) && !isRunning()) {
                        started = scheduledStart;                        
                        stops = scheduledStart.plus(total);
                        scheduledStart = null;
                    }
                }
                if (stops != null && now.isAfter(stops)) {
                    stop();
                    scheduledStart = null;
                }
                
                repaint(300);
            }
        }, 500, 500);
        setBackground(new Color(0, 0, 0, 0));
    }

    public boolean isPlayAudio() {
        return playAudio;
    }

    public void setPlayAudio(boolean playAudio) {
        this.playAudio = playAudio;
    }

    public void setTotalDuration(Duration total) {
        this.total = total;
        reset();
    }

    public Duration getTotalDuration() {
        return total;
    }

    public void start() {
        started = Instant.now();
        stops = Instant.now().plus(total);
    }

    public void stop() {
        started = null;
        stops = null;
        
        if (playAudio) {
          Toolkit.getDefaultToolkit().beep();
        }
    }

    public boolean isRunning() {
        return started != null;
    }

    public void reset() {
        stop();
    }
    
    public void setGist(JEditorPane gist) {
        this.gist = gist;
    }

    public static String getHumanReadableTime(long seconds) {
        if (seconds >= 3600) {
            return String.format("%2d h %02d m", seconds / 3600, (seconds % 3600) / 60);
        } else if (seconds > 60) {
            return String.format("%2d m %02d s", (seconds % 3600) / 60, seconds % 60);
        } else {
            return String.format("%2d s", seconds);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setBackground(getBackground());
        g2d.clearRect(0, 0, getWidth(), getHeight());
        
        int squaresize = Math.min(getWidth(), getHeight());
        int offsetx = (getWidth()-squaresize)/2;
        int offsety = (getHeight()-squaresize)/2;

        if (isRunning()) {
            Duration passed = Duration.between(started, Instant.now());
            Duration remaining = total.minus(passed);

            long angle = (360 * remaining.getSeconds()) / total.getSeconds();
            //System.out.println("t" + total.getSeconds() + "  p" + passed.getSeconds() + "  r" + remaining.getSeconds() + " -> "+angle);

            String d = getHumanReadableTime(remaining.getSeconds());
            StringBuilder msg = new StringBuilder(String.format("Render %d -> %s", remaining.getSeconds(), d));
            log.debug(msg.toString());

            // calculate position
            FontMetrics metrics = g2d.getFontMetrics();
            Rectangle2D rect = metrics.getStringBounds(d, g2d);
            Point strPos = new Point((int) ((getWidth() - rect.getWidth()) / 2), (int) ((getHeight() - rect.getHeight()) / 2) + metrics.getAscent());

            g2d.setColor(new Color(0, 0, 0, 128));
            //g2d.fillRect(strPos.x-1, (int) ((getHeight() - rect.getHeight()) / 2)-1, (int)rect.getWidth()+2, (int)rect.getHeight()+2);
            g2d.drawString(d, strPos.x + 1, strPos.y + 1);
            g2d.drawString(d, strPos.x + 1, strPos.y - 1);
            g2d.drawString(d, strPos.x - 1, strPos.y + 1);
            g2d.drawString(d, strPos.x - 1, strPos.y - 1);

            g2d.setColor(Color.red);
            g2d.fillArc(offsetx, offsety, squaresize, squaresize, 90, (int) angle);

            g2d.setColor(Color.black);
            g2d.drawString(d, strPos.x + 2, strPos.y + 2);
            g2d.setColor(Color.white);
            g2d.drawString(d, strPos.x, strPos.y);
        } else {
            g2d.setColor(Color.gray);
            g2d.fillArc(offsetx, offsety, squaresize, squaresize, 0, 360);

            String d = getHumanReadableTime(total.getSeconds());
            StringBuilder msg = new StringBuilder(String.format("Render %d -> %s", total.getSeconds(), d));
            if (scheduledStart != null) {
                msg.append(String.format(", starting at %s", scheduledStart));
            }
            log.debug(msg.toString());

            // calculate position
            FontMetrics metrics = g2d.getFontMetrics();
            Rectangle2D rect = metrics.getStringBounds(d, g2d);
            Point strPos = new Point((int) ((getWidth() - rect.getWidth()) / 2), (int) ((getHeight() - rect.getHeight()) / 2) + metrics.getAscent());

            g2d.setColor(Color.black);
            g2d.drawString(d, strPos.x + 2, strPos.y + 2);
            g2d.setColor(Color.white);
            g2d.drawString(d, strPos.x + 1, strPos.y);
        }
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(100, 100);
    }

    @Override
    public Dimension getPreferredSize() {
        return getMinimumSize();
    }

    /**
     * left click starts/stops the timer left doubleclick resets the timer right
     * click opens context menu
     *
     * @param e
     */
    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.isPopupTrigger() || SwingUtilities.isRightMouseButton(e)) {
            // show context menu (if not shown already)

            JMenuItem JMIabout = new JMenuItem("About...");
            JMIabout.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // show properties dialog
                    JLabel label = new JLabel("<html>ScrumTimer created by Hiran Chaudhuri<br/>Running on "+System.getProperty("java.vm.vendor")+" "+System.getProperty("java.vm.version")+"</html>");
                    JWindow window = (JWindow) SwingUtilities.getWindowAncestor(TimerPanel.this);
                    window.setAlwaysOnTop(false);
                    JOptionPane.showMessageDialog(TimerPanel.this, label);
                    window.setAlwaysOnTop(true);
                }

            });
            JMenuItem JMIconfigure = new JMenuItem("Settings...");
            JMIconfigure.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // show properties dialog
                    ConfigurationPanel cp = new ConfigurationPanel();
                    cp.setDuration(total);
                    cp.setPlaySound(playAudio);
                    JWindow window = (JWindow) SwingUtilities.getWindowAncestor(TimerPanel.this);
                    window.setAlwaysOnTop(false);
                    if (JOptionPane.showConfirmDialog(TimerPanel.this, cp, "Settings", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                        // if successfull, take data
                        try {
                            scheduledStart = cp.getStart();
                            total = cp.getDuration();
                            playAudio = cp.getPlaySound();
                            log.debug("Configured start={}, duration={}", scheduledStart, total);
                            savePreferences();
                            reset();
                        } catch (DateTimeParseException dtpe) {
                            // do nothing
                        }
                    }
                    window.setAlwaysOnTop(true);
                }

            });
            JMenuItem JMIquit = new JMenuItem("Exit");
            JMIquit.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    System.exit(0);
                }
            });

            JMenuItem JM15m = new JMenuItem("15m");
            JM15m.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setTotalDuration(Duration.ofMinutes(15));
                }
            });

            JMenuItem JM50m = new JMenuItem("50m");
            JM50m.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setTotalDuration(Duration.ofMinutes(50));
                }
            });

            JMenuItem JM110m = new JMenuItem("110m");
            JM110m.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setTotalDuration(Duration.ofMinutes(110));
                }
            });

            JPopupMenu menu = new JPopupMenu("Timer");
            menu.add(JMIabout);
            menu.add(new JSeparator());
            menu.add(JM15m);
            menu.add(JM50m);
            menu.add(JM110m);
            menu.add(new JSeparator());
            menu.add(JMIconfigure);
            menu.add(JMIquit);
            menu.show(this, e.getX(), e.getY());

            e.consume();
        } else if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
            // reset the timer
            reset();
            e.consume();
        } else if (e.getClickCount() == 1 && SwingUtilities.isLeftMouseButton(e)) {
            // start/stop the timer
            if (isRunning()) {
                stop();
            } else {
                start();
            }
            e.consume();
        }
    }

    private Point panelDragOffset;

    @Override
    public void mousePressed(MouseEvent e) {
        Point windowLoc = SwingUtilities.getWindowAncestor(this).getLocation();
        panelDragOffset = new Point(e.getXOnScreen() - windowLoc.x, e.getYOnScreen() - windowLoc.y);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        if (gist != null) {
            gist.setVisible(true);
        }
    }

    @Override
    public void mouseExited(MouseEvent e) {
        if (gist != null) {
            gist.setVisible(false);
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        SwingUtilities.getWindowAncestor(this).setLocation(e.getXOnScreen() - panelDragOffset.x, e.getYOnScreen() - panelDragOffset.y);
        savePreferences();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
        switch (e.getKeyChar()) {

            case ' ':
                if (isRunning()) {
                    stop();
                } else {
                    start();
                }
                break;
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    /**
     * Checks whether the given coordinate is visible on one of the attached
     * screens.
     * @param location
     * @return 
     */
    private boolean isVisible(Point location) {
        GraphicsDevice[] gds = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
        for(GraphicsDevice gd: gds) {
            Rectangle r = gd.getDefaultConfiguration().getBounds();
            //System.out.println(gd.getIDstring()+"  "+r);
            if(r.contains(location))
                return true;
        }
        return false;
    }

    private void loadPreferences() {
        Preferences prefs = Preferences.userRoot().node(getClass().getName());
        total = Duration.parse(prefs.get("totalduration", "PT10M"));

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        Window window = SwingUtilities.getWindowAncestor(this);
        Point defaultLocation = null;
        if (window != null) {
            defaultLocation = new Point((int) (screenSize.getWidth() - window.getWidth()) / 2, (int) (screenSize.getHeight() - window.getHeight()) / 2);
        } else {
            defaultLocation = new Point((int) screenSize.getWidth() / 2, (int) screenSize.getHeight() / 2);
        }
        Point location = new Point(prefs.getInt("locationX", defaultLocation.x), prefs.getInt("locationY", defaultLocation.y));
        if (window != null) {
            if(!isVisible(location)) {
                location = defaultLocation;
            }
            System.out.println("Positioning at "+location);
            window.setLocation(location);
        }
        
        playAudio = prefs.getBoolean("playAudio", false);
    }

    private void savePreferences() {
        Preferences prefs = Preferences.userRoot().node(getClass().getName());
        prefs.put("totalduration", total.toString());

        Point p = SwingUtilities.getWindowAncestor(this).getLocation();
        prefs.putInt("locationX", p.x);
        prefs.putInt("locationY", p.y);
        prefs.putBoolean("playAudio", playAudio);
    }
}
