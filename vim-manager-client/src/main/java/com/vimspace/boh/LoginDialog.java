package com.vimspace.boh;

import com.formdev.flatlaf.FlatLightLaf;
import com.vimspace.boh.config.AppConfig;
import com.vimspace.boh.service.ApiException;
import com.vimspace.boh.service.BohApiClient;
import com.vimspace.contract.LoginRequest;
import com.vimspace.contract.LoginResponse;

import javax.swing.*;
import java.awt.*;

/**
 * Modal login dialog. On successful authentication, stores the session token
 * in AppConfig and returns {@code true}; otherwise returns {@code false}.
 */
public class LoginDialog extends JDialog {

    private final AppConfig config;
    private final BohApiClient apiClient;
    private boolean authenticated = false;

    private JTextField userField;
    private JPasswordField passField;
    private JLabel errorLabel;

    public LoginDialog(JFrame owner, AppConfig config, BohApiClient apiClient) {
        super(owner, "Vim Manager — Login", true);
        this.config = config;
        this.apiClient = apiClient;

        setSize(400, 260);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("VimSpace Back of House", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        root.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.anchor = GridBagConstraints.WEST;

        g.gridx = 0; g.gridy = 0;
        form.add(new JLabel("Username:"), g);
        g.gridx = 1; g.gridwidth = 2; g.fill = GridBagConstraints.HORIZONTAL;
        userField = new JTextField(20);
        form.add(userField, g);

        g.gridx = 0; g.gridy = 1; g.gridwidth = 1; g.fill = GridBagConstraints.NONE;
        form.add(new JLabel("Password:"), g);
        g.gridx = 1; g.gridwidth = 2; g.fill = GridBagConstraints.HORIZONTAL;
        passField = new JPasswordField(20);
        form.add(passField, g);

        g.gridx = 0; g.gridy = 2; g.gridwidth = 3; g.fill = GridBagConstraints.NONE;
        errorLabel = new JLabel("");
        errorLabel.setForeground(Color.RED);
        errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        form.add(errorLabel, g);

        root.add(form, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        JButton loginBtn = new JButton("Login");
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        loginBtn.addActionListener(e -> attemptLogin());
        buttons.add(cancelBtn);
        buttons.add(loginBtn);

        root.add(buttons, BorderLayout.SOUTH);

        // Enter key triggers login
        getRootPane().setDefaultButton(loginBtn);

        add(root);
        pack();
        setLocationRelativeTo(owner);
    }

    private void attemptLogin() {
        String username = userField.getText().trim();
        String password = new String(passField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter both username and password.");
            return;
        }

        errorLabel.setText("Logging in...");
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        new javax.swing.SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    LoginResponse resp = apiClient.login(new LoginRequest(username, password));
                    config.setAuthToken(resp.getToken());
                    config.setLoggedInUsername(resp.getUsername());
                    config.setLoggedInRole(resp.getRole());
                    authenticated = true;
                } catch (ApiException ex) {
                    String msg = ex.getStatusCode() == 401
                            ? "Invalid username or password."
                            : "Login failed: " + ex.getMessage();
                    SwingUtilities.invokeLater(() -> errorLabel.setText(msg));
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() ->
                            errorLabel.setText("Cannot connect to backend: " + ex.getMessage()));
                }
                return null;
            }

            @Override
            protected void done() {
                setCursor(null);
                if (authenticated) {
                    dispose();
                }
            }
        }.execute();
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> {
            AppConfig config = new AppConfig();
            BohApiClient api = new BohApiClient(config);
            LoginDialog dlg = new LoginDialog(new JFrame(), config, api);
            dlg.setVisible(true);
            if (dlg.isAuthenticated()) {
                JOptionPane.showMessageDialog(null, "Logged in as " + config.getLoggedInUsername());
            } else {
                JOptionPane.showMessageDialog(null, "Login cancelled or failed.");
            }
        });
    }
}
