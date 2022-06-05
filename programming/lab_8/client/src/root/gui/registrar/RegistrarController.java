package root.gui.registrar;

import root.User;
import root.connection.Connector;
import root.tasks.ILanguageUpdateble;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.locks.ReentrantLock;

import static javax.swing.UIManager.getString;

public class RegistrarController implements ILanguageUpdateble {
    private final ReentrantLock locker = new ReentrantLock();
    private boolean first = true;

    private User user;
    private final Connector connector;
    private final RegistrarDialog dialog;
    private final JOptionPane optionPane = new JOptionPane(null, JOptionPane.ERROR_MESSAGE);

    public RegistrarController(RegistrarDialog dialog, Connector connector) {
        this.connector = connector;
        this.dialog = dialog;

        dialog.getLoginField().addCaretListener(event -> dialog.getLoginField().setForeground(Color.BLACK));
        dialog.getPasswordField().addCaretListener(event -> dialog.getPasswordField().setForeground(Color.BLACK));

        dialog.getAuthorizationButton().addActionListener((event) ->
                action(false, () -> {
                    dialog.getLoginField().setForeground(Color.RED);
                    dialog.getPasswordField().setForeground(Color.RED);
                }));
        dialog.getRegisterButton().addActionListener((event) ->
                action(true, () -> dialog.getLoginField().setForeground(Color.RED)));

        updateLanguage();
    }

    private void action(boolean reg, Runnable fail) {
        new Thread(() -> {
            if (locker.tryLock()) {
                try {
                    if (dialog.getLoginField().getText().isEmpty()) {
                        invalidInput("empty_login"); return;
                    } if (dialog.getPasswordField().getPassword().length == 0) {
                        invalidInput("empty_password"); return;
                    }
                    if (first) {
                        connector.connect();
                        first = false;
                    }
                    connector.setUser(user = new User(
                            dialog.getLoginField().getText(),
                            new String(dialog.getPasswordField().getPassword())
                    ));
                    if (connector.registration(reg)) {
                        dialog.setVisible(false);
                    } else {
                        fail.run();
                    }
                } finally { locker.unlock(); }
            }
        }).start();
    }

    private void invalidInput(String error) {
        optionPane.setMessage(getString("registrar." + error));
        Dialog dialog = optionPane.createDialog(getString("registrar.empty.title"));
        dialog.setVisible(true);
        dialog.dispose();
    }

    @Override
    public void updateLanguage() {
        optionPane.setLocale(UIManager.getDefaults().getDefaultLocale());
        optionPane.updateUI();
    }

    public User getUser() {
        return user;
    }
}
