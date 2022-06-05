package root.gui.registrar;

import root.tasks.IDisposable;
import root.tasks.ILanguageUpdateble;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import static javax.swing.UIManager.getString;

public class RegistrarDialog extends JDialog implements IDisposable, ILanguageUpdateble {
    private final JLabel loginLabel, passwordLabel;
    private final JTextField loginField;
    private final JPasswordField passwordField;
    private final JButton authorizationButton, registerButton;

    public RegistrarDialog(Frame owner) {
        super(owner, true);
        setContentPane(Box.createVerticalBox());
        Dimension labelSize = new Dimension(120, 20);
        final int fieldColumns = 17;

        loginLabel = new JLabel(); loginField = new JTextField(fieldColumns);
        loginLabel.setPreferredSize(labelSize);
        loginLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        addLine(10, loginLabel, loginField);

        passwordLabel = new JLabel(); passwordField = new JPasswordField(fieldColumns);
        passwordLabel.setPreferredSize(labelSize);
        passwordLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        addLine(10, passwordLabel, passwordField);

        authorizationButton = new JButton(); registerButton = new JButton();
        addLine(35, authorizationButton, registerButton);

        Dimension dimension = new Dimension(450, 190);
        setSize(dimension); setMinimumSize(dimension);
        setLocationRelativeTo(null);

        updateLanguage();
    }

    private void addLine(int hgap, Component... components) {
        getContentPane().add(Box.createGlue());
        FlowLayout flowLayout = new FlowLayout(); flowLayout.setHgap(hgap);
        JPanel panel = new JPanel(flowLayout); getContentPane().add(panel);
        for (Component component : components)
            panel.add(component);
    }

    @Override
    public void updateLanguage() {
        setTitle(getString("registrar.title"));
        authorizationButton.setText(getString("registrar.avt"));
        registerButton.setText(getString("registrar.reg"));
        loginLabel.setText(getString("registrar.login") + ":");
        passwordLabel.setText(getString("registrar.password") + ":");
    }

    public void setExit(Runnable exit) {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                exit.run();
            }
        });
    }

    public JButton getAuthorizationButton() {
        return authorizationButton;
    }

    public JButton getRegisterButton() {
        return registerButton;
    }

    public JTextField getLoginField() {
        return loginField;
    }

    public JPasswordField getPasswordField() {
        return passwordField;
    }
}