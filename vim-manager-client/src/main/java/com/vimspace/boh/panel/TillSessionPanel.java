package com.vimspace.boh.panel;

import com.vimspace.boh.service.ApiException;
import com.vimspace.boh.service.BohApiClient;
import com.vimspace.contract.*;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Cash shift / till session management for the Back of House manager.
 *
 * <p>Lets a manager open a session with an opening float, watch the live
 * expected cash build up as sales are rung up, close the session with a
 * counted cash figure, review the reconciliation and browse the shift
 * history.</p>
 */
public class TillSessionPanel extends JPanel {

    private final BohApiClient api;

    // Active sessions
    private final DefaultTableModel activeModel;
    private final JTable activeTable;

    // Open-session form
    private final JTextField terminalField = new JTextField(12);
    private final JTextField cashierField = new JTextField(12);
    private final JTextField openingFloatField = new JTextField(12);
    private final JTextField openNotesField = new JTextField(20);

    // Close-session form
    private final JTextField countedCashField = new JTextField(12);
    private final JTextField closeNotesField = new JTextField(20);

    // History
    private final JComboBox<String> historyStatusCombo =
            new JComboBox<>(new String[]{"ALL", "OPEN", "CLOSED"});
    private final DefaultTableModel historyModel;
    private final JTable historyTable;

    private final JLabel messageLabel = new JLabel(" ");
    private final JLabel summaryLabel = new JLabel(" ");

