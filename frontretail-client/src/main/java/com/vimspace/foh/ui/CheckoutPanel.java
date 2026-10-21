package com.vimspace.foh.ui;

import com.vimspace.contract.PaymentDTO;
import com.vimspace.contract.ProductDTO;
import com.vimspace.contract.SaleLineDTO;
import com.vimspace.contract.SaleRequest;
import com.vimspace.contract.SaleResponse;
import com.vimspace.foh.service.ApiClient;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CheckoutPanel extends JPanel {
    private final ApiClient apiClient;
    private final String username;

    private final DefaultTableModel tableModel;
    private final JTable itemTable;
    private final JTextField barcodeField = new JTextField(16);
    private final JLabel totalLabel = new JLabel("Total: $0.00", SwingConstants.RIGHT);
    private final JButton completeSaleBtn = new JButton("Complete Sale");

    private final List<CartItem> cartItems = new ArrayList<>();
    private double currentTotal = 0.0;

    public static class CartItem {
        final ProductDTO product;
        int quantity;
        double lineTotal;

        CartItem(ProductDTO product, int quantity) {
            this.product = product;
            this.quantity = quantity;
            this.lineTotal = product.getSellingPrice() * quantity;
        }
    }

    public CheckoutPanel(ApiClient apiClient, String username) {
        this.apiClient = apiClient;
        this.username = username;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Top bar: Scan / Search input
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        top.add(new JLabel("Barcode / SKU:"));
        barcodeField.setPreferredSize(new Dimension(200, 32));
        top.add(barcodeField);

        JButton addBtn = new JButton("Add Item");
        addBtn.setPreferredSize(new Dimension(100, 32));
        addBtn.addActionListener(e -> addItemByBarcode());
        barcodeField.addActionListener(e -> addItemByBarcode());
        top.add(addBtn);

        JButton clearBtn = new JButton("Clear Basket");
        clearBtn.setPreferredSize(new Dimension(110, 32));
        clearBtn.addActionListener(e -> clearCart());
        top.add(clearBtn);

        add(top, BorderLayout.NORTH);

        // Center: Item Table
        String[] columns = {"SKU", "Description", "Unit Price", "Qty", "Total"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        itemTable = new JTable(tableModel);
        itemTable.setRowHeight(26);
        itemTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        JScrollPane scroll = new JScrollPane(itemTable);
        add(scroll, BorderLayout.CENTER);

        // Bottom Bar: Totals, Payment Method & Sale submission
        JPanel bottom = new JPanel(new BorderLayout(10, 10));
        bottom.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        totalLabel.setForeground(new Color(20, 120, 60));
        bottom.add(totalLabel, BorderLayout.NORTH);

        JPanel payPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        String[] methods = {"Cash", "Card / POS", "Bank Transfer"};
        JComboBox<String> methodCombo = new JComboBox<>(methods);
        methodCombo.setPreferredSize(new Dimension(150, 34));

        completeSaleBtn.setPreferredSize(new Dimension(160, 34));
        completeSaleBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        completeSaleBtn.addActionListener(e -> submitSale((String) methodCombo.getSelectedItem()));

        payPanel.add(new JLabel("Payment Tender:"));
        payPanel.add(methodCombo);
        payPanel.add(completeSaleBtn);

        bottom.add(payPanel, BorderLayout.SOUTH);
        add(bottom, BorderLayout.SOUTH);
    }

    private void addItemByBarcode() {
        String input = barcodeField.getText().trim();
        if (input.isEmpty()) return;

        barcodeField.setEnabled(false);
        new SwingWorker<Optional<ProductDTO>, Void>() {
            @Override
            protected Optional<ProductDTO> doInBackground() {
                // Try barcode first
                Optional<ProductDTO> prod = apiClient.getProductByBarcode(input);
                if (prod.isEmpty()) {
                    // Try SKU search
                    List<ProductDTO> searchList = apiClient.searchProducts(input);
                    if (!searchList.isEmpty()) {
                        return Optional.of(searchList.get(0));
                    }
                }
                return prod;
            }

            @Override
            protected void done() {
                barcodeField.setEnabled(true);
                barcodeField.requestFocus();
                try {
                    Optional<ProductDTO> result = get();
                    if (result.isPresent()) {
                        addProductToCart(result.get());
                        barcodeField.setText("");
                    } else {
                        JOptionPane.showMessageDialog(CheckoutPanel.this,
                                "No active product found for: " + input,
                                "Product Not Found", JOptionPane.WARNING_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(CheckoutPanel.this,
                            "Error querying product: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void addProductToCart(ProductDTO product) {
        for (CartItem item : cartItems) {
            if (item.product.getSku().equals(product.getSku())) {
                item.quantity += 1;
                item.lineTotal = item.quantity * item.product.getSellingPrice();
                rebuildTable();
                return;
            }
        }

        cartItems.add(new CartItem(product, 1));
        rebuildTable();
    }

    private void rebuildTable() {
        tableModel.setRowCount(0);
        currentTotal = 0.0;
        for (CartItem item : cartItems) {
            currentTotal += item.lineTotal;
            tableModel.addRow(new Object[]{
                    item.product.getSku(),
                    item.product.getName(),
                    String.format("$%.2f", item.product.getSellingPrice()),
                    item.quantity,
                    String.format("$%.2f", item.lineTotal)
            });
        }
        totalLabel.setText(String.format("Total: $%.2f", currentTotal));
    }

    private void clearCart() {
        cartItems.clear();
        rebuildTable();
    }

    private void submitSale(String paymentMethod) {
        if (cartItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "The basket is empty.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                String.format("Process sale for $%.2f via %s?", currentTotal, paymentMethod),
                "Confirm Payment", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        completeSaleBtn.setEnabled(false);

        List<SaleLineDTO> lines = new ArrayList<>();
        for (CartItem item : cartItems) {
            lines.add(new SaleLineDTO(
                    item.product.getSku(),
                    item.quantity,
                    item.product.getSellingPrice(),
                    item.lineTotal
            ));
        }

        List<PaymentDTO> payments = List.of(new PaymentDTO(paymentMethod, currentTotal));
        String idempotencyKey = UUID.randomUUID().toString();

        SaleRequest saleRequest = new SaleRequest(username, "TERM-01", idempotencyKey, lines, payments);

        new SwingWorker<Optional<SaleResponse>, Void>() {
            @Override
            protected Optional<SaleResponse> doInBackground() {
                return apiClient.completeSale(saleRequest);
            }

            @Override
            protected void done() {
                completeSaleBtn.setEnabled(true);
                try {
                    Optional<SaleResponse> response = get();
                    if (response.isPresent()) {
                        SaleResponse res = response.get();
                        JOptionPane.showMessageDialog(CheckoutPanel.this,
                                "Sale Completed Successfully!\n\nReceipt: " + res.getReceiptNumber() +
                                        "\nTotal Paid: $" + String.format("%.2f", res.getTotal()) +
                                        "\nMethod: " + paymentMethod,
                                "Transaction Approved", JOptionPane.INFORMATION_MESSAGE);
                        clearCart();
                    } else {
                        JOptionPane.showMessageDialog(CheckoutPanel.this,
                                "Failed to commit sale to backend. Check connection.",
                                "Sale Failed", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(CheckoutPanel.this,
                            "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }
}
