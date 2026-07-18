package com.lifebalance.timeline.service;

import com.lifebalance.common.api.ModuleStatusResponse;
import org.springframework.stereotype.Service;

@Service
public class TimelineStatusService {

    public ModuleStatusResponse status(String resource) {
        return ModuleStatusResponse.ready(resource);
    }

}
