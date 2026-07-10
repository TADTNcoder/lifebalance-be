package com.lifebalance.finance.service;

import com.lifebalance.finance.shared.api.ModuleStatusResponse;
import org.springframework.stereotype.Service;

@Service
public class FinanceStatusService {

    public ModuleStatusResponse status(String resource) {
        return ModuleStatusResponse.ready(resource);
    }

}
