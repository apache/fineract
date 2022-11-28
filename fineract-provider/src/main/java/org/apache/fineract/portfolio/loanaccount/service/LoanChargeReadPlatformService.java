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

import java.util.Collection;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.portfolio.charge.data.ChargeData;
import org.apache.fineract.portfolio.charge.domain.Charge;
import org.apache.fineract.portfolio.loanaccount.data.LoanChargeData;
import org.apache.fineract.portfolio.loanaccount.data.LoanChargePaidByData;
import org.apache.fineract.portfolio.loanaccount.data.LoanInstallmentChargeData;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;

public interface LoanChargeReadPlatformService {

    ChargeData retrieveLoanChargeTemplate();

    Collection<LoanChargeData> retrieveLoanCharges(Long loanId);

    LoanChargeData retrieveLoanChargeDetails(Long loanChargeId, Long loanId);

    Collection<LoanChargeData> retrieveLoanChargesForFeePayment(Integer paymentMode, Integer loanStatus);

    Collection<LoanInstallmentChargeData> retrieveInstallmentLoanCharges(Long loanChargeId, boolean onlyPaymentPendingCharges);

    Collection<Integer> retrieveOverdueInstallmentChargeFrequencyNumber(Loan loan, Charge charge, Integer periodNumber);

    Collection<LoanChargeData> retrieveLoanChargesForAccrual(Long loanId);

    Collection<LoanChargePaidByData> retrieveLoanChargesPaidBy(Long chargeId, LoanTransactionType transactionType,
            Integer installmentNumber);

    Long retrieveLoanChargeIdByExternalId(ExternalId loanChargeExternalId);
}
