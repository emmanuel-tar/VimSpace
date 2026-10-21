package com.vimspace.boh.panel;

import com.vimspace.boh.service.BohApiClient;
import com.vimspace.contract.*;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PurchaseOrderPanel extends JPanel {
    private final BohApiClient api;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> supplierCombo;
    private JTextField poNumberField;
    private JTextField skuField;
    private JTextField nameField;
    private JTextField qtyField;
    private JTextField costField;
    private JTextField expDateField;
    private JTextField receivePoField;
    private JPanel createPanel;
    private JPanel receivePanel;
    private JLabel statusLabel;
    private JLabel errorLabel;

    public PurchaseOrderPanel(BohApiClient api) {
        this.api = api;
        setLayout(new MigLayout("fill, insets 5 10 5 10", "[grow,fill]", "[]0[][][]"));

        add(new JLabel("Purchase Orders", SwingConstants.CENTER), "span 2, growx, aligny top");
        add(new JLabel("Create and manage purchase orders; receive goods into stock."), "span 2, aligny top, wrap");

        // Filters
        searchField = new JTextField(15);
        searchField.setName("poSearch");
        JButton searchBtn = new JButton("Search");
        searchBtn.addActionListener(e -> loadPurchaseOrders());
        add(new JLabel("Search:"), "aligny top");
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);
        add(searchPanel, "aligny top, wrap");

        // PO list table
        tableModel = new DefaultTableModel(
                new Object[]{"PO #", "Supplier", "Status", "Ordered", "Expected", "Received", "Total"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setFillsViewportHeight(true);
        table.setRowHeight(22);
        table.getColumnModel().getColumn(0).setPreferredWidth(70);
        table.getColumnModel().getColumn(1).setPreferredWidth(180);
        table.getColumnModel().getColumn(2).setPreferredWidth(90);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);
        table.getColumnModel().getColumn(5).setPreferredWidth(100);
        table.getColumnModel().getColumn(6).setPreferredWidth(90);

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < 7; i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(center);
        }

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(800, 220));
        add(scrollPane, "grow, span 2");

        // Buttons
        JButton newBtn = new JButton("New PO");
        JButton receiveBtn = new JButton("Receive Goods");
        JButton cancelBtn = new JButton("Cancel");
        JButton closeBtn = new JButton("Close");
        newBtn.addActionListener(e -> showCreateForm());
        receiveBtn.addActionListener(e -> receiveSelected());
        cancelBtn.addActionListener(e -> cancelSelected());
        closeBtn.addActionListener(e -> {
            createPanel.setVisible(false);
            receivePanel.setVisible(false);
            revalidate();
            repaint();
        });
        add(newBtn, "width 90, split 3");
        add(receiveBtn, "width 90");
        add(cancelBtn, "width 90");
        add(closeBtn, "width 90");

        // Detail form (create PO)
        createPanel = new JPanel(new MigLayout("fill, ins 5 10 5 10", "[grow,fill]", "[]0[][][]"));
        createPanel.setVisible(false);

        supplierCombo = new JComboBox<>();
        poNumberField = new JTextField(12);
        expDateField = new JTextField(12);
        JLabel expLabel = new JLabel("Expected Date:");

        createPanel.add(new JLabel("Supplier:"), "aligny top");
        createPanel.add(supplierCombo, "growx");
        createPanel.add(expLabel, "aligny top, split 2");
        createPanel.add(expDateField, "growx");
        createPanel.add(new JLabel("PO #:"), "aligny top, split 2");
        createPanel.add(poNumberField, "growx");
        createPanel.add(new JLabel("Items:"), "aligny top, wrap");

        // Item row
        skuField = new JTextField(12);
        nameField = new JTextField(18);
        qtyField = new JTextField(6);
        costField = new JTextField(8);
        JLabel skuLabel = new JLabel("SKU:");
        JLabel qtyLabel = new JLabel("Qty:");
        JLabel costLabel = new JLabel("Cost:");

        JPanel itemRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        itemRow.add(skuLabel);
        itemRow.add(skuField);
        itemRow.add(qtyLabel);
        itemRow.add(qtyField);
        itemRow.add(costLabel);
        itemRow.add(costField);
        createPanel.add(itemRow, "aligny top, wrap");

        JButton addItemBtn = new JButton("Add Item");
        addItemBtn.addActionListener(e -> addItem());
        createPanel.add(addItemBtn, "aligny top");

        JButton saveBtn = new JButton("Save PO");
        saveBtn.addActionListener(e -> savePO());
        createPanel.add(saveBtn, "aligny top, split 2");
        createPanel.add(new JLabel("Status:"), "aligny top, split 2");
        statusLabel = new JLabel(" ");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        createPanel.add(statusLabel, "aligny top");

        // Detail form (receive)
        receivePanel = new JPanel(new MigLayout("fill, ins 5 10 5 10", "[grow,fill]", "[]0[][][]"));
        receivePanel.setVisible(false);

        receivePanel.add(new JLabel("Receive goods for PO"), "span 2, aligny top, wrap");
        receivePanel.add(new JLabel("PO #:"), "aligny top, split 2");
        receivePoField = new JTextField(12);
        receivePanel.add(receivePoField, "growx");

        JButton lookupBtn = new JButton("Lookup");
        lookupBtn.addActionListener(e -> {
            String po = receivePoField.getText().trim();
            if (!po.isEmpty()) {
                try {
                    PurchaseOrderDTO poDto = api.getPurchaseOrder(po);
                    if (poDto != null) {
                        showReceiveForm(poDto);
                    } else {
                        JOptionPane.showMessageDialog(this, "PO not found: " + po);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
                }
            }
        });
        receivePanel.add(lookupBtn, "aligny top");

        receivePanel.add(new JLabel("Items:"), "aligny top, wrap");

        // Receive item rows
        JTextField rSkuField = new JTextField(12);
        JTextField rQtyField = new JTextField(6);
        JPanel rRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        rRow.add(new JLabel("SKU:"));
        rRow.add(rSkuField);
        rRow.add(new JLabel("Qty:"));
        rRow.add(rQtyField);
        receivePanel.add(rRow, "aligny top, wrap");

        JButton rAddBtn = new JButton("Add Item");
        rAddBtn.addActionListener(e -> addReceiveItem(rSkuField, rQtyField, receivePanel));
        receivePanel.add(rAddBtn, "aligny top");

        JButton receiveConfirmBtn = new JButton("Confirm Receive");
        receiveConfirmBtn.addActionListener(e -> confirmReceive(receivePanel));
        receivePanel.add(receiveConfirmBtn, "aligny top, split 2");
        receivePanel.add(errorLabel = new JLabel(" "), "aligny top");
        errorLabel.setForeground(Color.RED);

        add(createPanel, "aligny top, wrap");
        add(receivePanel, "aligny top, wrap");

        // Load suppliers and POs
        loadSuppliers();
        loadPurchaseOrders();

        // Default date
        expDateField.setText(LocalDate.now().plusDays(14).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
    }

    private void loadSuppliers() {
        try {
            supplierCombo.removeAllItems();
            supplierCombo.addItem("-- Select Supplier --");
            List<SupplierDTO> suppliers = api.getSuppliers();
            for (SupplierDTO s : suppliers) {
                supplierCombo.addItem(s.getName() + " (" + s.getId() + ")");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading suppliers: " + ex.getMessage());
        }
    }

    private void loadPurchaseOrders() {
        try {
            String q = searchField.getText().trim();
            List<PurchaseOrderDTO> pos;
            if (q.isEmpty()) {
                pos = api.getOpenPurchaseOrders();
            } else {
                pos = api.getPurchaseOrders("OPEN");
            }
            tableModel.setRowCount(0);
            int count = 0;
            for (PurchaseOrderDTO po : pos) {
                tableModel.addRow(new Object[]{
                        po.getPoNumber(), po.getSupplierName(),
                        po.getStatus(), po.getOrderedDate(), po.getExpectedDate(),
                        po.getReceivedDate() != null ? po.getReceivedDate() : "-", po.getTotalAmount() != null ? String.format("$%,.2f", po.getTotalAmount()) : "0.00"
                });
                count++;
            }
            statusLabel.setText("Found " + count + " PO(s)");
        } catch (Exception ex) {
            statusLabel.setText("Error: " + ex.getMessage());
        }
    }

    private void showCreateForm() {
        createPanel.setVisible(true);
        skuField.setText("");
        qtyField.setText("1");
        costField.setText("0.00");
        poNumberField.setText("");
        expDateField.setText(LocalDate.now().plusDays(14).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        skuField.requestFocus();
    }

    private void addItem() {
        String sku = skuField.getText().trim();
        String qtyStr = qtyField.getText().trim();
        String costStr = costField.getText().trim();
        if (sku.isEmpty()) {
            JOptionPane.showMessageDialog(this, "SKU is required");
            return;
        }
        try {
            int qty = Integer.parseInt(qtyStr);
            if (qty <= 0) throw new NumberFormatException();
            double cost = Double.parseDouble(costStr);
            if (cost < 0) throw new NumberFormatException();

            // Check if SKU already exists in current PO
            DefaultTableModel cartModel = (DefaultTableModel) createPanel.getClientProperty("purchaseOrderCart");
            if (cartModel == null) {
                cartModel = new DefaultTableModel(new Object[]{"SKU", "Name", "Qty", "Unit Cost", "Line Total"}, 0);
                createPanel.putClientProperty("purchaseOrderCart", cartModel);
            }

            // Check duplicate
            for (int i = 0; i < cartModel.getRowCount(); i++) {
                if (cartModel.getValueAt(i, 0).equals(sku)) {
                    JOptionPane.showMessageDialog(this, "SKU already added: " + sku);
                    return;
                }
            }

            cartModel.addRow(new Object[]{sku, nameField.getText().trim(), qty, cost, cost * qty});
            skuField.setText("");
            qtyField.setText("1");
            costField.setText("0.00");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid quantity or cost");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void savePO() {
        String poNumber = poNumberField.getText().trim();
        String expDate = expDateField.getText().trim();
        if (poNumber.isEmpty()) {
            statusLabel.setText("PO # is required");
            return;
        }
        if (expDate.isEmpty()) {
            statusLabel.setText("Expected date is required");
            return;
        }

        // Get cart from the create form
        DefaultTableModel cartModel = (DefaultTableModel) createPanel.getClientProperty("purchaseOrderCart");
        if (cartModel == null || cartModel.getRowCount() == 0) {
            statusLabel.setText("Add at least one item");
            return;
        }
        if (supplierCombo.getSelectedIndex() < 0 || supplierCombo.getSelectedItem().toString().startsWith("--")) {
            statusLabel.setText("Select a supplier");
            return;
        }

        try {
            // Parse supplier
            String selected = (String) supplierCombo.getSelectedItem();
            Long supplierId = Long.parseLong(selected.substring(selected.lastIndexOf("(") + 1, selected.length() - 1));

            // Convert cart rows into PO line items
            java.util.List<CreatePurchaseOrderRequest.PurchaseOrderItemRequest> poItems = new java.util.ArrayList<>();
            for (int i = 0; i < cartModel.getRowCount(); i++) {
                CreatePurchaseOrderRequest.PurchaseOrderItemRequest item = new CreatePurchaseOrderRequest.PurchaseOrderItemRequest();
                item.setSku((String) cartModel.getValueAt(i, 0));
                item.setQuantityOrdered(((Number) cartModel.getValueAt(i, 2)).intValue());
                item.setUnitCost(((Number) cartModel.getValueAt(i, 3)).doubleValue());
                poItems.add(item);
            }

            CreatePurchaseOrderRequest req = new CreatePurchaseOrderRequest();
            req.setSupplierId(supplierId);
            req.setExpectedDate(expDate);
            req.setItems(poItems);

            api.createPurchaseOrder(req);
            loadPurchaseOrders();
            clearCreateForm();
            statusLabel.setText("PO created!");
        } catch (Exception ex) {
            statusLabel.setText("Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void clearCreateForm() {
        createPanel.setVisible(false);
        supplierCombo.setSelectedIndex(0);
        poNumberField.setText("");
        expDateField.setText("");
        statusLabel.setText(" ");
    }

    private void showReceiveForm(PurchaseOrderDTO poDto) {
        receivePanel.setVisible(true);
        receivePoField.setText(poDto.getPoNumber());
        errorLabel.setText("");
    }

    private void addReceiveItem(JTextField skuField, JTextField qtyField, JPanel parent) {
        String sku = skuField.getText().trim();
        String qtyStr = qtyField.getText().trim();
        if (sku.isEmpty()) {
            JOptionPane.showMessageDialog(parent, "SKU is required");
            return;
        }
        try {
            int qty = Integer.parseInt(qtyStr);
            if (qty <= 0) throw new NumberFormatException();
            // Add to a list - we store items in parent client property
            javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) parent.getClientProperty("receiveModel");
            if (model == null) {
                model = new javax.swing.table.DefaultTableModel(
                        new Object[]{"SKU", "Name", "Qty Ordered", "Qty Received", "Unit Cost", "Line Total"}, 0);
                parent.putClientProperty("receiveModel", model);
                parent.add(new JScrollPane(new JTable(model)), "grow, span 2, h 200:200:200");
            }
            model.addRow(new Object[]{sku, "Unknown Product", qty, 0, "0.00", "0.00"});
            skuField.setText("");
            qtyField.setText("");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(parent, "Invalid quantity");
        }
    }

    private void confirmReceive(JPanel receivePanel) {
        javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) receivePanel.getClientProperty("receiveModel");
        if (model == null || model.getRowCount() == 0) {
            errorLabel.setText("Add at least one item");
            return;
        }

        // Collect items
        java.util.List<RequestPurchaseOrderRequest.ReceiveItem> items = new java.util.ArrayList<>();
        for (int i = 0; i < model.getRowCount(); i++) {
            RequestPurchaseOrderRequest.ReceiveItem item = new RequestPurchaseOrderRequest.ReceiveItem();
            item.setSku((String) model.getValueAt(i, 0));
            item.setQuantityReceived(((Number) model.getValueAt(i, 3)).intValue());
            items.add(item);
        }

        try {
            RequestPurchaseOrderRequest req = new RequestPurchaseOrderRequest();
            req.setPoId(0L); // We need the PO ID - get from table
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                Long poId = (Long) tableModel.getValueAt(selectedRow, 0);
                req.setPoId(poId);
            } else {
                errorLabel.setText("Select a PO first");
                return;
            }
            req.setItems(items);

            PurchaseOrderDTO result = api.receiveGoods(req);
            loadPurchaseOrders();
            receivePanel.setVisible(false);
            errorLabel.setText("Received successfully! Status: " + result.getStatus());
        } catch (Exception ex) {
            errorLabel.setText("Error: " + ex.getMessage());
        }
    }

    private void receiveSelected() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            statusLabel.setText("Please select a PO to receive goods");
            return;
        }
        Long poId = (Long) tableModel.getValueAt(selectedRow, 0);
        String poNumber = (String) tableModel.getValueAt(selectedRow, 0);
        try {
            PurchaseOrderDTO po = api.getPurchaseOrder(String.valueOf(poId));
            if (po != null) {
                showReceiveForm(po);
                errorLabel.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "PO not found");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void cancelSelected() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            statusLabel.setText("Please select a PO to cancel");
            return;
        }
        Long poId = (Long) tableModel.getValueAt(selectedRow, 0);
        String poNumber = (String) tableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Cancel PO " + poNumber + "?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                PurchaseOrderDTO result = api.cancelPurchaseOrder(poId);
                loadPurchaseOrders();
                statusLabel.setText("PO cancelled: " + result.getStatus());
            } catch (Exception ex) {
                statusLabel.setText("Error: " + ex.getMessage());
            }
        }
    }

    public void refresh() {
        loadPurchaseOrders();
    }
}