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
package org.apache.fineract.accounting.journalentry.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.investor.domain.ExternalAssetOwner;
import org.apache.fineract.investor.domain.ExternalAssetOwnerTransfer;
import org.apache.fineract.investor.service.AccountingService;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnMissingBean(value = ExternalOwnerJournalEntryWriteService.class, ignored = ExternalOwnerJournalEntryWriteServiceImpl.class)
@RequiredArgsConstructor
@Slf4j
public class ExternalOwnerJournalEntryWriteServiceImpl implements ExternalOwnerJournalEntryWriteService {

    private final AccountingService accountingService;

    @Override
    public void createJournalEntriesForExternalOwnerTransfer(final Loan loan, final ExternalAssetOwnerTransfer externalAssetOwnerTransfer,
            final ExternalAssetOwner previousOwner) {
        final boolean isBuyback = externalAssetOwnerTransfer.getStatus().name().contains("BUYBACK");

        if (isBuyback) {
            this.accountingService.createJournalEntriesForBuybackAssetTransfer(loan, externalAssetOwnerTransfer);
        } else {
            this.accountingService.createJournalEntriesForSaleAssetTransfer(loan, externalAssetOwnerTransfer, previousOwner);
        }
    }
}
