/*
 */
package com.amadeus.tpe.src.sca.scrumtimer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.time.Duration;
import javax.swing.JWindow;

/**
 * This class is the main entrypoint that builds the undecorated always-on-top
 * window for the application to run in.
 * 
 * @author hiran.chaudhuri
 */
public class MainFrame extends JWindow {

    private TimerPanel timerPanel;

    /**
     * Creates a new application window.
     */
    public MainFrame() {
        //setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());
        
        timerPanel = new TimerPanel();
        getContentPane().add(timerPanel, BorderLayout.NORTH);
        
//        JEditorPane gist = new JEditorPane();
//        gist.setContentType("text/html");
//        gist.setText("<html><body><ul><li>What did I achieve?</li><li>What will I do next?</li><li>Where do I need help?</li></ul></body></html>");
//        gist.setBorder(new CompoundBorder(new TitledBorder("Gist"), new EmptyBorder(0, 0, 0, 20)));
//        gist.setBackground(new Color(255, 255, 180));
//        getContentPane().add(gist, BorderLayout.SOUTH);
//        
//        
//        timerPanel.setGist(gist);
        
        //setUndecorated(true);
        setAlwaysOnTop(true);
        setBackground(new Color(0,0,0,0));
        pack();
    }
    
    /**
     * Set the duration this instance shall cover.
     * 
     * @param d the duration
     */
    public void setTotalDuration(Duration d) {
        timerPanel.setTotalDuration(d);
    }

    /**
     * The main entry point for the application.
     * Allows one parameter (the duration string) to be set.
     * 
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // Determine if the GraphicsDevice supports translucency.
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        
        //If translucent windows aren't supported, exit.
        if (!gd.isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.TRANSLUCENT)) {
            System.err.println("Translucency is not supported");
            System.exit(0);
        }
        
        if (args.length > 1) {
            System.err.println("Only one parameter (duration) is supported");
            System.exit(1);
        }
        
        try {
            MainFrame mainframe = new MainFrame();
            if (args.length == 1) {
                mainframe.setTotalDuration(Duration.parse(args[0]));
            }
            mainframe.setVisible(true);
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(2);
        }
    }
}
