package com.lifebalance.notification.service;

import com.lifebalance.common.api.ModuleStatusResponse;
import org.springframework.stereotype.Service;

@Service
public class NotificationStatusService {

    public ModuleStatusResponse status(String resource) {
        return ModuleStatusResponse.ready(resource);
    }

}
