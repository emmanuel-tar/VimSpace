package com.vimspace.foh.ui;

import com.vimspace.contract.LoginResponse;
import com.vimspace.foh.service.ApiClient;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;

public class LoginFrame extends JFrame {
    private final ApiClient apiClient;
    private final JComboBox<String> userField = new JComboBox<>(new String[]{"cashier", "manager", "admin"});
    private final JPasswordField passField = new JPasswordField(18);
    private final JLabel status = new JLabel(" ");
    private final JButton loginBtn = new JButton("Login");

    public LoginFrame(ApiClient apiClient) {
        super("VimSpace FrontRetail — Login");
        this.apiClient = apiClient;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(380, 240);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.anchor = GridBagConstraints.WEST;

        g.gridx = 0; g.gridy = 0; form.add(new JLabel("Username:"), g);
        g.gridx = 1; form.add(userField, g);
        g.gridx = 0; g.gridy = 1; form.add(new JLabel("Password:"), g);
        g.gridx = 1; form.add(passField, g);
        userField.setPreferredSize(new Dimension(200, 28));
        passField.setPreferredSize(new Dimension(200, 28));
        userField.setEditable(true);

        loginBtn.setPreferredSize(new Dimension(200, 32));
        loginBtn.addActionListener(e -> attemptLogin());
        passField.addActionListener(e -> attemptLogin());
        g.gridx = 0; g.gridy = 2; g.gridwidth = 2; g.anchor = GridBagConstraints.CENTER;
        form.add(loginBtn, g);

        status.setForeground(new Color(180, 40, 40));
        g.gridy = 3;
        form.add(status, g);

        add(form);
    }

    private void attemptLogin() {
        String u = String.valueOf(userField.getSelectedItem()).trim();
        String p = new String(passField.getPassword()).trim();

        if (u.isEmpty() || p.isEmpty()) {
            status.setText("Please enter username and password");
            return;
        }

        loginBtn.setEnabled(false);
        status.setText("Authenticating with backend...");

        new SwingWorker<Optional<LoginResponse>, Void>() {
            @Override
            protected Optional<LoginResponse> doInBackground() {
                return apiClient.login(u, p);
            }

            @Override
            protected void done() {
                loginBtn.setEnabled(true);
                try {
                    Optional<LoginResponse> response = get();
                    if (response.isPresent()) {
                        LoginResponse res = response.get();
                        status.setText("Login successful");
                        dispose();
                        SplashScreen splash = new SplashScreen();
                        splash.setVisible(true);

                        new SwingWorker<Void, Void>() {
                            @Override
                            protected Void doInBackground() throws Exception {
                                Thread.sleep(1200);
                                return null;
                            }

                            @Override
                            protected void done() {
                                splash.dispose();
                                new HomeFrame(apiClient, res.getUsername(), res.getRole()).setVisible(true);
                            }
                        }.execute();
                    } else {
                        status.setText("Invalid credentials or backend offline");
                    }
                } catch (Exception ex) {
                    status.setText("Login failed: " + ex.getMessage());
                }
            }
        }.execute();
    }
}