    public TillSessionPanel(BohApiClient api) {
        this.api = api;
        setLayout(new MigLayout("fill, insets 8", "[grow,fill]", "[][][][]"));

        buildHeader();

        activeModel = new DefaultTableModel(
                new Object[]{"ID", "Terminal", "Cashier", "Opened", "Opening Float",
                        "Cash Sales", "Expected Cash", "Sales"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        activeTable = new JTable(activeModel);
        activeTable.setRowHeight(22);
        activeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        activeTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        activeTable.getColumnModel().getColumn(1).setPreferredWidth(90);
        activeTable.getColumnModel().getColumn(2).setPreferredWidth(90);
        activeTable.getColumnModel().getColumn(3).setPreferredWidth(150);
        activeTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        activeTable.getColumnModel().getColumn(5).setPreferredWidth(100);
        activeTable.getColumnModel().getColumn(6).setPreferredWidth(110);
        activeTable.getColumnModel().getColumn(7).setPreferredWidth(60);
        centerColumns(activeTable, 0, 4, 5, 6, 7);

        historyModel = new DefaultTableModel(
                new Object[]{"ID", "Terminal", "Cashier", "Status", "Opened", "Closed",
                        "Opening Float", "Counted", "Expected", "Variance", "Sales"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        historyTable = new JTable(historyModel);
        historyTable.setRowHeight(22);
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        historyTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        historyTable.getColumnModel().getColumn(3).setPreferredWidth(70);
        historyTable.getColumnModel().getColumn(4).setPreferredWidth(150);
        historyTable.getColumnModel().getColumn(5).setPreferredWidth(150);
        historyTable.getColumnModel().getColumn(6).setPreferredWidth(100);
        historyTable.getColumnModel().getColumn(7).setPreferredWidth(90);
        historyTable.getColumnModel().getColumn(8).setPreferredWidth(100);
        historyTable.getColumnModel().getColumn(9).setPreferredWidth(90);
        historyTable.getColumnModel().getColumn(10).setPreferredWidth(60);
        centerColumns(historyTable, 0, 3, 6, 7, 8, 9, 10);

        add(activeSection(), "grow, span 2, wrap");
        add(openSection(), "grow, span 2, wrap");
        add(historySection(), "grow, span 2, wrap");
        add(messageLabel, "span 2, wrap");
        add(summaryLabel, "span 2");

        refreshAll();
    }
    // ------------------------------------------------------------- sections

    private void buildHeader() {
        JPanel header = new JPanel(new MigLayout("insets 0", "[grow,fill][][][]", "[][]"));
        header.add(new JLabel("Till Sessions & Cash Reconciliation"), "span 3, wrap");
        header.add(new JLabel("Open a cash shift, monitor the expected drawer cash, "
                + "close with a counted total and review the variance."), "span 3, wrap");
        add(header, "span 2, growx, wrap");
    }

    private JPanel activeSection() {
        JPanel panel = new JPanel(new MigLayout("fill, insets 0", "[grow,fill]", "[][]"));
        panel.add(new JLabel("Open Sessions"), "span 2, wrap");
        panel.add(new JScrollPane(activeTable), "grow, span 2, wrap");

        JButton refreshBtn = new JButton("Refresh");
        JButton reconcileBtn = new JButton("Reconcile Selected");
        JButton closeBtn = new JButton("Close Selected...");
        refreshBtn.addActionListener(e -> refreshAll());
        reconcileBtn.addActionListener(e -> reconcileSelected());
        closeBtn.addActionListener(e -> showCloseDialog());

        JPanel buttons = new JPanel(new MigLayout("insets 0", "[][][]", "[]"));
        buttons.add(refreshBtn, "width 100!");
        buttons.add(reconcileBtn, "width 150!");
        buttons.add(closeBtn, "width 130!");
        panel.add(buttons, "span 2, wrap");
        return panel;
    }
    private JPanel openSection() {
        JPanel panel = new JPanel(new MigLayout("fill, insets 0", "[grow,fill]", "[][]"));
        panel.add(new JLabel("Open New Session"), "span 2, wrap");

        JPanel form = new JPanel(new MigLayout("insets 0", "[][150!]", "[][]"));
        form.add(new JLabel("Terminal:"));
        form.add(terminalField, "width 150!, wrap");
        form.add(new JLabel("Cashier:"));
        form.add(cashierField, "width 150!, wrap");
        form.add(new JLabel("Opening Float:"));
        form.add(openingFloatField, "width 150!, wrap");
        form.add(new JLabel("Notes:"));
        form.add(openNotesField, "width 300!, wrap");
        panel.add(form, "span 2, growx, wrap");

        JButton openBtn = new JButton("Open Session");
        openBtn.addActionListener(e -> openSession());
        panel.add(openBtn, "width 120!");
        return panel;
    }
    private JPanel historySection() {
        JPanel panel = new JPanel(new MigLayout("fill, insets 0", "[grow,fill]", "[][]"));
        panel.add(new JLabel("Session History"), "span 2, wrap");
        panel.add(new JScrollPane(historyTable), "grow, span 2, wrap");

        JButton historyRefreshBtn = new JButton("Refresh History");
        historyRefreshBtn.addActionListener(e -> loadHistory());

        JPanel controls = new JPanel(new MigLayout("insets 0", "[][][]", "[]"));
        controls.add(new JLabel("Status:"));
        controls.add(historyStatusCombo, "width 120!");
        controls.add(historyRefreshBtn, "width 140!");
        panel.add(controls, "span 2, wrap");
        return panel;
    }
    // -------------------------------------------------------------- actions

    private void openSession() {
        String terminalId = terminalField.getText().trim();
        String cashierId = cashierField.getText().trim();
        if (terminalId.isEmpty() || cashierId.isEmpty()) {
            showError("Terminal and cashier are both required.");
            return;
        }

        Double openingFloat = parseAmount(openingFloatField.getText());
        if (openingFloat == null) {
            showError("Opening float must be a valid number.");
            return;
        }

        OpenTillSessionRequest request = new OpenTillSessionRequest(terminalId, cashierId, openingFloat);
        request.setNotes(openNotesField.getText().trim());

        setMessage("Opening session...");
        new SwingWorker<TillSessionDTO, Void>() {
            private String error;
            @Override protected TillSessionDTO doInBackground() {
                try { return api.openTillSession(request); }
                catch (ApiException e) { error = e.getMessage(); return null; }
            }
            @Override protected void done() {
                try {
                    TillSessionDTO session = get();
                    if (session == null) {
                        showError(error);
                        return;
                    }
                    showInfo("Session #" + session.getId() + " opened for terminal " + session.getTerminalId()
                            + " with a float of " + money(session.getOpeningFloat()) + ".");
                    openingFloatField.setText("");
                    openNotesField.setText("");
                    refreshAll();
                } catch (Exception ex) {
                    showError(ex.getMessage());
                }
            }
        }.execute();
    }
    private void showCloseDialog() {
        Long sessionId = selectedSessionId(activeTable);
        if (sessionId == null) {
            showError("Please select an open session in the 'Open Sessions' table first.");
            return;
        }

        JPanel form = new JPanel(new MigLayout("insets 5", "[][grow,fill]", "[][]"));
        form.add(new JLabel("Session:"));
        form.add(new JLabel("#" + sessionId), "wrap");
        form.add(new JLabel("Counted Cash:"));
        form.add(countedCashField, "width 140, wrap");
        form.add(new JLabel("Notes:"));
        form.add(closeNotesField, "width 300, wrap");

        int result = JOptionPane.showConfirmDialog(this, form,
                "Close Till Session #" + sessionId,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        Double counted = parseAmount(countedCashField.getText());
        if (counted == null) {
            showError("Counted cash must be a valid number.");
            return;
        }

        CloseTillSessionRequest request = new CloseTillSessionRequest(sessionId, counted);
        request.setNotes(closeNotesField.getText().trim());

        setMessage("Closing session...");
        new SwingWorker<CashReconciliationDTO, Void>() {
            private String error;
            @Override protected CashReconciliationDTO doInBackground() {
                try { return api.closeTillSession(request); }
                catch (ApiException e) { error = e.getMessage(); return null; }
            }
            @Override protected void done() {
                try {
                    CashReconciliationDTO recon = get();
                    if (recon == null) {
                        showError(error);
                        return;
                    }
                    countedCashField.setText("");
                    closeNotesField.setText("");
                    showReconciliation(recon);
                    refreshAll();
                } catch (Exception ex) {
                    showError(ex.getMessage());
                }
            }
        }.execute();
    }
    private void reconcileSelected() {
        Long sessionId = selectedSessionId(activeTable);
        if (sessionId == null) {
            showError("Please select an open session in the 'Open Sessions' table first.");
            return;
        }
        setMessage("Reconciling...");
        new SwingWorker<CashReconciliationDTO, Void>() {
            private String error;
            @Override protected CashReconciliationDTO doInBackground() {
                try { return api.getCashReconciliation(sessionId); }
                catch (ApiException e) { error = e.getMessage(); return null; }
            }
            @Override protected void done() {
                try {
                    CashReconciliationDTO recon = get();
                    if (recon == null) {
                        showError(error);
                        return;
                    }
                    showReconciliation(recon);
                } catch (Exception ex) {
                    showError(ex.getMessage());
                }
            }
        }.execute();
    }
    // ---------------------------------------------------------------- load

    private void refreshAll() {
        loadActiveSessions();
        loadHistory();
    }

    private void loadActiveSessions() {
        new SwingWorker<List<TillSessionDTO>, Void>() {
            private String error;
            @Override protected List<TillSessionDTO> doInBackground() {
                try { return api.getActiveTillSessions(); }
                catch (ApiException e) { error = e.getMessage(); return null; }
            }
            @Override protected void done() {
                try {
                    List<TillSessionDTO> sessions = get();
                    activeModel.setRowCount(0);
                    if (sessions == null) {
                        showError(error);
                        return;
                    }
                    for (TillSessionDTO s : sessions) {
                        activeModel.addRow(new Object[]{
                                s.getId(), s.getTerminalId(), s.getCashierId(), s.getOpenedAt(),
                                money(s.getOpeningFloat()), money(s.getCashSalesTotal()),
                                money(s.getExpectedCash()),
                                s.getSaleCount() != null ? s.getSaleCount() : 0});
                    }
                    summaryLabel.setText(activeSummary(sessions));
                } catch (Exception ex) {
                    showError(ex.getMessage());
                }
            }
        }.execute();
    }
    private void loadHistory() {
        String status = (String) historyStatusCombo.getSelectedItem();
        if ("ALL".equalsIgnoreCase(status)) {
            status = null;
        }
        final String statusFilter = status;
        new SwingWorker<List<TillSessionDTO>, Void>() {
            private String error;
            @Override protected List<TillSessionDTO> doInBackground() {
                try { return api.getTillSessionHistory(statusFilter); }
                catch (ApiException e) { error = e.getMessage(); return null; }
            }
            @Override protected void done() {
                try {
                    List<TillSessionDTO> sessions = get();
                    historyModel.setRowCount(0);
                    if (sessions == null) {
                        showError(error);
                        return;
                    }
                    for (TillSessionDTO s : sessions) {
                        historyModel.addRow(new Object[]{
                                s.getId(), s.getTerminalId(), s.getCashierId(), s.getStatus(),
                                s.getOpenedAt(), s.getClosedAt(), money(s.getOpeningFloat()),
                                money(s.getCountedCash()), money(s.getExpectedCash()),
                                money(s.getVariance()),
                                s.getSaleCount() != null ? s.getSaleCount() : 0});
                    }
                } catch (Exception ex) {
                    showError(ex.getMessage());
                }
            }
        }.execute();
    }
    // -------------------------------------------------------------- helpers

    /** Visible for the panel smoke test. */
    public DefaultTableModel getActiveTableModel() {
        return activeModel;
    }

    /** Visible for the panel smoke test. */
    public DefaultTableModel getHistoryTableModel() {
        return historyModel;
    }

    /** Visible for the panel smoke test. */
    public String getSummaryText() {
        return summaryLabel.getText();
    }

    private Long selectedSessionId(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) {
            return null;
        }
        Object value = table.getValueAt(row, 0);
        if (value == null) {
            return null;
        }
        try {
            return Long.valueOf(value.toString());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private void centerColumns(JTable table, int... columns) {
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(JLabel.CENTER);
        for (int col : columns) {
            if (col < table.getColumnModel().getColumnCount()) {
                table.getColumnModel().getColumn(col).setCellRenderer(renderer);
            }
        }
    }

    private static String money(Double value) {
        return value == null ? "-" : String.format("$%.2f", value);
    }

    /** Parses a currency amount, returning {@code null} when the text is not a number. */
    private static Double parseAmount(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        String cleaned = text.trim().replace("$", "").replace(",", "");
        try {
            return Double.valueOf(cleaned);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String activeSummary(List<TillSessionDTO> sessions) {
        if (sessions == null || sessions.isEmpty()) {
            return "No open sessions.";
        }
        double expected = 0;
        int saleCount = 0;
        for (TillSessionDTO s : sessions) {
            expected += s.getExpectedCash() != null ? s.getExpectedCash() : 0;
            saleCount += s.getSaleCount() != null ? s.getSaleCount() : 0;
        }
        return sessions.size() + " open session(s), " + saleCount
                + " sale(s) totalling " + money(expected) + " expected in the drawer.";
    }

    private void showReconciliation(CashReconciliationDTO recon) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><b>Cash Reconciliation — Session #").append(recon.getSessionId()).append("</b><br><br>");
        sb.append("Terminal: ").append(recon.getTerminalId())
          .append(" &nbsp; Cashier: ").append(recon.getCashierId()).append("<br>");
        sb.append("Opening Float: ").append(money(recon.getOpeningFloat())).append("<br>");
        sb.append("Cash Sales: ").append(money(recon.getCashSalesTotal()))
          .append(" (").append(recon.getSaleCount() != null ? recon.getSaleCount() : 0).append(" sale(s))<br>");
        sb.append("Expected Cash: ").append(money(recon.getExpectedCash())).append("<br>");
        sb.append("Counted Cash: ").append(money(recon.getCountedCash())).append("<br>");
        sb.append("<b>Variance: ").append(money(recon.getVariance())).append("</b><br>");
        sb.append(recon.isBalanced() ? "<b>Balanced</b>" : "<b>Out of balance</b>");
        if (recon.getNotes() != null && !recon.getNotes().isBlank()) {
            sb.append("<br>Notes: ").append(recon.getNotes());
        }
        sb.append("</html>");

        JOptionPane.showMessageDialog(this, sb.toString(),
                "Cash Reconciliation", JOptionPane.INFORMATION_MESSAGE);
        setMessage("Reconciliation complete for session #" + recon.getSessionId() + ".");
    }

    private void setMessage(String text) {
        messageLabel.setText(text);
        messageLabel.setForeground(UIManager.getColor("Label.foreground"));
    }

    private void showError(String message) {
        messageLabel.setText(message == null ? " " : message);
        messageLabel.setForeground(Color.RED.darker());
    }

    private void showInfo(String message) {
        messageLabel.setText(message);
        messageLabel.setForeground(new Color(0, 110, 40));
    }
}
