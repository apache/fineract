package org.mifosng.platform.charge.service;

import org.mifosng.platform.api.data.ChargeData;
import org.mifosng.platform.api.data.LoanChargeData;

import java.util.Collection;

public interface ChargeReadPlatformService {

    Collection<ChargeData> retrieveAllCharges();

    ChargeData retrieveCharge(Long chargeId);

    ChargeData retrieveNewChargeDetails();

    Collection<ChargeData> retrieveLoanApplicableCharges(boolean feeChargesOnly);

    Collection<ChargeData> retrieveLoanProductCharges(Long loanProductId);

    Collection<LoanChargeData> retrieveLoanCharges(Long loanId);

    ChargeData retrieveLoanChargeTemplate();
}
