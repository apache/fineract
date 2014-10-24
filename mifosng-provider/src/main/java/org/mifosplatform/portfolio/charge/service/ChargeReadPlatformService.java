/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.charge.service;

import java.util.Collection;

import org.mifosplatform.portfolio.charge.data.ChargeData;
import org.mifosplatform.portfolio.charge.domain.ChargeTimeType;

public interface ChargeReadPlatformService {

    Collection<ChargeData> retrieveAllCharges();

    Collection<ChargeData> retrieveAllChargesForCurrency(String currencyCode);

    ChargeData retrieveCharge(Long chargeId);

    ChargeData retrieveNewChargeDetails();

    /**
     * Returns all charges applicable for loans
     * 
     * @param feeChargesOnly
     *            If set to "true", only fees are returned
     * @param excludeChargeTimes
     *            Excludes Given List of Charge Types from the response
     * @return
     */
    Collection<ChargeData> retrieveLoanApplicableCharges(boolean feeChargesOnly, ChargeTimeType[] excludeChargeTimes);

    /**
     * Returns all Penalties applicable for loans
     * 
     * @return
     */
    Collection<ChargeData> retrieveLoanApplicablePenalties();

    /**
     * Returns all Charges associated with a given Loan Product
     * 
     * @param loanProductId
     * @return
     */
    Collection<ChargeData> retrieveLoanProductCharges(Long loanProductId);

    /**
     * Returns all charges applicable for a given loan product
     * 
     * @param loanProductId
     * @param chargeTime
     *            Filters based on the type of the charge to be returned
     * @return
     */
    Collection<ChargeData> retrieveLoanProductCharges(Long loanProductId, ChargeTimeType chargeTime);

    /**
     * Returns all charges applicable for savings
     * 
     * @param feeChargesOnly
     * @return
     */
    Collection<ChargeData> retrieveSavingsAccountApplicableCharges(boolean feeChargesOnly);

    /**
     * Returns all penalties applicable for savings
     * 
     * @return
     */
    Collection<ChargeData> retrieveSavingsAccountApplicablePenalties();

    /**
     * Returns all charges applicable for a given savings product
     * 
     * @param savingsProductId
     * @return
     */
    Collection<ChargeData> retrieveSavingsProductCharges(Long savingsProductId);

}
