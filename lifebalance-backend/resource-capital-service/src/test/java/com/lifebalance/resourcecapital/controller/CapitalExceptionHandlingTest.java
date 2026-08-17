package com.lifebalance.resourcecapital.controller;

import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lifebalance.common.LifebalanceCommonAutoConfiguration;
import com.lifebalance.common.error.CommonErrorCode;
import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.domain.capital.exception.CapitalAllocationDataIntegrityException;
import com.lifebalance.resourcecapital.domain.capital.exception.CapitalAlreadyInitializedException;
import com.lifebalance.resourcecapital.domain.capital.exception.CapitalNotSetupException;
import com.lifebalance.resourcecapital.domain.capital.exception.InvalidAdjustmentAmountException;
import com.lifebalance.resourcecapital.domain.capitalallocation.AllocationStatus;
import com.lifebalance.resourcecapital.domain.capitalallocation.exception.InsufficientAvailableCapitalException;
import com.lifebalance.resourcecapital.domain.capitalallocation.exception.InvalidAllocationAmountException;
import com.lifebalance.resourcecapital.domain.capitalallocation.exception.InvalidAllocationStateException;
import com.lifebalance.resourcecapital.domain.capitalallocation.exception.OverAllocationConfirmationRequiredException;
import com.lifebalance.resourcecapital.domain.capitalallocation.exception.OverAllocationNotAllowedException;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycleStatus;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycleType;
import com.lifebalance.resourcecapital.domain.capitalcycle.exception.ActiveCapitalCycleAlreadyExistsException;
import com.lifebalance.resourcecapital.domain.capitalcycle.exception.CapitalCycleNotFoundException;
import com.lifebalance.resourcecapital.domain.capitalcycle.exception.CapitalCycleOwnershipException;
import com.lifebalance.resourcecapital.domain.capitalcycle.exception.InvalidCapitalCycleStateException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@WebMvcTest(controllers = CapitalExceptionHandlingTest.TestController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({
        LifebalanceCommonAutoConfiguration.class,
        CapitalExceptionHandlingTest.TestControllerConfig.class
})
class CapitalExceptionHandlingTest {

    private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID CYCLE_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final String BASE_PATH = "/lb-806/capital-errors";

    @Autowired
    private MockMvc mockMvc;

    @TestConfiguration
    static class TestControllerConfig {

        @Bean
        TestController capitalExceptionTestController() {
            return new TestController();
        }
    }

    @ParameterizedTest
    @CsvSource({
            "missing-cycle,404,CAPITAL_CYCLE_NOT_FOUND",
            "active-cycle-exists,409,CAPITAL_CYCLE_ACTIVE_ALREADY_EXISTS",
            "invalid-cycle-state,400,INVALID_CYCLE_STATE_TRANSITION",
            "capital-not-setup,409,CAPITAL_NOT_SETUP",
            "invalid-adjustment-amount,400,CAPITAL_INVALID_ADJUSTMENT_AMOUNT",
            "invalid-allocation-amount,400,CAPITAL_INVALID_ALLOCATION_AMOUNT",
            "invalid-allocation-state,409,CAPITAL_ALLOCATION_INVALID_STATE",
            "insufficient-available-capital,409,INSUFFICIENT_AVAILABLE_CAPITAL",
            "over-allocation-not-allowed,409,CAPITAL_OVER_ALLOCATION_NOT_ALLOWED",
            "over-allocation-confirmation,409,CAPITAL_OVER_ALLOCATION_CONFIRMATION_REQUIRED"
    })
    void capitalDomainExceptionsUseGlobalStandardErrorPayload(
            String scenario,
            int expectedStatus,
            String expectedCode
    ) throws Exception {
        mockMvc.perform(get(BASE_PATH + "/" + scenario))
                .andExpect(status().is(expectedStatus))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").value(nullValue()))
                .andExpect(jsonPath("$.error.code").value(expectedCode))
                .andExpect(jsonPath("$.error.message").isNotEmpty())
                .andExpect(jsonPath("$.error.details", anEmptyMap()))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void capitalOwnershipMismatchIsNormalizedAsNotFound() throws Exception {
        mockMvc.perform(get(BASE_PATH + "/ownership-mismatch"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(CapitalCycleNotFoundException.ERROR_CODE))
                .andExpect(jsonPath("$.error.message").value("Capital cycle " + CYCLE_ID + " was not found."))
                .andExpect(jsonPath("$.error.details", anEmptyMap()))
                .andExpect(content().string(not(containsString("OWNERSHIP"))))
                .andExpect(content().string(not(containsString(OWNER_ID.toString()))));
    }

    @Test
    void capitalAlreadyInitializedKeepsTypeSpecificErrorCodes() throws Exception {
        mockMvc.perform(get(BASE_PATH + "/already-initialized/TIME"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("TIME_CAPITAL_ALREADY_EXISTS"));

        mockMvc.perform(get(BASE_PATH + "/already-initialized/MONEY"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("MONEY_CAPITAL_ALREADY_EXISTS"));
    }

    @Test
    void validationExceptionKeepsFieldDetailsMap() throws Exception {
        mockMvc.perform(post(BASE_PATH + "/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(CommonErrorCode.VALIDATION_FAILED))
                .andExpect(jsonPath("$.error.details.name").isNotEmpty())
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void unknownRuntimeExceptionUsesGenericInternalErrorPayload() throws Exception {
        mockMvc.perform(get(BASE_PATH + "/unknown-runtime"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(CommonErrorCode.INTERNAL_ERROR))
                .andExpect(jsonPath("$.error.message").value("Unexpected server error"))
                .andExpect(jsonPath("$.error.details", anEmptyMap()))
                .andExpect(content().string(not(containsString("RuntimeException"))))
                .andExpect(content().string(not(containsString("select * from capital_cycles"))))
                .andExpect(content().string(not(containsString("database-password"))));
    }

    @Test
    void capitalDataIntegrityErrorDoesNotExposePersistedDataDetails() throws Exception {
        mockMvc.perform(get(BASE_PATH + "/data-integrity"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(CapitalAllocationDataIntegrityException.ERROR_CODE))
                .andExpect(jsonPath("$.error.message").value("Capital allocation data is inconsistent."))
                .andExpect(jsonPath("$.error.details", anEmptyMap()))
                .andExpect(content().string(not(containsString(CYCLE_ID.toString()))))
                .andExpect(content().string(not(containsString("invalid persisted format"))));
    }

    @RestController
    @RequestMapping(BASE_PATH)
    public static class TestController {

        @GetMapping("/{scenario}")
        void throwCapitalException(@PathVariable String scenario) {
            switch (scenario) {
                case "missing-cycle" -> throw new CapitalCycleNotFoundException(CYCLE_ID);
                case "active-cycle-exists" ->
                        throw new ActiveCapitalCycleAlreadyExistsException(OWNER_ID, CapitalCycleType.MONTHLY);
                case "invalid-cycle-state" ->
                        throw new InvalidCapitalCycleStateException(
                                CYCLE_ID,
                                CapitalCycleStatus.CLOSED,
                                "update",
                                "closed cycles cannot be edited"
                        );
                case "capital-not-setup" -> throw new CapitalNotSetupException(CYCLE_ID, CapitalKind.TIME);
                case "invalid-adjustment-amount" ->
                        throw InvalidAdjustmentAmountException.invalidMoney("amount must be greater than zero");
                case "invalid-allocation-amount" ->
                        throw new InvalidAllocationAmountException("Allocation amount must be greater than zero.");
                case "invalid-allocation-state" ->
                        throw new InvalidAllocationStateException(
                                UUID.fromString("33333333-3333-3333-3333-333333333333"),
                                AllocationStatus.RELEASED,
                                "release capital"
                        );
                case "insufficient-available-capital" ->
                        throw new InsufficientAvailableCapitalException(
                                CYCLE_ID,
                                CapitalKind.MONEY,
                                new BigDecimal("10.0000"),
                                new BigDecimal("25.0000")
                        );
                case "over-allocation-not-allowed" ->
                        throw new OverAllocationNotAllowedException(
                                CYCLE_ID,
                                CapitalKind.MONEY,
                                new BigDecimal("100.00"),
                                new BigDecimal("125.00")
                        );
                case "over-allocation-confirmation" ->
                        throw new OverAllocationConfirmationRequiredException(
                                CYCLE_ID,
                                CapitalKind.MONEY,
                                new BigDecimal("100.00"),
                                new BigDecimal("125.00")
                        );
                case "ownership-mismatch" -> throw new CapitalCycleOwnershipException(CYCLE_ID, OWNER_ID);
                case "unknown-runtime" ->
                        throw new RuntimeException(
                                "select * from capital_cycles failed with database-password in stack trace"
                        );
                case "data-integrity" ->
                        throw new CapitalAllocationDataIntegrityException(CYCLE_ID, CapitalKind.TIME);
                default -> throw new IllegalArgumentException("Unknown scenario " + scenario);
            }
        }

        @GetMapping("/already-initialized/{capitalKind}")
        void throwAlreadyInitialized(@PathVariable CapitalKind capitalKind) {
            throw new CapitalAlreadyInitializedException(CYCLE_ID, capitalKind);
        }

        @PostMapping("/validation")
        void validateRequest(@Valid @RequestBody TestRequest request) {
        }
    }

    record TestRequest(@NotBlank String name) {
    }
}
