package com.lifebalance.identity.service;

import com.lifebalance.identity.shared.api.ModuleStatusResponse;
import org.springframework.stereotype.Service;

@Service
public class IdentityStatusService {

    public ModuleStatusResponse status(String resource) {
        return ModuleStatusResponse.ready(resource);
    }

}
