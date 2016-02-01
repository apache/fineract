/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.service;

import org.mifosplatform.infrastructure.core.service.Page;
import org.mifosplatform.infrastructure.core.service.SearchParameters;
import org.mifosplatform.portfolio.savings.data.DepositAccountOnHoldTransactionData;

public interface DepositAccountOnHoldTransactionReadPlatformService {

    public Page<DepositAccountOnHoldTransactionData> retriveAll(final Long savingsId, final Long guarantorFundingId,
            final SearchParameters searchParameters);

}
