/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.portfolio.savings.service;

import java.util.Set;

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.organisation.staff.domain.Staff;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransaction;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;

public interface SavingsAccountWritePlatformService {

    CommandProcessingResult activate(Long savingsId, JsonCommand command);

    CommandProcessingResult deposit(Long savingsId, JsonCommand command);

    CommandProcessingResult withdrawal(Long savingsId, JsonCommand command);

    CommandProcessingResult applyAnnualFee(final Long savingsAccountChargeId, final Long accountId);

    CommandProcessingResult calculateInterest(Long savingsId);

    CommandProcessingResult undoTransaction(Long savingsId, Long transactionId, boolean allowAccountTransferModification);

    CommandProcessingResult adjustSavingsTransaction(Long savingsId, Long transactionId, JsonCommand command);

    CommandProcessingResult close(Long savingsId, JsonCommand command);

    SavingsAccountTransaction initiateSavingsTransfer(SavingsAccount account, LocalDate transferDate);

    SavingsAccountTransaction withdrawSavingsTransfer(SavingsAccount account, LocalDate transferDate);

    void rejectSavingsTransfer(SavingsAccount account);

    SavingsAccountTransaction acceptSavingsTransfer(SavingsAccount account, LocalDate transferDate, Office acceptedInOffice, Staff staff);

    CommandProcessingResult addSavingsAccountCharge(JsonCommand command);

    CommandProcessingResult updateSavingsAccountCharge(JsonCommand command);

    CommandProcessingResult deleteSavingsAccountCharge(Long savingsAccountId, Long savingsAccountChargeId, JsonCommand command);

    CommandProcessingResult waiveCharge(Long savingsAccountId, Long savingsAccountChargeId);

    CommandProcessingResult payCharge(Long savingsAccountId, Long savingsAccountChargeId, JsonCommand command);

    CommandProcessingResult inactivateCharge(Long savingsAccountId, Long savingsAccountChargeId);

    CommandProcessingResult assignFieldOfficer(Long savingsAccountId, JsonCommand command);

    CommandProcessingResult unassignFieldOfficer(Long savingsAccountId, JsonCommand command);

    void applyChargeDue(final Long savingsAccountChargeId, final Long accountId);

    void processPostActiveActions(SavingsAccount account, DateTimeFormatter fmt, Set<Long> existingTransactionIds,
            Set<Long> existingReversedTransactionIds);


    CommandProcessingResult modifyWithHoldTax(Long savingsAccountId, JsonCommand command);

	void setSubStatusInactive(Long savingsId);

	void setSubStatusDormant(Long savingsId);

	void escheat(Long savingsId);

    CommandProcessingResult postInterest(JsonCommand command);

    void postInterest(SavingsAccount account, boolean postInterestAs, LocalDate transactionDate);
    
    CommandProcessingResult blockAccount(Long savingsId);

    CommandProcessingResult unblockAccount(Long savingsId);

    CommandProcessingResult holdAmount(Long savingsId, JsonCommand command);

    CommandProcessingResult blockCredits(Long savingsId);

    CommandProcessingResult unblockCredits(Long savingsId);

    CommandProcessingResult blockDebits(Long savingsId);

    CommandProcessingResult unblockDebits(Long savingsId);

    CommandProcessingResult releaseAmount(Long savingsId, Long transactionId);
}