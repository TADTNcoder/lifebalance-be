package com.lifebalance.task.integration;

interface TaskIntegrationClient {

    void syncTimelineTask(TaskIntegrationEvent event);

    void createNotification(TaskNotificationRequest request, String authorizationHeader);

    void recordActualSeed(TaskActualRecordRequest request, String authorizationHeader);

    void recordMonthlyIncome(MonthlyIncomeTransactionRequest request, String authorizationHeader);
}
