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
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanDisbursementDetails;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProduct;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProductRelatedDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class WorkingCapitalLoanDataValidatorDiscountTest {

    private static final LocalDate BUSINESS_DATE = LocalDate.of(2026, 3, 15);

    private WorkingCapitalLoanDataValidator validator;

    @BeforeEach
    public void setUp() {
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, BUSINESS_DATE)));
        validator = new WorkingCapitalLoanDataValidator(new FromJsonHelper(), null, null, null, null, null, null);
    }

    @AfterEach
    public void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    public void discountValidationUsesAnyActualDateForDisbursedAndTheEarliestActualDateForTheTransactionDate() {
        final WorkingCapitalLoan loan = configuredLoan();

        loan.getDisbursementDetails().add(disbursementDetail(null));
        loan.getDisbursementDetails().add(disbursementDetail(BUSINESS_DATE.plusDays(1)));
        loan.getDisbursementDetails().add(disbursementDetail(BUSINESS_DATE));

        assertDoesNotThrow(() -> validator.validateDiscountTransaction(loan, "{}", BigDecimal.ZERO, null));
    }

    @Test
    public void discountValidationRejectsLoanWithoutAnActualDisbursement() {
        final WorkingCapitalLoan loan = configuredLoan();
        loan.getDisbursementDetails().add(disbursementDetail(null));

        final PlatformApiDataValidationException exception = assertThrows(PlatformApiDataValidationException.class,
                () -> validator.validateDiscountTransaction(loan, "{}", BigDecimal.ZERO, null));

        assertThat(exception.getErrors()).anyMatch(error -> error.getUserMessageGlobalisationCode().contains("loan.not.disbursed"));
    }

    @Test
    public void discountValidationUsesTheEarliestActualDateWhenALaterDateMatchesBusinessDate() {
        final WorkingCapitalLoan loan = configuredLoan();
        loan.getDisbursementDetails().add(disbursementDetail(BUSINESS_DATE));
        loan.getDisbursementDetails().add(disbursementDetail(BUSINESS_DATE.minusDays(1)));

        final PlatformApiDataValidationException exception = assertThrows(PlatformApiDataValidationException.class,
                () -> validator.validateDiscountTransaction(loan, "{}", BigDecimal.ZERO, null));

        assertThat(exception.getErrors())
                .anyMatch(error -> error.getUserMessageGlobalisationCode().contains("transaction.date.must.be.equal.disbursement.date"));
    }

    @Test
    public void firstActualDisbursementUsesDetailIdToBreakSameDateTiesAndAmountUsesTheSelectedDetail() {
        final WorkingCapitalLoan loan = configuredLoan();
        final WorkingCapitalLoanDisbursementDetails higherId = disbursementDetail(BUSINESS_DATE);
        higherId.setId(20L);
        higherId.setActualAmount(new BigDecimal("275.00"));
        final WorkingCapitalLoanDisbursementDetails lowerId = disbursementDetail(BUSINESS_DATE);
        lowerId.setId(10L);
        lowerId.setActualAmount(new BigDecimal("125.00"));
        loan.getDisbursementDetails().add(higherId);
        loan.getDisbursementDetails().add(lowerId);

        assertThat(loan.getFirstActualDisbursement()).isSameAs(lowerId);
        assertThat(loan.getFirstActualDisbursementAmount()).isEqualByComparingTo("125.00");
    }

    private static WorkingCapitalLoan configuredLoan() {
        final WorkingCapitalLoan loan = new WorkingCapitalLoan();
        loan.setLoanProduct(mock(WorkingCapitalLoanProduct.class));
        final WorkingCapitalLoanProductRelatedDetails relatedDetails = new WorkingCapitalLoanProductRelatedDetails();
        relatedDetails.setDiscountApproved(BigDecimal.ZERO);
        loan.setLoanProductRelatedDetails(relatedDetails);
        return loan;
    }

    private static WorkingCapitalLoanDisbursementDetails disbursementDetail(final LocalDate actualDisbursementDate) {
        final WorkingCapitalLoanDisbursementDetails detail = new WorkingCapitalLoanDisbursementDetails();
        detail.setActualDisbursementDate(actualDisbursementDate);
        return detail;
    }
}
