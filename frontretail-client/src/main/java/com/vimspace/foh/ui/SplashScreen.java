package com.vimspace.foh.ui;

import javax.swing.*;
import java.awt.*;

public class SplashScreen extends JWindow {
    public SplashScreen() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(30, 60, 114));
        JLabel title = new JLabel("VimSpace Superstore POS", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        JLabel sub = new JLabel("Loading FrontRetail...", SwingConstants.CENTER);
        sub.setForeground(Color.LIGHT_GRAY);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(title, BorderLayout.CENTER);
        panel.add(sub, BorderLayout.SOUTH);
        getContentPane().add(panel);
        setSize(480, 260);
        setLocationRelativeTo(null);
    }
}
