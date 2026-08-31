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
package org.apache.fineract.cob.workingcapitalloan.businessstep;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.service.WorkingCapitalLoanDiscountFeeAmortizationService;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class DiscountFeeAmortizationBusinessStep extends WorkingCapitalLoanCOBBusinessStep {

    private final WorkingCapitalLoanDiscountFeeAmortizationService discountFeeAmortizationService;

    @Override
    public WorkingCapitalLoan execute(final WorkingCapitalLoan loan) {
        if (loan.isClosed()) {
            log.debug("Skipping discount fee amortization for WC loan {} - loan is closed", loan.getId());
            return loan;
        }
        if (loan.getLoanProductRelatedDetails() == null) {
            log.debug("Skipping discount fee amortization for WC loan {} - no loan product details", loan.getId());
            return loan;
        }
        if (loan.isChargedOff()) {
            log.debug("Skipping discount fee amortization for WC loan {} - loan is charged off", loan.getId());
            return loan;
        }
        // Run when there is still a discount to amortize, OR when income was previously recognized and now needs to be
        // reconciled down (e.g. a full discount adjustment reduced the discount to zero). Otherwise there is nothing to
        // do.
        final BigDecimal discount = loan.getLoanProductRelatedDetails().getDiscount();
        final BigDecimal recognizedIncome = loan.getBalance() == null ? null : loan.getBalance().getRealizedIncomeFromDiscountFee();
        if (!MathUtil.isGreaterThanZero(discount) && !MathUtil.isGreaterThanZero(recognizedIncome)) {
            log.debug("Skipping discount fee amortization for WC loan {} - no discount fee and no recognized income to reconcile",
                    loan.getId());
            return loan;
        }

        final LocalDate businessDate = DateUtils.getBusinessLocalDate();

        discountFeeAmortizationService.processDiscountFeeAmortization(loan, businessDate);

        return loan;
    }

    @Override
    public String getEnumStyledName() {
        return "WC_DISCOUNT_FEE_AMORTIZATION";
    }

    @Override
    public String getHumanReadableName() {
        return "WC Discount Fee Amortization";
    }
}
