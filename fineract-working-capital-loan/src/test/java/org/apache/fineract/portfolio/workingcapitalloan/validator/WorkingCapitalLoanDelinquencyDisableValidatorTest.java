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
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyAction;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyBucket;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanDelinquencyAction;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanDelinquencyActionRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanDelinquencyRangeScheduleRepository;
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
 * Validation tests for the disable/enable delinquency actions routed through the single {@code validateAndParse} entry
 * point (mirroring the breach action validator).
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WorkingCapitalLoanDelinquencyDisableValidatorTest {

    private static final Long LOAN_ID = 1L;

    @Mock
    private WorkingCapitalLoanDelinquencyRangeScheduleRepository rangeScheduleRepository;
    @Mock
    private WorkingCapitalLoanDelinquencyActionRepository actionRepository;
    @Mock
    private WorkingCapitalLoan loan;
    @Mock
    private WorkingCapitalLoanProductRelatedDetails productRelatedDetails;
    @Mock
    private DelinquencyBucket delinquencyBucket;

    private WorkingCapitalLoanDelinquencyActionParseAndValidator validator;
    private LocalDate today;

    @BeforeEach
    void setUp() {
        validator = new WorkingCapitalLoanDelinquencyActionParseAndValidator(new FromJsonHelper(), rangeScheduleRepository,
                actionRepository);
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        final HashMap<BusinessDateType, LocalDate> businessDates = new HashMap<>();
        today = LocalDate.now(ZoneId.systemDefault());
        businessDates.put(BusinessDateType.BUSINESS_DATE, today);
        businessDates.put(BusinessDateType.COB_DATE, today.minusDays(1));
        ThreadLocalContextUtil.setBusinessDates(businessDates);
        when(loan.getId()).thenReturn(LOAN_ID);
        when(loan.getLoanStatus()).thenReturn(LoanStatus.ACTIVE);
        when(loan.getLoanProductRelatedDetails()).thenReturn(productRelatedDetails);
        when(productRelatedDetails.getDelinquencyBucket()).thenReturn(delinquencyBucket);
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    void disableAppliesAsOfCurrentBusinessDateWhenNoneActive() {
        when(actionRepository.isDelinquencyDisabledAsOf(eq(LOAN_ID), any())).thenReturn(false);

        final WorkingCapitalLoanDelinquencyAction result = validator.validateAndParse(command("disable", today, null), loan, List.of());

        assertThat(result.getAction()).isEqualTo(DelinquencyAction.DISABLE);
        assertThat(result.getStartDate()).isEqualTo(DateUtils.getBusinessLocalDate());
        assertThat(result.getEndDate()).isNull();
    }

    @Test
    void disableIsRejectedWhenAlreadyDisabled() {
        when(actionRepository.isDelinquencyDisabledAsOf(eq(LOAN_ID), any())).thenReturn(true);

        assertThatThrownBy(() -> validator.validateAndParse(command("disable", today, null), loan, List.of()))
                .isInstanceOf(PlatformApiDataValidationException.class).hasMessageContaining("Validation errors exist");
    }

    @Test
    void disableWithEndDateIsRejected() {
        when(actionRepository.isDelinquencyDisabledAsOf(eq(LOAN_ID), any())).thenReturn(false);

        assertThatThrownBy(() -> validator.validateAndParse(command("disable", today, today.plusDays(3)), loan, List.of()))
                .isInstanceOf(PlatformApiDataValidationException.class);
    }

    @Test
    void backdatedDisableIsRejected() {
        when(actionRepository.isDelinquencyDisabledAsOf(eq(LOAN_ID), any())).thenReturn(false);

        assertThatThrownBy(() -> validator.validateAndParse(command("disable", today.minusDays(1), null), loan, List.of()))
                .isInstanceOf(PlatformApiDataValidationException.class);
    }

    @Test
    void enableReturnsNewEnableAction() {
        when(actionRepository.isDelinquencyDisabledAsOf(eq(LOAN_ID), any())).thenReturn(true);
        final WorkingCapitalLoanDelinquencyAction active = activeDisable();

        final WorkingCapitalLoanDelinquencyAction result = validator.validateAndParse(command("enable", today, null), loan, List.of(active));

        // The enable is persisted as its own ENABLE row (mirroring breach), not by recycling the active disable.
        assertThat(result).isNotSameAs(active);
        assertThat(result.getAction()).isEqualTo(DelinquencyAction.ENABLE);
        assertThat(result.getStartDate()).isEqualTo(DateUtils.getBusinessLocalDate());
        assertThat(result.getEndDate()).isNull();
    }

    @Test
    void enableIsRejectedWhenNoActiveDisable() {
        when(actionRepository.isDelinquencyDisabledAsOf(eq(LOAN_ID), any())).thenReturn(false);

        assertThatThrownBy(() -> validator.validateAndParse(command("enable", today, null), loan, List.of()))
                .isInstanceOf(PlatformApiDataValidationException.class);
    }

    private WorkingCapitalLoanDelinquencyAction activeDisable() {
        final WorkingCapitalLoanDelinquencyAction action = new WorkingCapitalLoanDelinquencyAction();
        action.setAction(DelinquencyAction.DISABLE);
        action.setStartDate(today.minusDays(2));
        action.setEndDate(null);
        return action;
    }

    private JsonCommand command(final String action, final LocalDate startDate, final LocalDate endDate) {
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        final StringBuilder json = new StringBuilder("{\"action\":\"").append(action)
                .append("\",\"dateFormat\":\"yyyy-MM-dd\",\"locale\":\"en\"");
        if (startDate != null) {
            json.append(",\"startDate\":\"").append(startDate.format(formatter)).append("\"");
        }
        if (endDate != null) {
            json.append(",\"endDate\":\"").append(endDate.format(formatter)).append("\"");
        }
        json.append("}");
        final FromJsonHelper helper = new FromJsonHelper();
        final JsonElement parsed = helper.parse(json.toString());
        // from(json, parsed, helper, entityName, resourceId, subresourceId, groupId, clientId, loanId, savingsId,
        // transactionId, url, productId, creditBureauId, organisationCreditBureauId, jobName, loanExternalId)
        return JsonCommand.from(json.toString(), parsed, helper, null, null, null, null, null, null, null, null, null, null, null, null,
                null, null);
    }
}
