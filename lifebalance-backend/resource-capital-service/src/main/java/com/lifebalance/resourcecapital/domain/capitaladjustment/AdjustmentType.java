package com.lifebalance.resourcecapital.domain.capitaladjustment;

import com.lifebalance.resourcecapital.domain.capital.CapitalAdjustmentType;

public enum AdjustmentType {
    INCREASE,
    DECREASE,
    OVERRIDE;

    public static AdjustmentType from(CapitalAdjustmentType adjustmentType) {
        if (adjustmentType == null) {
            return null;
        }
        return switch (adjustmentType) {
            case INCREASE -> INCREASE;
            case DECREASE -> DECREASE;
            case OVERRIDE -> OVERRIDE;
        };
    }

    public CapitalAdjustmentType toCapitalAdjustmentType() {
        return switch (this) {
            case INCREASE -> CapitalAdjustmentType.INCREASE;
            case DECREASE -> CapitalAdjustmentType.DECREASE;
            case OVERRIDE -> CapitalAdjustmentType.OVERRIDE;
        };
    }
}
