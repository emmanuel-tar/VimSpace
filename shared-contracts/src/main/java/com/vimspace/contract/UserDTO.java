package com.vimspace.contract;

public class UserDTO {
    private Long id;
    private String username;
    private String role;
    private Boolean active;

    public UserDTO() {}

    public UserDTO(Long id, String username, String role, Boolean active) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.active = active;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}

