package com.lifebalance.identity.security;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CurrentUser {

    private String userID;

    private String username;

    private String email;

    private List<String> roles;

    public void setUserID(String subject) {
        this.userID = subject;
    }

    public void setUsername(String claimAsString) {
        this.username = claimAsString;
    }

    public void setEmail(String claimAsString) {
        this.email = claimAsString;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}
