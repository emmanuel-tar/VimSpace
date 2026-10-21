package com.vimspace.boh.config;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Central configuration for the Vim Manager client.
 * Reads settings from config.properties (if present) with sensible defaults,
 * and holds the current session auth token.
 */
public class AppConfig {

    private static final String DEFAULT_BASE_URL = "http://localhost:8080/api";
    private static final Path CONFIG_FILE = Paths.get("config", "app.properties");

    private final String baseUrl;
    private String authToken;
    private String loggedInUsername;
    private String loggedInRole;

    public AppConfig() {
        Properties props = new Properties();
        try {
            if (Files.exists(CONFIG_FILE)) {
                try (InputStream in = Files.newInputStream(CONFIG_FILE)) {
                    props.load(in);
                }
            } else {
                // Fallback to classpath resource
                try (InputStream in = AppConfig.class.getClassLoader()
                        .getResourceAsStream("app.properties")) {
                    if (in != null) {
                        props.load(in);
                    }
                }
            }
        } catch (Exception ignored) {
            // Use defaults
        }

        this.baseUrl = props.getProperty("api.base.url",
                System.getenv("VIMSPACE_API_BASE_URL") != null
                        ? System.getenv("VIMSPACE_API_BASE_URL")
                        : DEFAULT_BASE_URL);
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public String getLoggedInUsername() {
        return loggedInUsername;
    }

    public void setLoggedInUsername(String loggedInUsername) {
        this.loggedInUsername = loggedInUsername;
    }

    public String getLoggedInRole() {
        return loggedInRole;
    }

    public void setLoggedInRole(String loggedInRole) {
        this.loggedInRole = loggedInRole;
    }

    public boolean isAuthenticated() {
        return authToken != null && !authToken.isBlank();
    }

    public void clearSession() {
        this.authToken = null;
        this.loggedInUsername = null;
        this.loggedInRole = null;
    }
}
