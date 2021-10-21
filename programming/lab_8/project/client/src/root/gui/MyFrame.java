package root.gui;

import root.tasks.IDisposable;
import root.tasks.ILanguageUpdateble;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MyFrame extends JFrame implements IDisposable, ILanguageUpdateble {
    public MyFrame(JPanel leftPanel, JPanel rightPanel) {
        ImageIcon icon = new ImageIcon("images/icon.png");
        setIconImage(icon.getImage());

        getContentPane().add(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel));

        Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(getGraphicsConfiguration());
        int taskHeight = screenInsets.bottom;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        setLocation(0, 0);
        Dimension dimension = new Dimension(screenSize.width, screenSize.height - taskHeight);
        setSize(dimension);
        setPreferredSize(dimension);

        //pack();
        //setLocationRelativeTo(null);
        setVisible(true);
        setOpacity(0);
    }

    @Override
    public void updateLanguage() {
        setTitle(UIManager.getString("frame.title"));
    }

    public void setExit(Runnable exit) {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                exit.run();
            }
        });
    }
}