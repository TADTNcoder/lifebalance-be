package com.lifebalance.resourcecapital.integration;

interface CapitalIntegrationClient {

    void createNotification(CapitalNotificationRequest request, String authorizationHeader);
}
