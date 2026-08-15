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
package org.apache.fineract.portfolio.workingcapitalloan.serialization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.google.gson.JsonObject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.exception.UnsupportedParameterException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.workingcapitalloan.WorkingCapitalLoanConstants;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanPeriodPaymentRateChangeRepository;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProduct;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProductMinMaxConstraints;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProductRelatedDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WorkingCapitalLoanDataValidatorUpdateRateTest {

    private WorkingCapitalLoanDataValidator validator;

    @Mock
    private WorkingCapitalLoan loan;
    @Mock
    private WorkingCapitalLoanProductRelatedDetails relatedDetails;
    @Mock
    private WorkingCapitalLoanProduct product;
    @Mock
    private WorkingCapitalLoanProductMinMaxConstraints minMaxConstraints;
    @Mock
    private WorkingCapitalLoanPeriodPaymentRateChangeRepository rateChangeRepository;

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        ThreadLocalContextUtil
                .setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, LocalDate.now(ZoneId.systemDefault()))));

        final FromJsonHelper fromApiJsonHelper = new FromJsonHelper();
        validator = new WorkingCapitalLoanDataValidator(fromApiJsonHelper, null, null, null, null, null, rateChangeRepository);

        // Default: active loan, current rate = 10, product min = 1, max = 95. With no rate changes on record the loan's
        // own rate is the one in effect, so "10" is still the rate a request has to differ from.
        lenient().when(rateChangeRepository.findByWorkingCapitalLoanIdAndReversedFalse(any())).thenReturn(List.of());
        lenient().when(loan.getLoanStatus()).thenReturn(LoanStatus.ACTIVE);
        lenient().when(loan.getLoanProductRelatedDetails()).thenReturn(relatedDetails);
        lenient().when(relatedDetails.getPeriodPaymentRate()).thenReturn(BigDecimal.TEN);
        lenient().when(loan.getLoanProduct()).thenReturn(product);
        lenient().when(product.getMinMaxConstraints()).thenReturn(minMaxConstraints);
        lenient().when(minMaxConstraints.getMinPeriodPaymentRate()).thenReturn(BigDecimal.ONE);
        lenient().when(minMaxConstraints.getMaxPeriodPaymentRate()).thenReturn(new BigDecimal("95"));
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    void shouldThrowInvalidJsonWhenJsonIsBlank() {
        assertThrows(InvalidJsonException.class, () -> validator.validateUpdatePeriodPaymentRate("", loan));
    }

    @Test
    void shouldThrowInvalidJsonWhenJsonIsNull() {
        assertThrows(InvalidJsonException.class, () -> validator.validateUpdatePeriodPaymentRate(null, loan));
    }

    @Test
    void shouldRejectNonActiveLoan() {
        when(loan.getLoanStatus()).thenReturn(LoanStatus.SUBMITTED_AND_PENDING_APPROVAL);
        PlatformApiDataValidationException ex = assertThrows(PlatformApiDataValidationException.class,
                () -> validator.validateUpdatePeriodPaymentRate(validJson("20"), loan));
        assertThat(ex.getErrors()).anyMatch(e -> e.getUserMessageGlobalisationCode().contains("rate.change.not.allowed.for.non.active.loan"));
    }

    @Test
    void shouldRejectMissingRate() {
        final JsonObject json = new JsonObject();
        json.addProperty("locale", "en");
        PlatformApiDataValidationException ex = assertThrows(PlatformApiDataValidationException.class,
                () -> validator.validateUpdatePeriodPaymentRate(json.toString(), loan));
        assertThat(ex.getErrors()).anyMatch(e -> e.getParameterName().equals(WorkingCapitalLoanConstants.periodPaymentRateParamName));
    }

    @Test
    void shouldRejectNegativeRate() {
        PlatformApiDataValidationException ex = assertThrows(PlatformApiDataValidationException.class,
                () -> validator.validateUpdatePeriodPaymentRate(validJson("-5"), loan));
        assertThat(ex.getErrors()).anyMatch(e -> e.getParameterName().equals(WorkingCapitalLoanConstants.periodPaymentRateParamName));
    }

    @Test
    void shouldRejectZeroRate() {
        PlatformApiDataValidationException ex = assertThrows(PlatformApiDataValidationException.class,
                () -> validator.validateUpdatePeriodPaymentRate(validJson("0"), loan));
        assertThat(ex.getErrors()).anyMatch(e -> e.getParameterName().equals(WorkingCapitalLoanConstants.periodPaymentRateParamName));
    }

    @Test
    void shouldRejectSameAsCurrentRate() {
        PlatformApiDataValidationException ex = assertThrows(PlatformApiDataValidationException.class,
                () -> validator.validateUpdatePeriodPaymentRate(validJson("10"), loan));
        assertThat(ex.getErrors()).anyMatch(e -> e.getUserMessageGlobalisationCode().contains("rate.must.differ.from.current"));
    }

    @Test
    void shouldRejectRateBelowProductMinimum() {
        PlatformApiDataValidationException ex = assertThrows(PlatformApiDataValidationException.class,
                () -> validator.validateUpdatePeriodPaymentRate(validJson("0.5"), loan));
        assertThat(ex.getErrors()).anyMatch(e -> e.getUserMessageGlobalisationCode().contains("rate.below.product.minimum"));
    }

    @Test
    void shouldRejectRateAboveProductMaximum() {
        PlatformApiDataValidationException ex = assertThrows(PlatformApiDataValidationException.class,
                () -> validator.validateUpdatePeriodPaymentRate(validJson("96"), loan));
        assertThat(ex.getErrors()).anyMatch(e -> e.getUserMessageGlobalisationCode().contains("rate.exceeds.product.maximum"));
    }

    @Test
    void shouldRejectNoteTooLong() {
        final JsonObject json = new JsonObject();
        addCommonParameters(json);
        json.addProperty(WorkingCapitalLoanConstants.periodPaymentRateParamName, 20);
        json.addProperty(WorkingCapitalLoanConstants.noteParamName, "x".repeat(1001));
        PlatformApiDataValidationException ex = assertThrows(PlatformApiDataValidationException.class,
                () -> validator.validateUpdatePeriodPaymentRate(json.toString(), loan));
        assertThat(ex.getErrors()).anyMatch(e -> e.getParameterName().equals(WorkingCapitalLoanConstants.noteParamName));
    }

    @Test
    void shouldAcceptValidNote() {
        final JsonObject json = new JsonObject();
        addCommonParameters(json);
        json.addProperty(WorkingCapitalLoanConstants.periodPaymentRateParamName, 20);
        json.addProperty(WorkingCapitalLoanConstants.noteParamName, "Rate adjusted per client request");
        assertDoesNotThrow(() -> validator.validateUpdatePeriodPaymentRate(json.toString(), loan));
    }

    @Test
    void shouldRejectUnsupportedParameter() {
        final JsonObject json = new JsonObject();
        json.addProperty("locale", "en");
        json.addProperty(WorkingCapitalLoanConstants.periodPaymentRateParamName, 20);
        json.addProperty("unsupportedParam", "value");
        assertThrows(UnsupportedParameterException.class, () -> validator.validateUpdatePeriodPaymentRate(json.toString(), loan));
    }

    @Test
    void shouldAcceptRateAtMinBoundary() {
        assertDoesNotThrow(() -> validator.validateUpdatePeriodPaymentRate(validJson("1"), loan));
    }

    @Test
    void shouldAcceptRateAtMaxBoundary() {
        assertDoesNotThrow(() -> validator.validateUpdatePeriodPaymentRate(validJson("95"), loan));
    }

    @Test
    void shouldAcceptRateWhenNoMinMaxConstraints() {
        when(product.getMinMaxConstraints()).thenReturn(null);
        assertDoesNotThrow(() -> validator.validateUpdatePeriodPaymentRate(validJson("50"), loan));
    }

    @Test
    void shouldCollectMultipleErrors() {
        // Non-active loan + missing rate = at least 2 errors
        when(loan.getLoanStatus()).thenReturn(LoanStatus.APPROVED);
        final JsonObject json = new JsonObject();
        json.addProperty("locale", "en");
        PlatformApiDataValidationException ex = assertThrows(PlatformApiDataValidationException.class,
                () -> validator.validateUpdatePeriodPaymentRate(json.toString(), loan));
        assertThat(ex.getErrors().size()).isGreaterThanOrEqualTo(2);
    }

    @Test
    void shouldPassWithValidData() {
        assertDoesNotThrow(() -> validator.validateUpdatePeriodPaymentRate(validJson("20"), loan));
    }

    private void addCommonParameters(final JsonObject json) {
        json.addProperty("locale", "en");
        json.addProperty(WorkingCapitalLoanConstants.dateFormatParameterName, "dd MMMM yyyy");
        json.addProperty(WorkingCapitalLoanConstants.effectiveDateParamName, "01 January 2026");
    }

    private String validJson(final String rate) {
        final JsonObject json = new JsonObject();
        addCommonParameters(json);
        json.addProperty(WorkingCapitalLoanConstants.periodPaymentRateParamName, new BigDecimal(rate));
        return json.toString();
    }
}
