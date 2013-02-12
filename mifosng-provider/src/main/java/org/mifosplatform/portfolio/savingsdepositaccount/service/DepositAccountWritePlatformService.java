/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savingsdepositaccount.service;

import java.util.Collection;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.portfolio.savingsdepositaccount.data.DepositAccountsForLookup;
import org.springframework.security.access.prepost.PreAuthorize;

public interface DepositAccountWritePlatformService {

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'CREATE_DEPOSITACCOUNT')")
    CommandProcessingResult createDepositAccount(JsonCommand command);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'UPDATE_DEPOSITACCOUNT')")
    CommandProcessingResult updateDepositAccount(Long accountId, JsonCommand command);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'DELETE_DEPOSITACCOUNT')")
    CommandProcessingResult deleteDepositAccount(Long entityId);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'APPROVE_DEPOSITACCOUNT')")
    CommandProcessingResult approveDepositApplication(JsonCommand command);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'REJECT_DEPOSITACCOUNT')")
    CommandProcessingResult rejectDepositApplication(JsonCommand command);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'WITHDRAW_DEPOSITACCOUNT')")
    CommandProcessingResult withdrawDepositApplication(JsonCommand command);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'APPROVALUNDO_DEPOSITACCOUNT')")
    CommandProcessingResult undoDepositApproval(JsonCommand undoCommand);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'WITHDRAWAL_DEPOSITACCOUNT')")
    CommandProcessingResult withdrawDepositAccountMoney(JsonCommand command);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'INTEREST_DEPOSITACCOUNT')")
    CommandProcessingResult withdrawDepositAccountInterestMoney(JsonCommand command);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'RENEW_DEPOSITACCOUNT')")
    CommandProcessingResult renewDepositAccount(JsonCommand command);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS')")
    CommandProcessingResult postInterestToDepositAccount(Collection<DepositAccountsForLookup> accounts);

}