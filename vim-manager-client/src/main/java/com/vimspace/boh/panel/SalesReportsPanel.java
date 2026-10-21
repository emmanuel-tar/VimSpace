package com.vimspace.boh.panel;

import com.vimspace.boh.config.AppConfig;
import com.vimspace.boh.service.ApiException;
import com.vimspace.boh.service.BohApiClient;
import com.vimspace.contract.DailySalesSummaryDTO;
import com.vimspace.contract.PaymentDTO;
import com.vimspace.contract.SaleDetailDTO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

/**
 * Tab 3: Sales & Reports — sales listing, daily summary, CSV export.
 */
public class SalesReportsPanel extends JPanel {

    private final AppConfig config;
    private final BohApiClient apiClient;

    private final DefaultTableModel salesTableModel;
    private final JTable salesTable;
    private JLabel totalCard, transCard, cashCard, cardCard;

    public SalesReportsPanel(AppConfig config, BohApiClient apiClient) {
        this.config = config;
        this.apiClient = apiClient;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        String[] salesCols = {"Receipt #", "Terminal", "Cashier", "Timestamp", "Total", "Tax", "Status", "Payments"};
        salesTableModel = new DefaultTableModel(salesCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        salesTable = new JTable(salesTableModel);
        salesTable.setRowHeight(25);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JButton refreshBtn = new JButton("Refresh Sales");
        JButton exportBtn = new JButton("Export CSV");
        refreshBtn.addActionListener(e -> loadSales());
        exportBtn.addActionListener(e -> exportSalesToCsv());
        toolbar.add(refreshBtn);
        toolbar.add(exportBtn);

        add(toolbar, BorderLayout.NORTH);
        add(new JScrollPane(salesTable), BorderLayout.CENTER);

        JPanel summaryCard = createSummaryCard();
        add(summaryCard, BorderLayout.SOUTH);

        loadSales();
        loadDailySummary();
    }

    private JPanel createSummaryCard() {
        JPanel card = new JPanel(new GridLayout(1, 4, 10, 0));
        card.setBorder(BorderFactory.createTitledBorder("Today's Sales Summary"));
        totalCard = summaryLabel("Today's Gross Sales", "$0.00");
        transCard = summaryLabel("Total Transactions", "0");
        cashCard = summaryLabel("Cash Tenders", "$0.00");
        cardCard = summaryLabel("Card / POS Tenders", "$0.00");
        card.add(totalCard);
        card.add(transCard);
        card.add(cashCard);
        card.add(cardCard);
        return card;
    }

    private JLabel summaryLabel(String title, String value) {
        JLabel label = new JLabel("<html><div style='text-align:center;'><b>" + title + "</b><br><font size='+1'>" + value + "</font></div></html>");
        label.setBorder(BorderFactory.createLineBorder(label.getBackground().darker(), 1));
        return label;
    }

    private void loadSales() {
        new SwingWorker<List<SaleDetailDTO>, Void>() {
            private String error;
            @Override protected List<SaleDetailDTO> doInBackground() {
                try { return apiClient.getAllSales(); } catch (ApiException e) { error = e.getMessage(); return null; }
            }
            @Override protected void done() {
                salesTableModel.setRowCount(0);
                if (error != null) { JOptionPane.showMessageDialog(SalesReportsPanel.this, "Failed to load sales: " + error); return; }
                try {
                    List<SaleDetailDTO> list = get();
                    if (list == null) return;
                    for (SaleDetailDTO s : list) {
                        StringBuilder payStr = new StringBuilder();
                        if (s.getPayments() != null) {
                            for (PaymentDTO p : s.getPayments()) {
                                if (payStr.length() > 0) payStr.append("; ");
                                payStr.append(p.getMethod()).append(": $").append(String.format("%.2f", p.getAmount()));
                            }
                        }
                        salesTableModel.addRow(new Object[]{
                                s.getReceiptNumber(), s.getTerminalId(), s.getCashierId(),
                                s.getCreatedAt(), String.format("$%.2f", s.getTotal()),
                                String.format("$%.2f", s.getTax()), s.getStatus(), payStr.toString()
                        });
                    }
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    private void loadDailySummary() {
        new SwingWorker<DailySalesSummaryDTO, Void>() {
            @Override protected DailySalesSummaryDTO doInBackground() {
                try { return apiClient.getDailySummary(); } catch (ApiException e) { return null; }
            }
            @Override protected void done() {
                try {
                    DailySalesSummaryDTO sum = get();
                    if (sum == null || totalCard == null) return;
                    totalCard.setText("<html><div style='text-align:center;'><b>Today's Gross Sales</b><br><font size='+1'>$" +
                            String.format("%.2f", sum.getTotalSales()) + "</font></div></html>");
                    transCard.setText("<html><div style='text-align:center;'><b>Total Transactions</b><br><font size='+1'>" +
                            sum.getTotalTransactions() + "</font></div></html>");
                    Map<String, Double> pb = sum.getPaymentBreakdown();
                    Double cash = pb != null ? pb.getOrDefault("Cash", 0.0) : 0.0;
                    Double card = pb != null ? pb.getOrDefault("Card / POS", 0.0) : 0.0;
                    cashCard.setText("<html><div style='text-align:center;'><b>Cash Tenders</b><br><font size='+1'>$" +
                            String.format("%.2f", cash) + "</font></div></html>");
                    cardCard.setText("<html><div style='text-align:center;'><b>Card / POS Tenders</b><br><font size='+1'>$" +
                            String.format("%.2f", card) + "</font></div></html>");
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    private void exportSalesToCsv() {
        if (salesTableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No sales records to export.");
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("VimSpace_Sales_Report_" + System.currentTimeMillis() + ".csv"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try (PrintWriter writer = new PrintWriter(file)) {
                for (int i = 0; i < salesTableModel.getColumnCount(); i++) {
                    writer.print(salesTableModel.getColumnName(i));
                    if (i < salesTableModel.getColumnCount() - 1) writer.print(",");
                }
                writer.println();
                for (int r = 0; r < salesTableModel.getRowCount(); r++) {
                    for (int c = 0; c < salesTableModel.getColumnCount(); c++) {
                        String cell = String.valueOf(salesTableModel.getValueAt(r, c)).replace(",", " ");
                        writer.print(cell);
                        if (c < salesTableModel.getColumnCount() - 1) writer.print(",");
                    }
                    writer.println();
                }
                JOptionPane.showMessageDialog(this, "Sales exported to: " + file.getAbsolutePath());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error exporting CSV: " + ex.getMessage());
            }
        }
    }
}