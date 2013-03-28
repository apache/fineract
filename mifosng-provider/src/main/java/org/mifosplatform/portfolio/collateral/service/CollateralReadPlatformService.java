/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.collateral.service;

import java.util.List;

import org.mifosplatform.portfolio.collateral.data.CollateralData;

public interface CollateralReadPlatformService {

    /**
     * Validates the passed in loanId before retrieving Collaterals for the same
     * 
     * @param loanId
     * @return
     */
    List<CollateralData> retrieveCollateralsForValidLoan(Long loanId);

    List<CollateralData> retrieveCollaterals(Long loanId);

    CollateralData retrieveCollateral(Long loanId, Long collateralId);

}
