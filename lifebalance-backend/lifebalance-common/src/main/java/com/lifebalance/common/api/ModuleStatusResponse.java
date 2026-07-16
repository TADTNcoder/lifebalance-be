package com.lifebalance.common.api;

public record ModuleStatusResponse(
        String module,
        String status
) {

    public static ModuleStatusResponse ready(String module) {
        return new ModuleStatusResponse(module, "ready");
    }

}
