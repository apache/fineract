/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.guarantor.service;

import org.joda.time.LocalDate;
import org.mifosplatform.portfolio.loanaccount.domain.Loan;
import org.mifosplatform.portfolio.loanaccount.guarantor.domain.GuarantorFundingDetails;

public interface GuarantorDomainService {

    void releaseGuarantor(GuarantorFundingDetails guarantorFundingDetails, LocalDate transactionDate);

    void validateGuarantorBusinessRules(Loan loan);

    void assignGuarantor(GuarantorFundingDetails guarantorFundingDetails, LocalDate transactionDate);

    void transaferFundsFromGuarantor(Loan loan);

}
