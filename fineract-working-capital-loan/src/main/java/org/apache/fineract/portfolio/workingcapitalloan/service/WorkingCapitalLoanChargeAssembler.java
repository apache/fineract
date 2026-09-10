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
package org.apache.fineract.portfolio.workingcapitalloan.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.organisation.monetary.exception.InvalidCurrencyException;
import org.apache.fineract.portfolio.charge.domain.Charge;
import org.apache.fineract.portfolio.charge.domain.ChargeCalculationType;
import org.apache.fineract.portfolio.charge.domain.ChargeRepositoryWrapper;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;
import org.apache.fineract.portfolio.charge.exception.ChargeCannotBeAppliedToException;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanCharge;
import org.apache.fineract.portfolio.workingcapitalloan.serialization.WorkingCapitalLoanChargeDataValidator;
import org.springframework.stereotype.Component;

/**
 * Turns a charge reference from a request into a {@link WorkingCapitalLoanCharge} for a given loan, applying the rules
 * that depend on the charge definition and on the loan state. Shared by the account charge endpoint and by the loan
 * application (submit / modify), so a charge added either way behaves the same.
 */
@Component
@RequiredArgsConstructor
public class WorkingCapitalLoanChargeAssembler {

    private final ChargeRepositoryWrapper chargeRepository;
    private final WorkingCapitalLoanChargeDataValidator loanChargeDataValidator;
    private final WorkingCapitalLoanChargeAmountResolver amountResolver;

    /**
     * @param amount
     *            the requested amount, or the rate for a percentage charge; may be null for a disbursement charge, in
     *            which case the definition's own amount is used
     * @param dueDate
     *            mandatory for a specified-due-date charge, forbidden for a disbursement charge
     */
    public WorkingCapitalLoanCharge assemble(final WorkingCapitalLoan loan, final Long chargeId, final BigDecimal amount,
            final LocalDate dueDate, final ExternalId externalId) {
        final LocalDate businessDate = ThreadLocalContextUtil.getBusinessDate();
        final Charge chargeDefinition = chargeRepository.findOneWithNotFoundDetection(chargeId);

        // The time-type guard alone let a term-loan charge of an accepted time type through; check the domain
        // explicitly, and the currency, like the product catalogue already does.
        if (!chargeDefinition.isWorkingCapitalLoanCharge()) {
            throw new ChargeCannotBeAppliedToException("working.capital.loan",
                    "Charge with identifier " + chargeId + " cannot be applied to Working Capital Loan account.", chargeId);
        }
        if (loan.getCurrencyCode() != null && !loan.getCurrencyCode().equals(chargeDefinition.getCurrencyCode())) {
            throw new InvalidCurrencyException("charge", "attach.to.working.capital.loan",
                    "Charge and Working Capital Loan must have the same currency.");
        }

        final ChargeTimeType chargeTimeType = ChargeTimeType.fromInt(chargeDefinition.getChargeTimeType());
        loanChargeDataValidator.validateCreateLoanChargeAgainstLoan(loan, chargeTimeType, amount, dueDate, businessDate);

        if (chargeTimeType.isTimeOfDisbursement()) {
            // The request amount overrides the definition's; for a percentage charge both are the rate. The money
            // amount is provisional against the current principal and is re-resolved at disbursement.
            final ChargeCalculationType calculationType = ChargeCalculationType.fromInt(chargeDefinition.getChargeCalculation());
            final BigDecimal amountOrRate = amount != null ? amount : chargeDefinition.getAmount();
            final BigDecimal percentage = calculationType.isPercentageBased() ? amountOrRate : null;
            final BigDecimal resolvedAmount = amountResolver.resolve(calculationType, amountOrRate,
                    loan.getLoanProductRelatedDetails().getPrincipal());
            return WorkingCapitalLoanCharge.build(loan, externalId, chargeDefinition, resolvedAmount, percentage, null, businessDate);
        }
        return WorkingCapitalLoanCharge.build(loan, externalId, chargeDefinition, amount, dueDate, businessDate);
    }

    /**
     * Changes the amount (or rate) of a disbursement charge that is already on a not-yet-disbursed loan, keeping the
     * provisional money amount in step with the current principal.
     */
    public void updateAmount(final WorkingCapitalLoan loan, final WorkingCapitalLoanCharge charge, final BigDecimal amountOrRate) {
        if (charge.isPercentageBased()) {
            charge.setPercentage(amountOrRate);
            charge.setAmount(amountResolver.resolve(charge.getChargeCalculationType(), amountOrRate,
                    loan.getLoanProductRelatedDetails().getPrincipal()));
        } else {
            charge.setAmount(amountOrRate);
        }
    }
}
