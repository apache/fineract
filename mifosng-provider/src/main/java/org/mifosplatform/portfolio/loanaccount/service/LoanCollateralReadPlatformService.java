package org.mifosplatform.portfolio.loanaccount.service;

import java.util.Collection;

import org.mifosplatform.portfolio.loanaccount.data.LoanCollateralData;

public interface LoanCollateralReadPlatformService {

    Collection<LoanCollateralData> retrieveLoanCollateral(Long loanId);
}
