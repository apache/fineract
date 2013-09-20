/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.charge.service;

import java.util.Collection;

import org.mifosplatform.portfolio.charge.data.ChargeData;

public interface ChargeReadPlatformService {

    Collection<ChargeData> retrieveAllCharges();

    ChargeData retrieveCharge(Long chargeId);

    ChargeData retrieveNewChargeDetails();

    Collection<ChargeData> retrieveLoanApplicableCharges(boolean feeChargesOnly);

    Collection<ChargeData> retrieveLoanApplicablePenalties();

    Collection<ChargeData> retrieveLoanProductCharges(Long loanProductId);

    Collection<ChargeData> retrieveSavingsAccountApplicableCharges(boolean feeChargesOnly);

    Collection<ChargeData> retrieveSavingsAccountApplicablePenalties();

    Collection<ChargeData> retrieveSavingsProductCharges(Long savingsProductId);
}
