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

import jakarta.annotation.Nullable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanTransactionFeeRefundPreBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.portfolio.loanaccount.domain.DownPaymentFees;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRelation;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRelationTypeEnum;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoanFeeRefundService {

    private final BusinessEventNotifierService businessEventNotifierService;
    private final ExternalIdFactory externalIdFactory;

    /**
     * Fee refund of the refund, dated as the refund and linked to it, or null when nothing is left to refund.
     */
    @Nullable
    public LoanTransaction createFeeRefund(final Loan loan, final LoanTransaction refund) {
        final BigDecimal feeRefundAmount = DownPaymentFees.refundAmount(loan, refund);
        if (MathUtil.isZero(feeRefundAmount)) {
            return null;
        }
        businessEventNotifierService.notifyPreBusinessEvent(new LoanTransactionFeeRefundPreBusinessEvent(loan));
        final LoanTransaction feeRefund = LoanTransaction.feeRefund(loan, feeRefundAmount, refund.getDateOf(), externalIdFactory.create());
        feeRefund.getLoanTransactionRelations()
                .add(LoanTransactionRelation.linkToTransaction(feeRefund, refund, LoanTransactionRelationTypeEnum.RELATED));
        return feeRefund;
    }

    /**
     * Fee refunds of the refunds that have none but get a positive amount now, for example after the refund that used
     * up the cap was undone. They are added to the loan; the caller replays them, then saves them and raises the post
     * event.
     */
    public List<LoanTransaction> createMissingFeeRefunds(final Loan loan) {
        final List<LoanTransaction> feeRefunds = DownPaymentFees.refundsWithoutFeeRefund(loan).stream()
                .map(refund -> createFeeRefund(loan, refund)).filter(Objects::nonNull).toList();
        feeRefunds.forEach(loan::addLoanTransaction);
        return feeRefunds;
    }
}
