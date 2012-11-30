package org.mifosplatform.portfolio.charge.service;

import java.util.Collection;

import org.mifosng.platform.api.data.LoanChargeData;
import org.mifosplatform.portfolio.charge.data.ChargeData;

// FIXME - KW - break out methods that belong to product charges and loan charges
public interface ChargeReadPlatformService {

    Collection<ChargeData> retrieveAllCharges();

    ChargeData retrieveCharge(Long chargeId);

    ChargeData retrieveNewChargeDetails();

    Collection<ChargeData> retrieveLoanApplicableCharges(boolean feeChargesOnly);

    Collection<ChargeData> retrieveLoanProductCharges(Long loanProductId);

    Collection<LoanChargeData> retrieveLoanCharges(Long loanId);

    LoanChargeData retrieveLoanChargeDetails(Long loanChargeId, Long loanId);

    ChargeData retrieveLoanChargeTemplate();
}
