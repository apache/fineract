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
package org.apache.fineract.portfolio.loanaccount.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.loanaccount.api.LoanApiConstants;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.paymentdetail.domain.PaymentDetail;
import org.apache.fineract.portfolio.paymentdetail.service.PaymentDetailWritePlatformService;

@RequiredArgsConstructor
public class LoanTransactionAssembler {

    private final ExternalIdFactory externalIdFactory;
    private final PaymentDetailWritePlatformService paymentDetailWritePlatformService;

    LoanTransaction assembleTransactionAndCalculateChanges(Loan loan, JsonCommand command, Map<String, Object> changes) {
        final String noteText = command.stringValueOfParameterNamed("note");
        final ExternalId txnExternalId = externalIdFactory.createFromCommand(command, LoanApiConstants.externalIdParameterName);
        changes.put("transactionDate", command.stringValueOfParameterNamed("transactionDate"));
        changes.put("transactionAmount", command.stringValueOfParameterNamed("transactionAmount"));
        changes.put("locale", command.locale());
        changes.put("dateFormat", command.dateFormat());
        changes.put("paymentTypeId", command.longValueOfParameterNamed("paymentTypeId"));

        if (StringUtils.isNotBlank(noteText)) {
            changes.put("note", noteText);
        }
        if (!txnExternalId.isEmpty()) {
            changes.put(LoanApiConstants.externalIdParameterName, txnExternalId);
        }

        final PaymentDetail paymentDetail = paymentDetailWritePlatformService.createAndPersistPaymentDetail(command, changes);
        final LoanTransactionType repaymentTransactionType = LoanTransactionType.INTEREST_PAYMENT_WAIVER;
        final LocalDate transactionDate = command.localDateValueOfParameterNamed("transactionDate");

        final BigDecimal transactionAmount = command.bigDecimalValueOfParameterNamed("transactionAmount");
        final Money repaymentAmount = Money.of(loan.getCurrency(), transactionAmount);
        return LoanTransaction.repaymentType(repaymentTransactionType, loan.getOffice(), repaymentAmount, paymentDetail, transactionDate,
                txnExternalId, null);
    }

    public LoanTransaction assembleAccrualActivityTransaction(Loan loan, final LoanRepaymentScheduleInstallment installment,
            final LocalDate transactionDate) {
        MonetaryCurrency currency = loan.getCurrency();
        ExternalId externalId = externalIdFactory.create();

        BigDecimal interestPortion = installment.getInterestCharged(currency).getAmount();
        BigDecimal feeChargesPortion = installment.getFeeChargesCharged(currency).getAmount();
        BigDecimal penaltyChargesPortion = installment.getPenaltyChargesCharged(currency).getAmount();
        BigDecimal transactionAmount = interestPortion.add(feeChargesPortion).add(penaltyChargesPortion);
        return new LoanTransaction(loan, loan.getOffice(), LoanTransactionType.ACCRUAL_ACTIVITY.getValue(), transactionDate,
                transactionAmount, null, interestPortion, feeChargesPortion, penaltyChargesPortion, null, false, null, externalId);
    }
}
