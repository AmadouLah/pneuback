package com.pneumaliback.www.enums;

public enum Role {
    ADMIN("Administrateur"),
    CLIENT("Client"),
    INFLUENCEUR("Influenceur");
    
    private final String displayName;
    
    Role(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public boolean hasAdminPrivileges() {
        return this == ADMIN;
    }
}
