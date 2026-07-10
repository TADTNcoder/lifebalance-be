package com.lifebalance.task.service;

import com.lifebalance.task.shared.api.ModuleStatusResponse;
import org.springframework.stereotype.Service;

@Service
public class TaskStatusService {

    public ModuleStatusResponse status(String resource) {
        return ModuleStatusResponse.ready(resource);
    }

}
