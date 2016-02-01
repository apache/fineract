/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.service;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.jobs.exception.JobExecutionException;
import org.mifosplatform.organisation.office.domain.Office;
import org.mifosplatform.organisation.staff.domain.Staff;
import org.mifosplatform.portfolio.savings.DepositAccountType;
import org.mifosplatform.portfolio.savings.data.SavingsAccountTransactionDTO;
import org.mifosplatform.portfolio.savings.domain.SavingsAccountTransaction;

public interface DepositAccountWritePlatformService {

    CommandProcessingResult activateFDAccount(Long savingsId, JsonCommand command);

    CommandProcessingResult activateRDAccount(Long savingsId, JsonCommand command);

    CommandProcessingResult updateDepositAmountForRDAccount(Long savingsId, JsonCommand command);

    CommandProcessingResult depositToFDAccount(Long savingsId, JsonCommand command);

    CommandProcessingResult depositToRDAccount(Long savingsId, JsonCommand command);

    CommandProcessingResult withdrawal(Long savingsId, JsonCommand command, final DepositAccountType depositAccountType);

    CommandProcessingResult calculateInterest(Long savingsId, final DepositAccountType depositAccountType);

    CommandProcessingResult postInterest(Long savingsId, final DepositAccountType depositAccountType);

    CommandProcessingResult undoFDTransaction(Long savingsId, Long transactionId, boolean allowAccountTransferModification);

    CommandProcessingResult undoRDTransaction(Long savingsId, Long transactionId, boolean allowAccountTransferModification);

    CommandProcessingResult adjustFDTransaction(Long savingsId, Long transactionId, JsonCommand command);

    CommandProcessingResult adjustRDTransaction(Long savingsId, Long transactionId, JsonCommand command);

    CommandProcessingResult closeFDAccount(Long savingsId, JsonCommand command);

    CommandProcessingResult closeRDAccount(Long savingsId, JsonCommand command);

    CommandProcessingResult prematureCloseFDAccount(Long savingsId, JsonCommand command);

    CommandProcessingResult prematureCloseRDAccount(Long savingsId, JsonCommand command);

    SavingsAccountTransaction initiateSavingsTransfer(Long accountId, LocalDate transferDate, final DepositAccountType depositAccountType);

    SavingsAccountTransaction withdrawSavingsTransfer(Long accountId, LocalDate transferDate, final DepositAccountType depositAccountType);

    void rejectSavingsTransfer(Long accountId, final DepositAccountType depositAccountType);

    SavingsAccountTransaction acceptSavingsTransfer(Long accountId, LocalDate transferDate, Office acceptedInOffice, Staff staff,
            final DepositAccountType depositAccountType);

    CommandProcessingResult addSavingsAccountCharge(JsonCommand command, final DepositAccountType depositAccountType);

    CommandProcessingResult updateSavingsAccountCharge(JsonCommand command, final DepositAccountType depositAccountType);

    CommandProcessingResult deleteSavingsAccountCharge(Long savingsAccountId, Long savingsAccountChargeId, JsonCommand command,
            final DepositAccountType depositAccountType);

    CommandProcessingResult waiveCharge(Long savingsAccountId, Long savingsAccountChargeId, final DepositAccountType depositAccountType);

    CommandProcessingResult payCharge(Long savingsAccountId, Long savingsAccountChargeId, JsonCommand command,
            final DepositAccountType depositAccountType);

    void applyChargeDue(final Long savingsAccountChargeId, final Long accountId, final DepositAccountType depositAccountType);

    void updateMaturityDetails(final Long depositAccountId, final DepositAccountType depositAccountType);

    void transferInterestToSavings() throws JobExecutionException;

    SavingsAccountTransaction mandatorySavingsAccountDeposit(final SavingsAccountTransactionDTO accountTransactionDTO);
}