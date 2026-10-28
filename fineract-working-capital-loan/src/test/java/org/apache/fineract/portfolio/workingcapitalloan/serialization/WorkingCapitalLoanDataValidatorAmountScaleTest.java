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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.workingcapitalloan.WorkingCapitalLoanConstants;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransaction;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProduct;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * An amount that books money on a Working Capital loan may carry no more decimals than the loan's currency, as the
 * product and application amounts already may not. A finer amount used to be stored and posted unrounded.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WorkingCapitalLoanDataValidatorAmountScaleTest {

    private static final LocalDate BUSINESS_DATE = LocalDate.of(2026, 3, 15);
    private static final String SCALE_CODE = "scale.is.greater.than.2";

    private WorkingCapitalLoanDataValidator validator;

    @Mock
    private WorkingCapitalLoan loan;
    @Mock
    private WorkingCapitalLoanProduct product;
    @Mock
    private WorkingCapitalLoanTransaction transaction;

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "UTC", null));
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, BUSINESS_DATE)));

        validator = new WorkingCapitalLoanDataValidator(new FromJsonHelper(), null, null, null, null, null, null);

        lenient().when(loan.getCurrency()).thenReturn(new MonetaryCurrency("USD", 2, null));
        lenient().when(loan.getLoanStatus()).thenReturn(LoanStatus.ACTIVE);
        lenient().when(loan.isOpen()).thenReturn(true);
        lenient().when(loan.getOfficeId()).thenReturn(null);
        lenient().when(loan.getLoanProduct()).thenReturn(product);
        lenient().when(loan.getFirstActualDisbursementDate()).thenReturn(BUSINESS_DATE.minusDays(10));
        lenient().when(transaction.getTransactionAmount()).thenReturn(new BigDecimal("100"));
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    void repaymentRefusesAnAmountFinerThanTheCurrency() {
        assertScaleRefused(WorkingCapitalLoanConstants.transactionAmountParamName,
                () -> validator.validateRepayment(transactionJson("10.005").toString(), loan, LoanTransactionType.REPAYMENT));
    }

    @Test
    void repaymentAcceptsAnAmountInTheCurrencyDigits() {
        validator.validateRepayment(transactionJson("10.01").toString(), loan, LoanTransactionType.REPAYMENT);
    }

    @Test
    void creditBalanceRefundRefusesAnAmountFinerThanTheCurrency() {
        assertScaleRefused(WorkingCapitalLoanConstants.transactionAmountParamName,
                () -> validator.validateCreditBalanceRefund(transactionJson("10.005").toString(), loan));
    }

    @Test
    void disbursementRefusesAnAmountFinerThanTheCurrency() {
        final JsonObject json = disbursementJson("100.005");
        assertScaleRefused(WorkingCapitalLoanConstants.transactionAmountParamName,
                () -> validator.validateDisbursement(json.toString(), loan));
    }

    @Test
    void disbursementRefusesADiscountFinerThanTheCurrency() {
        final JsonObject json = disbursementJson("100");
        json.addProperty(WorkingCapitalLoanConstants.discountAmountParamName, new BigDecimal("1.005"));
        assertScaleRefused(WorkingCapitalLoanConstants.discountAmountParamName,
                () -> validator.validateDisbursement(json.toString(), loan));
    }

    @Test
    void discountFeeRefusesAnAmountFinerThanTheCurrency() {
        assertScaleRefused(WorkingCapitalLoanConstants.discountAmountParamName,
                () -> validator.validateDiscountTransaction(loan, "{}", new BigDecimal("1.005"), null));
    }

    @Test
    void discountFeeAdjustmentRefusesAnAmountFinerThanTheCurrency() {
        assertScaleRefused(WorkingCapitalLoanConstants.transactionAmountParamName,
                () -> validator.validateDiscountAdjustmentTransaction(loan, "{}", new BigDecimal("1.005"), null, BigDecimal.TEN, null));
    }

    @Test
    void adjustTransactionRefusesAnAmountFinerThanTheCurrency() {
        assertScaleRefused(WorkingCapitalLoanConstants.transactionAmountParamName,
                () -> validator.validateAdjustTransaction(command(transactionJson("10.005")), loan, transaction));
    }

    private static void assertScaleRefused(final String parameterName, final Executable validation) {
        final PlatformApiDataValidationException exception = assertThrows(PlatformApiDataValidationException.class, validation);
        assertThat(exception.getErrors()).anyMatch(
                error -> parameterName.equals(error.getParameterName()) && error.getUserMessageGlobalisationCode().endsWith(SCALE_CODE));
    }

    private static JsonObject transactionJson(final String amount) {
        final JsonObject json = new JsonObject();
        json.addProperty("locale", "en");
        json.addProperty("dateFormat", "yyyy-MM-dd");
        json.addProperty(WorkingCapitalLoanConstants.transactionDateParamName, BUSINESS_DATE.toString());
        json.addProperty(WorkingCapitalLoanConstants.transactionAmountParamName, new BigDecimal(amount));
        return json;
    }

    private static JsonObject disbursementJson(final String amount) {
        final JsonObject json = new JsonObject();
        json.addProperty("locale", "en");
        json.addProperty("dateFormat", "yyyy-MM-dd");
        json.addProperty(WorkingCapitalLoanConstants.actualDisbursementDateParamName, BUSINESS_DATE.toString());
        json.addProperty(WorkingCapitalLoanConstants.transactionAmountParamName, new BigDecimal(amount));
        return json;
    }

    private static JsonCommand command(final JsonObject json) {
        final FromJsonHelper helper = new FromJsonHelper();
        final JsonElement parsed = helper.parse(json.toString());
        return JsonCommand.from(json.toString(), parsed, helper, null, null, null, null, null, null, null, null, null, null, null, null,
                null, null);
    }
}
