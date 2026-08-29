package com.lifebalance.identity.service;

public interface CurrentPasswordVerifier {

    boolean verify(String username, String currentPassword);
}
