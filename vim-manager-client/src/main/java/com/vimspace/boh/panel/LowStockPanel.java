package com.vimspace.boh.panel;

import com.vimspace.boh.config.AppConfig;
import com.vimspace.boh.service.ApiException;
import com.vimspace.boh.service.BohApiClient;
import com.vimspace.contract.ProductDTO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Tab: Low-Stock Report — products at or below a reorder threshold.
 */
public class LowStockPanel extends JPanel {

    private final AppConfig config;
    private final BohApiClient apiClient;

    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JSpinner thresholdSpinner;
    private final JLabel countLabel;

    public LowStockPanel(AppConfig config, BohApiClient apiClient) {
        this.config = config;
        this.apiClient = apiClient;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        toolbar.add(new JLabel("Reorder Threshold:"));
        thresholdSpinner = new JSpinner(new SpinnerNumberModel(10, 0, 1000, 1));
        thresholdSpinner.setPreferredSize(new Dimension(80, 26));
        thresholdSpinner.addChangeListener(e -> loadLowStock());
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> loadLowStock());
        JButton exportBtn = new JButton("Export CSV");
        exportBtn.addActionListener(e -> exportToCsv());
        toolbar.add(thresholdSpinner);
        toolbar.add(refreshBtn);
        toolbar.add(exportBtn);

        countLabel = new JLabel("Low-stock items: 0");
        countLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        toolbar.add(Box.createHorizontalGlue());
        toolbar.add(countLabel);

        add(toolbar, BorderLayout.NORTH);

        String[] cols = {"SKU", "Name", "Category", "Size", "Color", "Stock on Hand", "Threshold", "Cost ($)", "Price ($)"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(25);

        add(new JScrollPane(table), BorderLayout.CENTER);

        loadLowStock();
    }

    private void loadLowStock() {
        int threshold = (Integer) thresholdSpinner.getValue();
        new SwingWorker<List<ProductDTO>, Void>() {
            private String error;
            @Override protected List<ProductDTO> doInBackground() {
                try { return apiClient.getProducts(false); } catch (ApiException e) { error = e.getMessage(); return null; }
            }
            @Override protected void done() {
                tableModel.setRowCount(0);
                if (error != null) { JOptionPane.showMessageDialog(LowStockPanel.this, "Failed to load products: " + error); return; }
                try {
                    List<ProductDTO> list = get();
                    if (list == null) return;
                    int lowCount = 0;
                    for (ProductDTO p : list) {
                        Double stock = p.getStockOnHand() == null ? 0 : p.getStockOnHand();
                        if (stock <= threshold) {
                            lowCount++;
                            tableModel.addRow(new Object[]{
                                    p.getSku(), p.getName(), p.getCategory(), p.getSize(), p.getColor(),
                                    stock, threshold,
                                    String.format("$%.2f", p.getCostPrice()),
                                    String.format("$%.2f", p.getSellingPrice())
                            });
                        }
                    }
                    countLabel.setText("Low-stock items: " + lowCount);
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    private void exportToCsv() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No low-stock records to export.");
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File("VimSpace_LowStock_Report_" + System.currentTimeMillis() + ".csv"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            java.io.File file = chooser.getSelectedFile();
            try (java.io.PrintWriter writer = new java.io.PrintWriter(file)) {
                for (int i = 0; i < tableModel.getColumnCount(); i++) {
                    writer.print(tableModel.getColumnName(i));
                    if (i < tableModel.getColumnCount() - 1) writer.print(",");
                }
                writer.println();
                for (int r = 0; r < tableModel.getRowCount(); r++) {
                    for (int c = 0; c < tableModel.getColumnCount(); c++) {
                        String cell = String.valueOf(tableModel.getValueAt(r, c)).replace(",", " ");
                        writer.print(cell);
                        if (c < tableModel.getColumnCount() - 1) writer.print(",");
                    }
                    writer.println();
                }
                JOptionPane.showMessageDialog(this, "Low-stock report exported to: " + file.getAbsolutePath());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error exporting CSV: " + ex.getMessage());
            }
        }
    }
}

