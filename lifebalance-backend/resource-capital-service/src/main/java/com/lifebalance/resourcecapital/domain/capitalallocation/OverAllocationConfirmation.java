package com.lifebalance.resourcecapital.domain.capitalallocation;

import com.lifebalance.resourcecapital.domain.capital.CapitalKind;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public final class OverAllocationConfirmation {

    public static final String CONFIRMATION_FIELD = "overAllocationConfirmationKey";
    public static final String REMAINING_EXPLANATION =
            "Negative remaining is an over-allocation state, not additional available capital.";

    private static final int MONEY_SCALE = 4;
    private static final String KEY_PREFIX = "oac";

    private OverAllocationConfirmation() {
    }

    public static String allocationReference(AllocationTargetType targetType, UUID targetId) {
        if (targetType == null || targetId == null) {
            return "ALLOCATION_TARGET:UNKNOWN";
        }
        return "ALLOCATION_TARGET:" + targetType.name() + ":" + targetId;
    }

    public static String adjustmentReference(String adjustmentType) {
        if (adjustmentType == null || adjustmentType.isBlank()) {
            return "CAPITAL_ADJUSTMENT:UNKNOWN";
        }
        return "CAPITAL_ADJUSTMENT:" + adjustmentType;
    }

    public static String confirmationKey(
            String operationType,
            UUID cycleId,
            CapitalKind capitalType,
            String operationReference,
            BigDecimal requestedAmount,
            BigDecimal availableAmount,
            BigDecimal projectedRemainingAmount
    ) {
        String payload = String.join(
                "|",
                "OVER_ALLOCATION_CONFIRMATION_V1",
                normalizeText(operationType),
                cycleId == null ? "" : cycleId.toString(),
                capitalType == null ? "" : capitalType.name(),
                normalizeText(operationReference),
                normalizeAmount(requestedAmount),
                normalizeAmount(availableAmount),
                normalizeAmount(projectedRemainingAmount)
        );
        return KEY_PREFIX + "_" + sha256(payload);
    }

    public static boolean matches(String providedKey, String expectedKey) {
        if (providedKey == null || expectedKey == null) {
            return false;
        }
        return MessageDigest.isEqual(
                providedKey.getBytes(StandardCharsets.UTF_8),
                expectedKey.getBytes(StandardCharsets.UTF_8)
        );
    }

    public static Map<String, String> details(
            UUID cycleId,
            CapitalKind capitalType,
            BigDecimal availableAmount,
            BigDecimal requestedAmount,
            BigDecimal projectedRemainingAmount,
            String operationType,
            String operationReference,
            String confirmationKey
    ) {
        BigDecimal normalizedProjectedRemaining = normalize(projectedRemainingAmount);
        Map<String, String> details = new LinkedHashMap<>();
        details.put("confirmationRequired", "true");
        details.put("confirmationField", CONFIRMATION_FIELD);
        details.put("confirmationKey", confirmationKey);
        details.put("operationType", normalizeText(operationType));
        details.put("operationReference", normalizeText(operationReference));
        details.put("capitalCycleId", cycleId == null ? "" : cycleId.toString());
        details.put("capitalType", capitalType == null ? "" : capitalType.name());
        details.put("availableAmount", normalizeAmount(availableAmount));
        details.put("requestedAmount", normalizeAmount(requestedAmount));
        details.put("shortageAmount", normalizeAmount(normalizedProjectedRemaining.abs()));
        details.put("projectedRemainingAmount", normalizeAmount(normalizedProjectedRemaining));
        details.put("remainingState", "OVER_ALLOCATED");
        details.put("remainingExplanation", REMAINING_EXPLANATION);
        return details;
    }

    private static String sha256(String payload) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 digest is not available.", exception);
        }
    }

    private static String normalizeText(String value) {
        if (value == null || value.isBlank()) {
            return "UNKNOWN";
        }
        return value.trim();
    }

    private static String normalizeAmount(BigDecimal amount) {
        return normalize(amount).toPlainString();
    }

    private static BigDecimal normalize(BigDecimal amount) {
        if (amount == null) {
            return BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.UNNECESSARY);
        }
        return amount.setScale(MONEY_SCALE, RoundingMode.UNNECESSARY);
    }
}
