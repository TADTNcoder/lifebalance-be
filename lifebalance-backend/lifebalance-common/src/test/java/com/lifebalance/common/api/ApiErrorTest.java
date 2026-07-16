package com.lifebalance.common.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ApiErrorTest {

    @Test
    void shouldDefensivelyCopyDetails() {
        Map<String, String> details = new HashMap<>();
        details.put("field", "invalid");

        ApiError error = ApiError.of("CODE", "Message", details);

        details.put("field", "changed");

        assertThat(error.details()).containsEntry("field", "invalid");
        assertThatThrownBy(() -> error.details().put("other", "value"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

}
