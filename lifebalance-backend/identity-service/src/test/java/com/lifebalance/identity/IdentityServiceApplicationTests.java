package com.lifebalance.identity;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.profiles.active=test",
        "eureka.client.enabled=false"
})
class IdentityServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}
