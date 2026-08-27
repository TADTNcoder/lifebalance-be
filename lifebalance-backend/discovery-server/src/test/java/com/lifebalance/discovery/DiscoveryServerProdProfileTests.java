package com.lifebalance.discovery;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "spring.profiles.active=prod")
class DiscoveryServerProdProfileTests {

    @Test
    void contextLoadsWithProdDefaults() {
    }
}
