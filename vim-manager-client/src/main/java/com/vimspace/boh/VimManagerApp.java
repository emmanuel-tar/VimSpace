package com.vimspace.boh;

import com.formdev.flatlaf.FlatLightLaf;
import com.vimspace.boh.config.AppConfig;
import com.vimspace.boh.panel.InventoryPanel;
import com.vimspace.boh.panel.LowStockPanel;
import com.vimspace.boh.panel.ProductPanel;
import com.vimspace.boh.panel.SalesReportsPanel;
import com.vimspace.boh.panel.SupplierPanel;
import com.vimspace.boh.panel.TillSessionPanel;
import com.vimspace.boh.panel.UserPanel;
import com.vimspace.boh.panel.PurchaseOrderPanel;
import com.vimspace.boh.service.BohApiClient;

import javax.swing.*;
import java.awt.*;

/**
 * VimSpace Back of House — Vim Manager.
 * Shows a login dialog first, then the main tabbed BOH interface.
 */
public class VimManagerApp extends JFrame {

    private final AppConfig config;
    private final BohApiClient apiClient;

    public VimManagerApp(AppConfig config, BohApiClient apiClient) {
        super("VimSpace Back of House — Vim Manager (" + config.getLoggedInUsername() + " [" + config.getLoggedInRole() + "])");
        this.config = config;
        this.apiClient = apiClient;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1220, 780);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));

        tabbedPane.addTab("Product Catalog", new ProductPanel(config, apiClient));
        tabbedPane.addTab("Low Stock Report", new LowStockPanel(config, apiClient));
        tabbedPane.addTab("Inventory & Stock", new InventoryPanel(config, apiClient));
        tabbedPane.addTab("Sales & Reports", new SalesReportsPanel(config, apiClient));
        tabbedPane.addTab("Till Sessions", new TillSessionPanel(apiClient));
        tabbedPane.addTab("Suppliers", new SupplierPanel(apiClient));
        tabbedPane.addTab("Purchase Orders", new PurchaseOrderPanel(apiClient));
        tabbedPane.addTab("Staff & Users", new UserPanel(config, apiClient));

        add(tabbedPane, BorderLayout.CENTER);

        JLabel statusLabel = new JLabel("Logged in as: " + config.getLoggedInUsername() + " (" + config.getLoggedInRole() + ")   |   API: " + config.getBaseUrl());
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        add(statusLabel, BorderLayout.SOUTH);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            AppConfig config = new AppConfig();
            BohApiClient apiClient = new BohApiClient(config);

            // Show login dialog first
            LoginDialog loginDlg = new LoginDialog(null, config, apiClient);
            loginDlg.setVisible(true);

            if (!loginDlg.isAuthenticated()) {
                JOptionPane.showMessageDialog(null, "Login required. Exiting.");
                System.exit(0);
                return;
            }

            VimManagerApp app = new VimManagerApp(config, apiClient);
            app.setVisible(true);
        });
    }
}
