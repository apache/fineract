/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.portfolio.workingcapitalloan.validator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.google.gson.JsonElement;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBreachAction;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBreachActionType;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBreachSchedule;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanBreachActionRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanBreachScheduleRepository;
import org.apache.fineract.portfolio.workingcapitalloan.service.WorkingCapitalLoanActiveBreachResetResolver;
import org.apache.fineract.portfolio.workingcapitalloanbreach.domain.WorkingCapitalBreach;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProductRelatedDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * A breach pause must never be backdated behind a reset: the reset settles the period it lands on and restarts the
 * evaluation from its own date, while a pause re-dates the periods it precedes.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WorkingCapitalLoanBreachPauseResetValidatorTest {

    private static final Long LOAN_ID = 1L;

    @Mock
    private WorkingCapitalLoanBreachScheduleRepository breachScheduleRepository;
    @Mock
    private WorkingCapitalLoanBreachActionRepository breachActionRepository;
    @Mock
    private WorkingCapitalLoan loan;
    @Mock
    private WorkingCapitalLoanProductRelatedDetails productRelatedDetails;
    @Mock
    private WorkingCapitalBreach breachConfiguration;

    private WorkingCapitalLoanBreachActionParseAndValidator validator;
    private LocalDate today;
    private LocalDate scheduleStart;

    @BeforeEach
    void setUp() {
        validator = new WorkingCapitalLoanBreachActionParseAndValidator(new FromJsonHelper(), breachScheduleRepository,
                new WorkingCapitalLoanActiveBreachResetResolver(breachActionRepository), breachActionRepository);
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        today = LocalDate.now(ZoneId.systemDefault());
        scheduleStart = today.minusDays(60);
        final HashMap<BusinessDateType, LocalDate> businessDates = new HashMap<>();
        businessDates.put(BusinessDateType.BUSINESS_DATE, today);
        businessDates.put(BusinessDateType.COB_DATE, today.minusDays(1));
        ThreadLocalContextUtil.setBusinessDates(businessDates);

        when(loan.getId()).thenReturn(LOAN_ID);
        when(loan.isOpen()).thenReturn(true);
        when(loan.getLoanProductRelatedDetails()).thenReturn(productRelatedDetails);
        when(productRelatedDetails.getBreach()).thenReturn(breachConfiguration);
        when(breachActionRepository.isBreachDisabledAsOf(eq(LOAN_ID), any())).thenReturn(false);

        final WorkingCapitalLoanBreachSchedule firstPeriod = new WorkingCapitalLoanBreachSchedule();
        firstPeriod.setFromDate(scheduleStart);
        when(breachScheduleRepository.findTopByLoanIdOrderByPeriodNumberAsc(LOAN_ID)).thenReturn(Optional.of(firstPeriod));
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    void pauseStartingBeforeTheResetIsRejected() {
        final LocalDate resetDate = today.minusDays(10);

        assertThatThrownBy(() -> validate(resetDate.minusDays(1), resetDate.plusDays(2), List.of(reset(resetDate))))
                .isInstanceOf(PlatformApiDataValidationException.class).hasMessageContaining("Validation errors exist");
    }

    @Test
    void pauseStartingOnTheResetDateIsAccepted() {
        final LocalDate resetDate = today.minusDays(10);

        final WorkingCapitalLoanBreachAction pause = validate(resetDate, resetDate.plusDays(2), List.of(reset(resetDate)));

        assertThat(pause.getAction()).isEqualTo(WorkingCapitalLoanBreachActionType.PAUSE);
        assertThat(pause.getStartDate()).isEqualTo(resetDate);
    }

    @Test
    void pauseStartingAfterTheResetIsAccepted() {
        final LocalDate resetDate = today.minusDays(10);

        assertThatCode(() -> validate(resetDate.plusDays(1), resetDate.plusDays(3), List.of(reset(resetDate)))).doesNotThrowAnyException();
    }

    @Test
    void backdatedPauseIsAcceptedWhenThereIsNoReset() {
        assertThatCode(() -> validate(today.minusDays(20), today.minusDays(18), List.of())).doesNotThrowAnyException();
    }

    @Test
    void theLatestActiveResetIsTheBoundary() {
        final LocalDate firstReset = today.minusDays(20);
        final LocalDate latestReset = today.minusDays(5);

        assertThatThrownBy(() -> validate(firstReset.plusDays(1), latestReset.plusDays(2), List.of(reset(firstReset), reset(latestReset))))
                .isInstanceOf(PlatformApiDataValidationException.class);
    }

    @Test
    void anUndoneResetNoLongerBlocksABackdatedPause() {
        final LocalDate resetDate = today.minusDays(10);

        assertThatCode(
                () -> validate(resetDate.minusDays(1), resetDate.plusDays(2), List.of(reset(resetDate), undoReset(today.minusDays(3)))))
                .doesNotThrowAnyException();
    }

    @Test
    void theResetUnderTheUndoneOneStillBlocksABackdatedPause() {
        final LocalDate firstReset = today.minusDays(20);
        final LocalDate undoneReset = today.minusDays(5);

        assertThatThrownBy(() -> validate(firstReset.minusDays(1), firstReset.plusDays(2),
                List.of(reset(firstReset), reset(undoneReset), undoReset(today.minusDays(2)))))
                .isInstanceOf(PlatformApiDataValidationException.class);
    }

    private WorkingCapitalLoanBreachAction validate(final LocalDate startDate, final LocalDate endDate,
            final List<WorkingCapitalLoanBreachAction> existing) {
        return validator.validateAndParse(command(startDate, endDate), loan, existing);
    }

    private WorkingCapitalLoanBreachAction reset(final LocalDate resetDate) {
        return action(WorkingCapitalLoanBreachActionType.RESET, resetDate);
    }

    private WorkingCapitalLoanBreachAction undoReset(final LocalDate undoDate) {
        return action(WorkingCapitalLoanBreachActionType.UNDO_RESET, undoDate);
    }

    private WorkingCapitalLoanBreachAction action(final WorkingCapitalLoanBreachActionType type, final LocalDate startDate) {
        final WorkingCapitalLoanBreachAction action = new WorkingCapitalLoanBreachAction();
        action.setAction(type);
        action.setStartDate(startDate);
        return action;
    }

    private JsonCommand command(final LocalDate startDate, final LocalDate endDate) {
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        final String json = "{\"action\":\"pause\",\"dateFormat\":\"yyyy-MM-dd\",\"locale\":\"en\",\"startDate\":\""
                + startDate.format(formatter) + "\",\"endDate\":\"" + endDate.format(formatter) + "\"}";
        final FromJsonHelper jsonHelper = new FromJsonHelper();
        final JsonElement parsed = jsonHelper.parse(json);
        return JsonCommand.from(json, parsed, jsonHelper, null, null, null, null, null, LOAN_ID, null, null, null, null, null, null, null,
                null);
    }
}
