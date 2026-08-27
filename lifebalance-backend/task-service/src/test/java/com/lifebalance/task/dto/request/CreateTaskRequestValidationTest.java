package com.lifebalance.task.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CreateTaskRequestValidationTest {

    private static final ValidatorFactory VALIDATOR_FACTORY = Validation.buildDefaultValidatorFactory();
    private static final Validator VALIDATOR = VALIDATOR_FACTORY.getValidator();

    @AfterAll
    static void closeValidatorFactory() {
        VALIDATOR_FACTORY.close();
    }

    @Test
    void acceptsDraftLikeRequestWithoutPlanningWindowOrEstimate() {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setName("Read a chapter");

        assertThat(VALIDATOR.validate(request)).isEmpty();
    }

    @Test
    void rejectsNonPositiveEstimatedMinutesWhenProvided() {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setName("Read a chapter");
        request.setEstimatedMinutes(0);

        assertThat(fieldsOf(VALIDATOR.validate(request))).contains("estimatedMinutes");
    }

    @Test
    void rejectsPartialPlanningWindow() {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setName("Read a chapter");
        request.setEstimatedMinutes(30);
        request.setPlannedStartAt(OffsetDateTime.parse("2026-08-26T09:00:00+07:00"));

        assertThat(fieldsOf(VALIDATOR.validate(request))).contains("plannedEndAt");
    }

    @Test
    void rejectsPlanningWindowWhenEndIsNotAfterStart() {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setName("Read a chapter");
        request.setEstimatedMinutes(30);
        request.setPlannedStartAt(OffsetDateTime.parse("2026-08-26T09:00:00+07:00"));
        request.setPlannedEndAt(OffsetDateTime.parse("2026-08-26T09:00:00+07:00"));

        assertThat(fieldsOf(VALIDATOR.validate(request))).contains("plannedEndAt");
    }

    private Set<String> fieldsOf(Set<ConstraintViolation<CreateTaskRequest>> violations) {
        return violations.stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(java.util.stream.Collectors.toSet());
    }
}
