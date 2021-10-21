package root.gui;

import root.tasks.IDisposable;
import root.tasks.ILanguageUpdateble;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Printer extends JDialog implements IDisposable, ILanguageUpdateble {
    private final JTextArea textArea;

    public Printer() {
        updateLanguage();
        setLocationRelativeTo(null);
        Dimension dimension = new Dimension(600, 400);
        setSize(dimension); setPreferredSize(dimension);
        textArea = new JTextArea();
        textArea.setEditable(false);
        setContentPane(new JScrollPane(textArea)); // scrollPane.setViewportView(textArea);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                textArea.setText("");
                setVisible(false);
            }
        });
    }

    @Override
    public void updateLanguage() {
        setTitle(UIManager.getString("printer.title"));
    }

    public void print(String str) {
        textArea.append(str + "\n\n");
    }
}