package org.mifosplatform.portfolio.loanaccount.service;

import java.util.Collection;

import org.mifosplatform.portfolio.charge.data.ChargeData;
import org.mifosplatform.portfolio.loanaccount.data.LoanChargeData;

public interface LoanChargeReadPlatformService {

    ChargeData retrieveLoanChargeTemplate();

    Collection<LoanChargeData> retrieveLoanCharges(Long loanId);

    LoanChargeData retrieveLoanChargeDetails(Long loanChargeId, Long loanId);
}
