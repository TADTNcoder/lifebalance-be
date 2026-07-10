package com.lifebalance.analytics.service;

import com.lifebalance.analytics.shared.api.ModuleStatusResponse;
import org.springframework.stereotype.Service;

@Service
public class AnalyticsStatusService {

    public ModuleStatusResponse status(String resource) {
        return ModuleStatusResponse.ready(resource);
    }

}
