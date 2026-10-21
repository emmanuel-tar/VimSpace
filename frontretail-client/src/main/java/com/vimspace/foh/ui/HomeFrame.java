package com.vimspace.foh.ui;

import com.vimspace.foh.service.ApiClient;

import javax.swing.*;
import java.awt.*;

public class HomeFrame extends JFrame {
    private final ApiClient apiClient;
    private final String user;
    private final String role;
    private final JPanel content = new JPanel(new CardLayout());

    public HomeFrame(ApiClient apiClient, String user, String role) {
        this.apiClient = apiClient;
        this.user = user;
        this.role = role;
        setTitle("VimSpace FrontRetail — POS Terminal");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1080, 720);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.add(createHeader(), BorderLayout.NORTH);
        root.add(createSidebar(), BorderLayout.WEST);
        root.add(content, BorderLayout.CENTER);

        add(root);

        CardLayout cl = (CardLayout) content.getLayout();
        content.add(new CheckoutPanel(apiClient, user), "checkout");
        content.add(new TillPanel(), "till");
        content.add(new DashboardPanel(), "dashboard");
        cl.show(content, "checkout");
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(25, 42, 86));
        header.setPreferredSize(new Dimension(0, 56));
        header.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));

        JLabel title = new JLabel("VimSpace Superstore  |  FrontRetail POS");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JLabel userLabel = new JLabel("Cashier: " + user + "  [" + role + "]");
        userLabel.setForeground(new Color(220, 220, 220));
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        header.add(title, BorderLayout.WEST);
        header.add(userLabel, BorderLayout.EAST);
        return header;
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(44, 62, 80));
        sidebar.setPreferredSize(new Dimension(210, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));

        JButton logout = new JButton("Log Out");
        logout.setAlignmentX(Component.CENTER_ALIGNMENT);
        logout.setMaximumSize(new Dimension(190, 38));
        logout.addActionListener(e -> {
            dispose();
            new LoginFrame(apiClient).setVisible(true);
        });

        sidebar.add(menuBtn("Checkout / Register", "checkout"));
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(menuBtn("Till Management", "till"));
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(menuBtn("Terminal Overview", "dashboard"));
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(logout);
        return sidebar;
    }

    private JButton menuBtn(String label, String card) {
        JButton b = new JButton(label);
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        b.setMaximumSize(new Dimension(190, 42));
        b.setFocusPainted(false);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.addActionListener(e -> ((CardLayout) content.getLayout()).show(content, card));
        return b;
    }

    static class TillPanel extends JPanel {
        TillPanel() {
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            JLabel t = new JLabel("Till Session & Cash Drawer", SwingConstants.CENTER);
            t.setFont(new Font("Segoe UI", Font.BOLD, 22));
            add(t, BorderLayout.NORTH);

            JPanel center = new JPanel(new GridBagLayout());
            center.add(new JLabel("Active Till: Drawer #1 — Status: OPEN (Float: $200.00)"));
            add(center, BorderLayout.CENTER);
        }
    }

    static class DashboardPanel extends JPanel {
        DashboardPanel() {
            setLayout(new GridLayout(2, 2, 16, 16));
            setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            addCard("Terminal", "TERM-01 (Active)", new Color(30, 60, 114));
            addCard("Today's Transactions", "Online Sync OK", new Color(20, 120, 60));
            addCard("Connection", "Backend 8080: Connected", new Color(120, 80, 10));
            addCard("Printer Status", "ESC/POS Ready", new Color(90, 60, 140));
        }

        void addCard(String title, String value, Color c) {
            JPanel card = new JPanel(new BorderLayout());
            card.setBackground(c);
            card.setBorder(BorderFactory.createLineBorder(c.darker(), 1));
            JLabel t = new JLabel("  " + title, SwingConstants.LEFT);
            t.setForeground(Color.WHITE);
            t.setFont(new Font("Segoe UI", Font.BOLD, 13));
            JLabel v = new JLabel(value, SwingConstants.CENTER);
            v.setForeground(Color.WHITE);
            v.setFont(new Font("Segoe UI", Font.BOLD, 18));
            card.add(t, BorderLayout.NORTH);
            card.add(v, BorderLayout.CENTER);
            add(card);
        }
    }
}
