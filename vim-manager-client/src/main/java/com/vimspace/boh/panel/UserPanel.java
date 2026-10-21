package com.vimspace.boh.panel;

import com.vimspace.boh.config.AppConfig;
import com.vimspace.boh.service.ApiException;
import com.vimspace.boh.service.BohApiClient;
import com.vimspace.contract.CreateUserRequest;
import com.vimspace.contract.UserDTO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Tab 4: Staff & User Accounts — create users, toggle active/inactive.
 */
public class UserPanel extends JPanel {

    private final AppConfig config;
    private final BohApiClient apiClient;

    private final DefaultTableModel tableModel;
    private final JTable table;

    public UserPanel(AppConfig config, BohApiClient apiClient) {
        this.config = config;
        this.apiClient = apiClient;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JButton addBtn = new JButton("+ Add Staff User");
        JButton toggleBtn = new JButton("Toggle Active/Inactive");
        JButton refreshBtn = new JButton("Refresh Users");
        addBtn.addActionListener(e -> showAddUserDialog());
        toggleBtn.addActionListener(e -> toggleUser());
        refreshBtn.addActionListener(e -> loadUsers());
        toolbar.add(addBtn);
        toolbar.add(toggleBtn);
        toolbar.add(refreshBtn);

        String[] cols = {"ID", "Username", "Role", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(25);

        add(toolbar, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        loadUsers();
    }

    private void loadUsers() {
        new SwingWorker<List<UserDTO>, Void>() {
            private String error;
            @Override protected List<UserDTO> doInBackground() {
                try { return apiClient.getAllUsers(); } catch (ApiException e) { error = e.getMessage(); return null; }
            }
            @Override protected void done() {
                tableModel.setRowCount(0);
                if (error != null) { JOptionPane.showMessageDialog(UserPanel.this, "Failed to load users: " + error); return; }
                try {
                    List<UserDTO> list = get();
                    if (list == null) return;
                    for (UserDTO u : list) {
                        tableModel.addRow(new Object[]{
                                u.getId(), u.getUsername(), u.getRole(),
                                Boolean.TRUE.equals(u.getActive()) ? "Active" : "Disabled"
                        });
                    }
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    private void showAddUserDialog() {
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "Add New User Account", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(380, 280);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new GridBagLayout());

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 8, 6, 8);
        g.anchor = GridBagConstraints.WEST;

        JTextField userField = new JTextField(18);
        JPasswordField passField = new JPasswordField(18);
        String[] roles = {"Cashier", "Store Manager", "System Administrator"};
        JComboBox<String> roleCombo = new JComboBox<>(roles);
        roleCombo.setPreferredSize(new Dimension(200, 26));

        g.gridx = 0; g.gridy = 0; dlg.add(new JLabel("Username:"), g);
        g.gridx = 1; dlg.add(userField, g);
        g.gridx = 0; g.gridy = 1; dlg.add(new JLabel("Password:"), g);
        g.gridx = 1; dlg.add(passField, g);
        g.gridx = 0; g.gridy = 2; dlg.add(new JLabel("Role:"), g);
        g.gridx = 1; dlg.add(roleCombo, g);

        JButton saveBtn = new JButton("Create User");
        saveBtn.addActionListener(e -> {
            String u = userField.getText().trim();
            String p = new String(passField.getPassword()).trim();
            if (u.isEmpty() || p.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Username and password cannot be empty.");
                return;
            }
            CreateUserRequest req = new CreateUserRequest(u, p, (String) roleCombo.getSelectedItem());

            new SwingWorker<UserDTO, Void>() {
                private String error;
                @Override protected UserDTO doInBackground() {
                    try { return apiClient.createUser(req); } catch (ApiException ex) { error = ex.getMessage(); return null; }
                }
                @Override protected void done() {
                    if (error != null) {
                        JOptionPane.showMessageDialog(dlg, "Failed to create user: " + error);
                    } else {
                        JOptionPane.showMessageDialog(dlg, "User account created!");
                        dlg.dispose();
                        loadUsers();
                    }
                }
            }.execute();
        });

        g.gridx = 0; g.gridy = 3; g.gridwidth = 2; g.anchor = GridBagConstraints.CENTER;
        dlg.add(saveBtn, g);
        dlg.setVisible(true);
    }

    private void toggleUser() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a user to toggle status.");
            return;
        }
        Long id = Long.valueOf(tableModel.getValueAt(row, 0).toString());

        new SwingWorker<UserDTO, Void>() {
            private String error;
            @Override protected UserDTO doInBackground() {
                try { return apiClient.toggleUserActive(id); } catch (ApiException e) { error = e.getMessage(); return null; }
            }
            @Override protected void done() {
                if (error != null) {
                    JOptionPane.showMessageDialog(UserPanel.this, "Toggle failed: " + error);
                } else {
                    loadUsers();
                }
            }
        }.execute();
    }
}