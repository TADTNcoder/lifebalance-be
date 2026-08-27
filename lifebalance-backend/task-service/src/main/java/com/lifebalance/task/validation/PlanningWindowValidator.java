package com.lifebalance.task.validation;

import com.lifebalance.task.dto.request.PlanningWindowRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.OffsetDateTime;

public class PlanningWindowValidator implements ConstraintValidator<ValidPlanningWindow, PlanningWindowRequest> {

    @Override
    public boolean isValid(
            PlanningWindowRequest request,
            ConstraintValidatorContext context) {

        if (request == null) {
            return true;
        }

        OffsetDateTime plannedStartAt = request.getPlannedStartAt();
        OffsetDateTime plannedEndAt = request.getPlannedEndAt();
        if (plannedStartAt == null && plannedEndAt == null) {
            return true;
        }

        boolean valid = true;
        context.disableDefaultConstraintViolation();

        if (plannedStartAt == null) {
            valid = false;
            addViolation(
                    context,
                    "plannedStartAt",
                    "Planned start time is required when planned end time is provided.");
        }

        if (plannedEndAt == null) {
            valid = false;
            addViolation(
                    context,
                    "plannedEndAt",
                    "Planned end time is required when planned start time is provided.");
        }

        if (plannedStartAt != null
                && plannedEndAt != null
                && !plannedStartAt.isBefore(plannedEndAt)) {

            valid = false;
            addViolation(
                    context,
                    "plannedEndAt",
                    "Planned end time must be after planned start time.");
        }

        return valid;
    }

    private void addViolation(
            ConstraintValidatorContext context,
            String field,
            String message) {

        context
                .buildConstraintViolationWithTemplate(message)
                .addPropertyNode(field)
                .addConstraintViolation();
    }
}
