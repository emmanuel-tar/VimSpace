package com.vimspace.boh.panel;

import com.vimspace.boh.config.AppConfig;
import com.vimspace.boh.service.ApiException;
import com.vimspace.boh.service.BohApiClient;
import com.vimspace.contract.InventoryMovementDTO;
import com.vimspace.contract.ProductDTO;
import com.vimspace.contract.StockAdjustmentRequest;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Optional;

/**
 * Tab 2: Inventory & Stock — stock on hand, valuations, movement ledger, adjustments.
 */
public class InventoryPanel extends JPanel {

    private final AppConfig config;
    private final BohApiClient apiClient;

    private final DefaultTableModel inventoryTableModel;
    private final JTable inventoryTable;
    private final DefaultTableModel movementTableModel;
    private final JTable movementTable;

    public InventoryPanel(AppConfig config, BohApiClient apiClient) {
        this.config = config;
        this.apiClient = apiClient;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JButton adjustBtn = new JButton("Stock Adjustment / Count");
        JButton refreshBtn = new JButton("Refresh");
        adjustBtn.addActionListener(e -> showAdjustmentDialog());
        refreshBtn.addActionListener(e -> { loadInventory(); loadMovements(); });
        toolbar.add(adjustBtn);
        toolbar.add(refreshBtn);

        add(toolbar, BorderLayout.NORTH);

        String[] invCols = {"SKU", "Name", "Category", "Size", "Color", "Stock on Hand", "Cost Valuation", "Selling Valuation"};
        inventoryTableModel = new DefaultTableModel(invCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        inventoryTable = new JTable(inventoryTableModel);
        inventoryTable.setRowHeight(25);

        String[] movCols = {"ID", "SKU", "Product Name", "Movement Type", "Reference", "Delta", "Reason", "Created By", "Timestamp"};
        movementTableModel = new DefaultTableModel(movCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        movementTable = new JTable(movementTableModel);
        movementTable.setRowHeight(24);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        split.setResizeWeight(0.5);
        JPanel topStock = new JPanel(new BorderLayout());
        topStock.setBorder(BorderFactory.createTitledBorder("Stock on Hand & Valuations"));
        topStock.add(new JScrollPane(inventoryTable), BorderLayout.CENTER);
        JPanel bottomMovements = new JPanel(new BorderLayout());
        bottomMovements.setBorder(BorderFactory.createTitledBorder("Stock Movement Ledger (Audit Trail)"));
        bottomMovements.add(new JScrollPane(movementTable), BorderLayout.CENTER);
        split.setTopComponent(topStock);
        split.setBottomComponent(bottomMovements);

        add(split, BorderLayout.CENTER);

        loadInventory();
        loadMovements();
    }

    private void loadInventory() {
        new SwingWorker<List<ProductDTO>, Void>() {
            private String error;
            @Override protected List<ProductDTO> doInBackground() {
                try { return apiClient.getProducts(false); } catch (ApiException e) { error = e.getMessage(); return null; }
            }
            @Override protected void done() {
                inventoryTableModel.setRowCount(0);
                if (error != null) { JOptionPane.showMessageDialog(InventoryPanel.this, "Failed to load inventory: " + error); return; }
                try {
                    List<ProductDTO> list = get();
                    if (list == null) return;
                    for (ProductDTO p : list) {
                        Double stock = p.getStockOnHand() == null ? 0 : p.getStockOnHand();
                        Double costVal = p.getCostPrice() == null ? 0 : p.getCostPrice() * stock;
                        Double sellVal = p.getSellingPrice() == null ? 0 : p.getSellingPrice() * stock;
                        inventoryTableModel.addRow(new Object[]{
                                p.getSku(), p.getName(), p.getCategory(), p.getSize(), p.getColor(),
                                stock, String.format("$%.2f", costVal), String.format("$%.2f", sellVal)
                        });
                    }
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    private void loadMovements() {
        new SwingWorker<List<InventoryMovementDTO>, Void>() {
            private String error;
            @Override protected List<InventoryMovementDTO> doInBackground() {
                try { return apiClient.getStockMovements(); } catch (ApiException e) { error = e.getMessage(); return null; }
            }
            @Override protected void done() {
                movementTableModel.setRowCount(0);
                if (error != null) { JOptionPane.showMessageDialog(InventoryPanel.this, "Failed to load movements: " + error); return; }
                try {
                    List<InventoryMovementDTO> list = get();
                    if (list == null) return;
                    for (InventoryMovementDTO m : list) {
                        movementTableModel.addRow(new Object[]{
                                m.getId(), m.getProductSku(), m.getProductName(), m.getType(),
                                m.getReferenceId(), m.getQuantityDelta(), m.getReason(),
                                m.getCreatedBy(), m.getCreatedAt()
                        });
                    }
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    private void showAdjustmentDialog() {
        int row = inventoryTable.getSelectedRow();
        String defaultSku = row >= 0 ? inventoryTableModel.getValueAt(row, 0).toString() : "";

        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "Stock Adjustment / Count Correction", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(400, 320);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new GridBagLayout());

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 8, 6, 8);
        g.anchor = GridBagConstraints.WEST;

        JTextField skuField = new JTextField(defaultSku, 18);
        JTextField deltaField = new JTextField("0", 18);
        String[] reasons = {"Count Correction", "Received Delivery", "Damaged / Spoiled", "Expired", "Shrinkage / Theft", "Return to Vendor"};
        JComboBox<String> reasonCombo = new JComboBox<>(reasons);
        reasonCombo.setPreferredSize(new Dimension(200, 26));
        JTextField userField = new JTextField(config.getLoggedInUsername() != null ? config.getLoggedInUsername() : "", 18);

        g.gridx = 0; g.gridy = 0; dlg.add(new JLabel("Product SKU:"), g);
        g.gridx = 1; dlg.add(skuField, g);
        g.gridx = 0; g.gridy = 1; dlg.add(new JLabel("Quantity Delta (+/-):"), g);
        g.gridx = 1; dlg.add(deltaField, g);
        g.gridx = 0; g.gridy = 2; dlg.add(new JLabel("Reason:"), g);
        g.gridx = 1; dlg.add(reasonCombo, g);
        g.gridx = 0; g.gridy = 3; dlg.add(new JLabel("Adjusted By:"), g);
        g.gridx = 1; dlg.add(userField, g);

        JButton submitBtn = new JButton("Apply Stock Adjustment");
        submitBtn.addActionListener(e -> {
            try {
                String sku = skuField.getText().trim();
                double delta = Double.parseDouble(deltaField.getText().trim());
                if (sku.isEmpty() || delta == 0) {
                    JOptionPane.showMessageDialog(dlg, "Please specify a valid SKU and non-zero delta.");
                    return;
                }
                StockAdjustmentRequest req = new StockAdjustmentRequest(sku, delta,
                        (String) reasonCombo.getSelectedItem(), userField.getText().trim());

                new SwingWorker<ProductDTO, Void>() {
                    private String error;
                    @Override protected ProductDTO doInBackground() {
                        try { return apiClient.adjustStock(req); } catch (ApiException ex) { error = ex.getMessage(); return null; }
                    }
                    @Override protected void done() {
                        if (error != null) {
                            JOptionPane.showMessageDialog(dlg, "Adjustment failed: " + error);
                        } else {
                            JOptionPane.showMessageDialog(dlg, "Stock adjusted successfully!");
                            dlg.dispose();
                            loadInventory();
                            loadMovements();
                        }
                    }
                }.execute();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Invalid input: " + ex.getMessage());
            }
        });

        g.gridx = 0; g.gridy = 4; g.gridwidth = 2; g.anchor = GridBagConstraints.CENTER;
        dlg.add(submitBtn, g);
        dlg.setVisible(true);
    }
}