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
import java.util.List;
import org.apache.fineract.portfolio.paymentdetail.domain.PaymentDetail;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBalance;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanCharge;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanChargePaidBy;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransaction;

/**
 * Charges due at disbursement of a Working Capital loan: the ones the application or the account endpoint put on the
 * loan. The product catalogue is only a default for the client application; it is never inherited here (term-loan
 * model). They are settled out of the disbursed money the moment the loan is disbursed, one transaction for their
 * total, and undone together with the disbursement.
 */
public interface WorkingCapitalLoanDisbursementChargeWriteService {

    /**
     * Resolves every disbursement charge on the loan against the disbursed amount and settles them all in one
     * repayment-at-disbursement transaction linked to the disbursement. Always records the net cash handed to the
     * client on the loan.
     *
     * @return the settlement transaction, or null when the loan carries no disbursement charge
     * @throws org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException
     *             when the charges would consume the whole disbursed amount
     */
    WorkingCapitalLoanTransaction settleChargesAtDisbursement(WorkingCapitalLoan loan,
            WorkingCapitalLoanTransaction disbursementTransaction, BigDecimal disbursedAmount, LocalDate disbursementDate,
            PaymentDetail paymentDetail);

    /**
     * Backs out what {@link #settleChargesAtDisbursement} did: reverses the settlement's journal entries, marks the
     * charges unpaid again and clears the net disbursal amount. The charges stay on the loan so a later disbursement
     * settles them again. Expects the settlement transaction to be already marked reversed by the caller.
     */
    void reverseChargesOnUndoDisbursal(WorkingCapitalLoan loan);

    /**
     * Reprocessing resets every charge's paid state and drops the charge-paid-by rows before replaying the repayments.
     * The settlement at disbursement is not a repayment and is never re-allocated: this puts its effect back - the
     * disbursement charges paid in full by the settlement, and the balance's paid fee / penalty totals - and returns
     * the rebuilt charge-paid-by lines for the caller to persist with the others.
     */
    List<WorkingCapitalLoanChargePaidBy> reapplyOnReprocess(WorkingCapitalLoan loan, WorkingCapitalLoanBalance balance,
            List<WorkingCapitalLoanCharge> charges, List<WorkingCapitalLoanTransaction> allTransactions);
}
