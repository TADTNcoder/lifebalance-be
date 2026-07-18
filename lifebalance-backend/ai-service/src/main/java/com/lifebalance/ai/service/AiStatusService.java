package com.lifebalance.ai.service;

import com.lifebalance.common.api.ModuleStatusResponse;
import org.springframework.stereotype.Service;

@Service
public class AiStatusService {

    public ModuleStatusResponse status(String resource) {
        return ModuleStatusResponse.ready(resource);
    }

}
