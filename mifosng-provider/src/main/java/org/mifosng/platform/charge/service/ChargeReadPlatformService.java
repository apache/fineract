package org.mifosng.platform.charge.service;

import org.mifosng.platform.api.data.ChargeData;

import java.util.Collection;

public interface ChargeReadPlatformService {

    Collection<ChargeData> retrieveAllCharges();

    ChargeData retrieveCharge(Long chargeId);

    ChargeData retrieveNewChargeDetails();

    Collection<ChargeData> retrieveLoanApplicableCharges();

    Collection<ChargeData> retrieveLoanProductCharges(Long loanProductId);
}
