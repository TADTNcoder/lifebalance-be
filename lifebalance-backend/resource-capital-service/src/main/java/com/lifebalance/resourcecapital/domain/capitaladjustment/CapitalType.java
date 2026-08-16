package com.lifebalance.resourcecapital.domain.capitaladjustment;

import com.lifebalance.resourcecapital.domain.capital.CapitalKind;

public enum CapitalType {
    TIME,
    MONEY;

    public static CapitalType from(CapitalKind capitalKind) {
        if (capitalKind == null) {
            return null;
        }
        return switch (capitalKind) {
            case TIME -> TIME;
            case MONEY -> MONEY;
        };
    }

    public CapitalKind toCapitalKind() {
        return switch (this) {
            case TIME -> CapitalKind.TIME;
            case MONEY -> CapitalKind.MONEY;
        };
    }
}
