package com.lifebalance.ai;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "eureka.client.enabled=false",
        "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost/.well-known/jwks.json"
})
class AiServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}
