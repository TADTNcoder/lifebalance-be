package com.lifebalance.app;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "eureka.client.enabled=false")
class AppApplicationTests {

    @Test
    void contextLoads() {
    }

}
