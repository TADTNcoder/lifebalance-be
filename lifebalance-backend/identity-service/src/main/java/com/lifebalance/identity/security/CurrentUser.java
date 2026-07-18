package com.lifebalance.identity.security;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

public class CurrentUser {

    private String userId;

    private String username;

    private String email;

    private List<String> roles;
}
