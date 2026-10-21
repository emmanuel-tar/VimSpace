package com.vimspace.foh;

import com.formdev.flatlaf.FlatLightLaf;
import com.vimspace.foh.service.ApiClient;
import com.vimspace.foh.ui.LoginFrame;

import javax.swing.*;

public class FrontRetailApp {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ignored) {}

        ApiClient apiClient = new ApiClient();
        SwingUtilities.invokeLater(() -> new LoginFrame(apiClient).setVisible(true));
    }
}
