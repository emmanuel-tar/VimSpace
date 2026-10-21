package com.vimspace.boh.panel;

import com.vimspace.boh.service.BohApiClient;
import com.vimspace.contract.CreateSupplierRequest;
import com.vimspace.contract.SupplierDTO;
import com.vimspace.contract.UserDTO;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class SupplierPanel extends JPanel {
    private final BohApiClient api;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JTextField nameField;
    private JTextField contactField;
    private JTextField phoneField;
    private JTextField emailField;
    private JTextArea addressArea;
    private JPanel formPanel;
    private JLabel statusLabel;

    public SupplierPanel(BohApiClient api) {
        this.api = api;
        setLayout(new MigLayout("fill, insets 5 10 5 10", "[grow,fill]", "[]0[][][]"));

        add(new JLabel("Suppliers", SwingConstants.CENTER), "span 2, growx, aligny top");
        add(new JLabel("Manage suppliers, contacts, and purchase orders."), "span 2, aligny top, wrap");

        searchField = new JTextField(20);
        searchField.setName("supplierSearch");
        JButton searchBtn = new JButton("Search");
        searchBtn.setToolTipText("Search suppliers by name");
        searchBtn.addActionListener(e -> loadSuppliers());
        add(new JLabel("Search:"), "aligny top");
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);
        add(searchPanel, "aligny top, wrap");

        tableModel = new DefaultTableModel(
                new Object[]{"ID", "Name", "Contact", "Phone", "Email", "Address", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setFillsViewportHeight(true);
        table.setRowHeight(22);
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(180);
        table.getColumnModel().getColumn(2).setPreferredWidth(150);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(150);
        table.getColumnModel().getColumn(5).setPreferredWidth(250);
        table.getColumnModel().getColumn(6).setPreferredWidth(80);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(800, 220));
        add(scrollPane, "grow, span 2");

        JButton newBtn = new JButton("New");
        JButton editBtn = new JButton("Edit");
        JButton deleteBtn = new JButton("Delete");
        JButton closeBtn = new JButton("Close");
        newBtn.addActionListener(e -> showForm(null));
        editBtn.addActionListener(e -> editSelected());
        deleteBtn.addActionListener(e -> deleteSelected());
        closeBtn.addActionListener(e -> {
            formPanel.setVisible(false);
            revalidate();
            repaint();
        });
        add(newBtn, "width 80, split 3");
        add(editBtn, "width 80");
        add(deleteBtn, "width 80");
        add(closeBtn, "width 80");

        formPanel = new JPanel(new MigLayout("fill, ins 5 10 5 10", "[grow,fill]", "[]0[][][]"));
        formPanel.setVisible(false);

        nameField = new JTextField(20);
        contactField = new JTextField(20);
        phoneField = new JTextField(20);
        emailField = new JTextField(20);
        addressArea = new JTextArea(4, 20);
        JScrollPane addressScroll = new JScrollPane(addressArea);

        statusLabel = new JLabel(" ");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);

        formPanel.add(new JLabel("Name:"), "aligny top");
        formPanel.add(nameField, "growx");
        formPanel.add(new JLabel("Contact:"), "aligny top, split 2");
        formPanel.add(contactField, "growx");
        formPanel.add(new JLabel("Phone:"), "aligny top, split 2");
        formPanel.add(phoneField, "growx");
        formPanel.add(new JLabel("Email:"), "aligny top, split 2");
        formPanel.add(emailField, "growx");
        formPanel.add(new JLabel("Address:"), "aligny top, wrap");
        formPanel.add(addressScroll, "growx, h 80:200:200");
        formPanel.add(statusLabel, "aligny bottom, split 2, growx");
        formPanel.add(new JButton("Save"), "width 80, aligny top");
        formPanel.add(new JButton("Cancel"), "width 80, aligny top");

        add(formPanel, "aligny top, wrap");

        loadSuppliers();
    }

    private void loadSuppliers() {
        try {
            String q = searchField.getText().trim();
            List<SupplierDTO> suppliers = q.isEmpty() ? api.getSuppliers() : api.searchSuppliers(q);
            tableModel.setRowCount(0);
            for (SupplierDTO s : suppliers) {
                boolean active = s.getActive() != null && s.getActive();
                tableModel.addRow(new Object[]{
                        s.getId(), s.getName(), s.getContactPerson(), s.getPhone(),
                        s.getEmail(), s.getAddress(), active ? "Active" : "Inactive"
                });
            }
            statusLabel.setText("Found " + tableModel.getRowCount() + " supplier(s)");
        } catch (Exception ex) {
            statusLabel.setText("Error loading suppliers: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void showForm(SupplierDTO existing) {
        formPanel.setVisible(true);
        nameField.setText(existing != null ? existing.getName() : "");
        contactField.setText(existing != null ? existing.getContactPerson() : "");
        phoneField.setText(existing != null ? existing.getPhone() : "");
        emailField.setText(existing != null ? existing.getEmail() : "");
        addressArea.setText(existing != null ? existing.getAddress() : "");
        statusLabel.setText(existing != null ? "Editing supplier ID " + existing.getId() : "New supplier");
        nameField.requestFocus();
    }

    private void save() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            statusLabel.setText("Name is required");
            return;
        }

        try {
            CreateSupplierRequest req = new CreateSupplierRequest(
                    name, contactField.getText().trim(), phoneField.getText().trim(),
                    emailField.getText().trim(), addressArea.getText().trim()
            );
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                Long id = (Long) tableModel.getValueAt(selectedRow, 0);
                api.updateSupplier(id, req);
                statusLabel.setText("Saved!");
            } else {
                api.createSupplier(req);
                statusLabel.setText("Created!");
            }
            loadSuppliers();
            formPanel.setVisible(false);
            searchField.requestFocus();
        } catch (Exception ex) {
            statusLabel.setText("Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void editSelected() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            statusLabel.setText("Please select a supplier to edit");
            return;
        }
        try {
            Long id = (Long) tableModel.getValueAt(selectedRow, 0);
            showForm(api.getSupplierById(id));
            nameField.requestFocus();
        } catch (Exception ex) {
            statusLabel.setText("Error: " + ex.getMessage());
        }
    }

    private void deleteSelected() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            statusLabel.setText("Please select a supplier to delete");
            return;
        }
        Long id = (Long) tableModel.getValueAt(selectedRow, 0);
        String name = (String) tableModel.getValueAt(selectedRow, 1);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete supplier '" + name + "'?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                api.deleteSupplier(id);
                loadSuppliers();
                statusLabel.setText("Deleted!");
            } catch (Exception ex) {
                statusLabel.setText("Error: " + ex.getMessage());
            }
        }
    }

    public void refresh() {
        loadSuppliers();
    }
}