package com.vimspace.boh.panel;

import com.vimspace.boh.config.AppConfig;
import com.vimspace.boh.service.ApiException;
import com.vimspace.boh.service.BohApiClient;
import com.vimspace.contract.CreateProductRequest;
import com.vimspace.contract.ProductDTO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Tab 1: Product Catalog & Variants — list, add, edit, delete products.
 */
public class ProductPanel extends JPanel {

    private final AppConfig config;
    private final BohApiClient apiClient;

    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JComboBox<String> categoryFilter;

    public ProductPanel(AppConfig config, BohApiClient apiClient) {
        this.config = config;
        this.apiClient = apiClient;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        String[] cols = {"ID", "SKU", "Name", "Category", "Size", "Color", "Cost ($)", "Price ($)", "Stock", "Barcodes", "Active"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(25);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JButton addBtn = new JButton("+ Add Product");
        JButton editBtn = new JButton("Edit Product");
        JButton delBtn = new JButton("Delete Product");
        JButton refreshBtn = new JButton("Refresh");
        categoryFilter = new JComboBox<>();
        categoryFilter.setPreferredSize(new Dimension(200, 26));

        addBtn.addActionListener(e -> showProductDialog(null));
        editBtn.addActionListener(e -> editSelected());
        delBtn.addActionListener(e -> deleteSelected());
        refreshBtn.addActionListener(e -> loadProducts());
        categoryFilter.addActionListener(e -> filterByCategory());

        toolbar.add(addBtn);
        toolbar.add(editBtn);
        toolbar.add(delBtn);
        toolbar.add(refreshBtn);
        toolbar.add(new JLabel("Category:"));
        toolbar.add(categoryFilter);

        // Barcode scanner lookup
        toolbar.add(new JLabel("  |  Scan Barcode:"));
        JTextField barcodeScanField = new JTextField(15);
        barcodeScanField.setToolTipText("Type or scan a barcode to find the product");
        barcodeScanField.addActionListener(e -> searchByBarcode(barcodeScanField.getText().trim()));
        toolbar.add(barcodeScanField);

        // CSV import/export
        JButton exportCsvBtn = new JButton("Export CSV");
        exportCsvBtn.addActionListener(e -> exportProductsToCsv());
        JButton importCsvBtn = new JButton("Import CSV");
        importCsvBtn.addActionListener(e -> importProductsFromCsv());
        toolbar.add(exportCsvBtn);
        toolbar.add(importCsvBtn);

        add(toolbar, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        loadProducts();
        loadCategories();
    }

    private void loadProducts() {
        new SwingWorker<List<ProductDTO>, Void>() {
            private String error;

            @Override protected List<ProductDTO> doInBackground() {
                try {
                    return apiClient.getProducts(false);
                } catch (ApiException e) {
                    error = e.getMessage();
                    return null;
                }
            }

            @Override protected void done() {
                tableModel.setRowCount(0);
                if (error != null) {
                    JOptionPane.showMessageDialog(ProductPanel.this, "Failed to load products: " + error);
                    return;
                }
                try {
                    List<ProductDTO> list = get();
                    if (list == null) return;
                    for (ProductDTO p : list) {
                        tableModel.addRow(new Object[]{
                                p.getId(), p.getSku(), p.getName(), p.getCategory(),
                                p.getSize(), p.getColor(),
                                formatMoney(p.getCostPrice()), formatMoney(p.getSellingPrice()),
                                p.getStockOnHand(),
                                p.getBarcodes() == null ? "" : String.join(", ", p.getBarcodes()),
                                Boolean.TRUE.equals(p.getActive()) ? "Active" : "Inactive"
                        });
                    }
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    private void loadCategories() {
        new SwingWorker<List<String>, Void>() {
            @Override protected List<String> doInBackground() {
                try {
                    return apiClient.getCategories();
                } catch (ApiException e) {
                    return null;
                }
            }
            @Override protected void done() {
                try {
                    List<String> cats = get();
                    if (cats == null) return;
                    categoryFilter.removeAllItems();
                    categoryFilter.addItem("All Categories");
                    for (String c : cats) categoryFilter.addItem(c);
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    private void filterByCategory() {
        String selected = (String) categoryFilter.getSelectedItem();
        if (selected == null || "All Categories".equals(selected)) {
            loadProducts();
            return;
        }
        new SwingWorker<List<ProductDTO>, Void>() {
            @Override protected List<ProductDTO> doInBackground() {
                try {
                    return apiClient.getProducts(false);
                } catch (ApiException e) {
                    return null;
                }
            }
            @Override protected void done() {
                try {
                    List<ProductDTO> list = get();
                    if (list == null) return;
                    tableModel.setRowCount(0);
                    for (ProductDTO p : list) {
                        if (selected.equals(p.getCategory())) {
                            tableModel.addRow(new Object[]{
                                    p.getId(), p.getSku(), p.getName(), p.getCategory(),
                                    p.getSize(), p.getColor(),
                                    formatMoney(p.getCostPrice()), formatMoney(p.getSellingPrice()),
                                    p.getStockOnHand(),
                                    p.getBarcodes() == null ? "" : String.join(", ", p.getBarcodes()),
                                    Boolean.TRUE.equals(p.getActive()) ? "Active" : "Inactive"
                            });
                        }
                    }
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    private void editSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a product to edit.");
            return;
        }
        Long id = Long.valueOf(tableModel.getValueAt(row, 0).toString());
        String sku = tableModel.getValueAt(row, 1).toString();
        String name = tableModel.getValueAt(row, 2).toString();
        String category = tableModel.getValueAt(row, 3).toString();
        String size = tableModel.getValueAt(row, 4).toString();
        String color = tableModel.getValueAt(row, 5).toString();
        Double cost = parseMoney(tableModel.getValueAt(row, 6).toString());
        Double price = parseMoney(tableModel.getValueAt(row, 7).toString());
        Double stock = parseNumber(tableModel.getValueAt(row, 8).toString());
        String barcodesStr = tableModel.getValueAt(row, 9).toString();

        showProductDialog(new ProductDTO(id, sku, name, category, "pcs", size, color,
                cost, price, stock,
                "Active".equals(tableModel.getValueAt(row, 10).toString()),
                barcodesStr.isEmpty() ? null : Arrays.asList(barcodesStr.split("\\s*,\\s*"))));
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a product to delete.");
            return;
        }
        Long id = Long.valueOf(tableModel.getValueAt(row, 0).toString());
        String sku = tableModel.getValueAt(row, 1).toString();
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete product '" + sku + "'? This cannot be undone.", "Confirm Delete",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        new SwingWorker<Void, Void>() {
            private String error;

            @Override protected Void doInBackground() {
                try {
                    apiClient.deleteProduct(id);
                } catch (ApiException e) {
                    error = e.getMessage();
                }
                return null;
            }
            @Override protected void done() {
                if (error != null) {
                    JOptionPane.showMessageDialog(ProductPanel.this, "Delete failed: " + error);
                } else {
                    loadProducts();
                }
            }
        }.execute();
    }

    private void showProductDialog(ProductDTO existing) {
        boolean isEdit = existing != null;
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this),
                isEdit ? "Edit Product: " + existing.getSku() : "Add New Product",
                Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(500, 620);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new GridBagLayout());

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 8, 6, 8);
        g.anchor = GridBagConstraints.WEST;

        JTextField skuField = new JTextField(isEdit ? existing.getSku() : "SKU-" + System.currentTimeMillis() % 100000, 18);
        if (isEdit) skuField.setEditable(false);
        JTextField nameField = new JTextField(isEdit ? existing.getName() : "", 18);

        JComboBox<String> catBox = new JComboBox<>(new String[]{"Groceries", "Beverages", "Dairy", "Bakery", "Produce", "Apparel / Clothing", "Electronics", "Household"});
        catBox.setEditable(true);
        if (isEdit && existing.getCategory() != null) catBox.setSelectedItem(existing.getCategory());

        JComboBox<String> sizeBox = new JComboBox<>(new String[]{"Standard", "Small (S)", "Medium (M)", "Large (L)", "XL", "XXL", "250ml", "500ml", "1L", "2L", "500g", "1kg"});
        sizeBox.setEditable(true);
        if (isEdit && existing.getSize() != null) sizeBox.setSelectedItem(existing.getSize());

        JComboBox<String> colorBox = new JComboBox<>(new String[]{"N/A", "Black", "White", "Blue", "Red", "Green", "Yellow", "Grey", "Navy"});
        colorBox.setEditable(true);
        if (isEdit && existing.getColor() != null) colorBox.setSelectedItem(existing.getColor());

        JTextField costField = new JTextField(isEdit ? String.format("%.2f", existing.getCostPrice()) : "0.00", 18);
        JTextField priceField = new JTextField(isEdit ? String.format("%.2f", existing.getSellingPrice()) : "0.00", 18);
        JTextField stockField = new JTextField(isEdit ? String.valueOf(existing.getStockOnHand().intValue()) : "50", 18);

        JTextField barcodeField = new JTextField(isEdit && existing.getBarcodes() != null ? String.join(", ", existing.getBarcodes()) : "", 12);

        JPanel barcodePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        barcodePanel.add(barcodeField);
        barcodePanel.add(Box.createHorizontalStrut(5));

        g.gridx = 0; g.gridy = 0; dlg.add(new JLabel("SKU Code:"), g);
        g.gridx = 1; dlg.add(skuField, g);
        g.gridx = 0; g.gridy = 1; dlg.add(new JLabel("Product Name:"), g);
        g.gridx = 1; dlg.add(nameField, g);
        g.gridx = 0; g.gridy = 2; dlg.add(new JLabel("Category:"), g);
        g.gridx = 1; dlg.add(catBox, g);
        g.gridx = 0; g.gridy = 3; dlg.add(new JLabel("Size / Weight:"), g);
        g.gridx = 1; dlg.add(sizeBox, g);
        g.gridx = 0; g.gridy = 4; dlg.add(new JLabel("Color:"), g);
        g.gridx = 1; dlg.add(colorBox, g);
        g.gridx = 0; g.gridy = 5; dlg.add(new JLabel("Cost Price ($):"), g);
        g.gridx = 1; dlg.add(costField, g);
        g.gridx = 0; g.gridy = 6; dlg.add(new JLabel("Selling Price ($):"), g);
        g.gridx = 1; dlg.add(priceField, g);
        g.gridx = 0; g.gridy = 7; dlg.add(new JLabel("Initial Stock:"), g);
        g.gridx = 1; dlg.add(stockField, g);
        g.gridx = 0; g.gridy = 8; dlg.add(new JLabel("Barcodes:"), g);
        g.gridx = 1; dlg.add(barcodePanel, g);

        JButton genBarcodeBtn = new JButton("Generate Barcode");
        genBarcodeBtn.addActionListener(e -> {
            new SwingWorker<Optional<com.vimspace.contract.BarcodeGenerateResponse>, Void>() {
                @Override protected Optional<com.vimspace.contract.BarcodeGenerateResponse> doInBackground() {
                    try {
                        return Optional.of(apiClient.generateBarcode("EAN13", "20"));
                    } catch (ApiException ex) {
                        return Optional.empty();
                    }
                }
                @Override protected void done() {
                    try {
                        Optional<com.vimspace.contract.BarcodeGenerateResponse> gen = get();
                        if (gen.isPresent()) {
                            String cur = barcodeField.getText().trim();
                            barcodeField.setText(cur.isEmpty() ? gen.get().getBarcode() : cur + ", " + gen.get().getBarcode());
                        } else {
                            JOptionPane.showMessageDialog(dlg, "Failed to generate barcode.");
                        }
                    } catch (Exception ignored) {}
                }
            }.execute();
        });
        barcodePanel.add(genBarcodeBtn);

        JButton saveBtn = new JButton(isEdit ? "Update Product" : "Save Product");
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        saveBtn.addActionListener(e -> {
            try {
                String sku = skuField.getText().trim();
                String name = nameField.getText().trim();
                if (sku.isEmpty() || name.isEmpty()) {
                    JOptionPane.showMessageDialog(dlg, "SKU and Name are required.");
                    return;
                }
                Double cost = Double.parseDouble(costField.getText().trim());
                Double price = Double.parseDouble(priceField.getText().trim());
                Double stock = Double.parseDouble(stockField.getText().trim());
                List<String> codes = Arrays.stream(barcodeField.getText().split(","))
                        .map(String::trim).filter(s -> !s.isEmpty()).toList();

                CreateProductRequest req = new CreateProductRequest(
                        sku, name, String.valueOf(catBox.getSelectedItem()), "pcs",
                        String.valueOf(sizeBox.getSelectedItem()), String.valueOf(colorBox.getSelectedItem()),
                        cost, price, stock, codes
                );

                new SwingWorker<ProductDTO, Void>() {
                    private String error;

                    @Override protected ProductDTO doInBackground() {
                        try {
                            if (isEdit) {
                                return apiClient.updateProduct(existing.getId(), req);
                            } else {
                                return apiClient.saveProduct(req);
                            }
                        } catch (ApiException ex) {
                            error = ex.getMessage();
                            return null;
                        }
                    }
                    @Override protected void done() {
                        if (error != null) {
                            JOptionPane.showMessageDialog(dlg, "Save failed: " + error);
                        } else {
                            JOptionPane.showMessageDialog(dlg, "Product saved successfully!");
                            dlg.dispose();
                            loadProducts();
                        }
                    }
                }.execute();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Invalid input: " + ex.getMessage());
            }
        });

        g.gridx = 0; g.gridy = 9; g.gridwidth = 2; g.anchor = GridBagConstraints.CENTER;
        dlg.add(saveBtn, g);
        dlg.setVisible(true);
    }

    private void searchByBarcode(String barcode) {
        if (barcode.isEmpty()) {
            loadProducts();
            return;
        }
        new SwingWorker<List<ProductDTO>, Void>() {
            private String error;
            @Override protected List<ProductDTO> doInBackground() {
                try { return apiClient.getProducts(false); } catch (ApiException e) { error = e.getMessage(); return null; }
            }
            @Override protected void done() {
                tableModel.setRowCount(0);
                if (error != null) { JOptionPane.showMessageDialog(ProductPanel.this, "Search failed: " + error); return; }
                try {
                    List<ProductDTO> list = get();
                    if (list == null) return;
                    boolean found = false;
                    for (ProductDTO p : list) {
                        if (p.getBarcodes() != null && p.getBarcodes().contains(barcode)) {
                            found = true;
                            tableModel.addRow(new Object[]{
                                    p.getId(), p.getSku(), p.getName(), p.getCategory(),
                                    p.getSize(), p.getColor(),
                                    formatMoney(p.getCostPrice()), formatMoney(p.getSellingPrice()),
                                    p.getStockOnHand(),
                                    String.join(", ", p.getBarcodes()),
                                    Boolean.TRUE.equals(p.getActive()) ? "Active" : "Inactive"
                            });
                        }
                    }
                    if (!found) {
                        JOptionPane.showMessageDialog(ProductPanel.this, "No product found with barcode: " + barcode);
                    }
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    private static String formatMoney(Double v) {
        return v == null ? "$0.00" : String.format("$%.2f", v);
    }

    private void exportProductsToCsv() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No products to export.");
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("VimSpace_Products_Export_" + System.currentTimeMillis() + ".csv"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try (PrintWriter writer = new PrintWriter(file)) {
                writer.println("SKU,Name,Category,Unit,Size,Color,Cost Price,Selling Price,Stock on Hand,Active,Barcodes");
                for (int r = 0; r < tableModel.getRowCount(); r++) {
                    StringBuilder row = new StringBuilder();
                    for (int c = 0; c < tableModel.getColumnCount(); c++) {
                        row.append(String.valueOf(tableModel.getValueAt(r, c)).replace(",", " "));
                        if (c < tableModel.getColumnCount() - 1) row.append(",");
                    }
                    writer.println(row.toString());
                }
                JOptionPane.showMessageDialog(this, "Products exported to: " + file.getAbsolutePath());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error exporting CSV: " + ex.getMessage());
            }
        }
    }

    private static Double parseMoney(String s) {
        if (s == null) return 0.0;
        s = s.replace("$", "").replace(",", "").trim();
        return s.isEmpty() ? 0.0 : Double.parseDouble(s);
    }

    private void importProductsFromCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Import Products from CSV");
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            new SwingWorker<Void, Void>() {
                private String error;
                private int imported = 0;
                @Override protected Void doInBackground() {
                    try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(file))) {
                        String line;
                        boolean header = true;
                        while ((line = br.readLine()) != null) {
                            if (header) { header = false; continue; }
                            String[] parts = line.split(",", -1);
                            if (parts.length < 5) continue;
                            try {
                                CreateProductRequest req = new CreateProductRequest(
                                        parts[0].trim(), parts[1].trim(), parts[2].trim(), "pcs",
                                        parts.length > 4 ? parts[4].trim() : "Standard",
                                        parts.length > 5 ? parts[5].trim() : "N/A",
                                        parts.length > 6 ? Double.parseDouble(parts[6].trim()) : 0.0,
                                        parts.length > 7 ? Double.parseDouble(parts[7].trim()) : 0.0,
                                        parts.length > 8 ? Double.parseDouble(parts[8].trim()) : 0.0,
                                        parts.length > 9 && !parts[9].trim().isEmpty()
                                                ? Arrays.asList(parts[9].trim().split("\\s*;\\s*"))
                                                : null
                                );
                                apiClient.saveProduct(req);
                                imported++;
                            } catch (Exception ex) {
                                // skip bad row
                            }
                        }
                    } catch (Exception ex) {
                        error = ex.getMessage();
                    }
                    return null;
                }
                @Override protected void done() {
                    if (error != null) {
                        JOptionPane.showMessageDialog(ProductPanel.this, "Import failed: " + error);
                    } else {
                        JOptionPane.showMessageDialog(ProductPanel.this, "Imported " + imported + " products.");
                        loadProducts();
                        loadCategories();
                    }
                }
            }.execute();
        }
    }

    private static Double parseNumber(String s) {
        if (s == null || s.trim().isEmpty()) return 0.0;
        return Double.parseDouble(s.trim());
    }
}