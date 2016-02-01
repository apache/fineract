/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.service;

import java.util.Collection;

import org.mifosplatform.portfolio.charge.data.ChargeData;
import org.mifosplatform.portfolio.loanaccount.data.LoanChargeData;
import org.mifosplatform.portfolio.loanaccount.data.LoanChargePaidByData;
import org.mifosplatform.portfolio.loanaccount.data.LoanInstallmentChargeData;
import org.mifosplatform.portfolio.loanaccount.domain.LoanTransactionType;

public interface LoanChargeReadPlatformService {

    ChargeData retrieveLoanChargeTemplate();

    Collection<LoanChargeData> retrieveLoanCharges(Long loanId);

    LoanChargeData retrieveLoanChargeDetails(Long loanChargeId, Long loanId);

    Collection<LoanChargeData> retrieveLoanChargesForFeePayment(Integer paymentMode, Integer loanStatus);

    Collection<LoanInstallmentChargeData> retrieveInstallmentLoanCharges(Long loanChargeId, boolean onlyPaymentPendingCharges);

    Collection<Integer> retrieveOverdueInstallmentChargeFrequencyNumber(Long loanId, Long chargeId, Integer periodNumber);
    
    Collection<LoanChargeData> retrieveLoanChargesForAccural(Long loanId);

    Collection<LoanChargePaidByData> retriveLoanChargesPaidBy(Long chargeId, LoanTransactionType transactionType, Integer installmentNumber);
}
