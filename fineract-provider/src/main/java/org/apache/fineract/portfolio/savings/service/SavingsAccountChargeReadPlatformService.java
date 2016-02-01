/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.service;

import java.util.Collection;

import org.mifosplatform.portfolio.charge.data.ChargeData;
import org.mifosplatform.portfolio.savings.data.SavingsAccountAnnualFeeData;
import org.mifosplatform.portfolio.savings.data.SavingsAccountChargeData;

public interface SavingsAccountChargeReadPlatformService {

    ChargeData retrieveSavingsAccountChargeTemplate();

    Collection<SavingsAccountChargeData> retrieveSavingsAccountCharges(Long savingsAccountId, String status);

    SavingsAccountChargeData retrieveSavingsAccountChargeDetails(Long savingsAccountChargeId, Long savingsAccountId);

    Collection<SavingsAccountAnnualFeeData> retrieveChargesWithAnnualFeeDue();

    Collection<SavingsAccountAnnualFeeData> retrieveChargesWithDue();
}
