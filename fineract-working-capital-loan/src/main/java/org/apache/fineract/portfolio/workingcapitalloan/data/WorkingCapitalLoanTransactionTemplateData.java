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

package org.apache.fineract.portfolio.workingcapitalloan.data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.portfolio.loanaccount.data.LoanTransactionEnumData;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeData;

/**
 * Template behind {@code GET /working-capital-loans/{loanId}/transactions/template?command=...}: what a caller needs to
 * pre-fill the form for one transaction type. Every transaction command answers with this shape, the way the term-loan
 * transaction template answers every command with {@code LoanTransactionData}, so each command fills in the subset that
 * applies to it and leaves the rest null.
 * <p>
 * Approval is the one loan action that is not a transaction - it moves the loan's status and posts nothing - so it
 * keeps its own {@link WorkingCapitalLoanCommandTemplateData} on {@code GET /working-capital-loans/{loanId}/template}.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkingCapitalLoanTransactionTemplateData implements Serializable {

    private Long wcLoanId;
    private CurrencyData currency;

    private LocalDate transactionDate;
    private BigDecimal transactionAmount;
    private LoanTransactionEnumData type;

    /**
     * Suggested amount to pre-fill, where the command has one that the user may then change - outstanding principal for
     * a repayment, the overpayment for a credit balance refund, what is still recoverable for a recovery payment. It is
     * deliberately separate from {@code transactionAmount}: a prepayment quotes an exact payoff that has to be paid in
     * full, whereas this is only an opening suggestion.
     */
    private BigDecimal expectedAmount;

    /** disburse only: the disbursement's own date and discount terms. */
    private LocalDate expectedDisbursementDate;
    private BigDecimal discountAmount;
    private Boolean overrideDiscountDisabled;

    private BigDecimal principalPortion;
    private BigDecimal feeChargesPortion;
    private BigDecimal penaltyChargesPortion;

    private BigDecimal chargeOffAmount;
    private LocalDate chargeOffDate;

    private List<PaymentTypeData> paymentTypeOptions;
    private List<CodeValueData> classificationOptions;
    private List<CodeValueData> chargeOffReasonOptions;
}
