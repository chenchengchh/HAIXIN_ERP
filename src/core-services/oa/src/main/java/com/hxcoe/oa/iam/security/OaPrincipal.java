package com.hxcoe.oa.iam.security;

public class OaPrincipal {

    private final Long userId;
    private final Long employeeId;
    private final String username;

    public OaPrincipal(Long userId, Long employeeId, String username) {
        this.userId = userId;
        this.employeeId = employeeId;
        this.username = username;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public String toString() {
        return username == null ? "" : username;
    }
}

