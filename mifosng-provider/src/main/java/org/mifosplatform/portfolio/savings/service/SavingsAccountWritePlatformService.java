/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.service;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;

public interface SavingsAccountWritePlatformService {

    CommandProcessingResult activate(Long savingsId, JsonCommand command);

    CommandProcessingResult deposit(Long savingsId, JsonCommand command);

    CommandProcessingResult withdrawal(Long savingsId, JsonCommand command);

    CommandProcessingResult applyAnnualFee(Long savingsId, LocalDate annualFeeTransactionDate);

    CommandProcessingResult calculateInterest(Long savingsId);

    CommandProcessingResult postInterest(Long savingsId);

    CommandProcessingResult undoTransaction(Long savingsId, Long transactionId);

    void postInterestForAccounts();

    CommandProcessingResult adjustSavingsTransaction(Long savingsId, Long transactionId, JsonCommand command);
}