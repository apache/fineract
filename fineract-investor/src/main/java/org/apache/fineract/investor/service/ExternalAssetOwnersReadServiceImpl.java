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
package org.apache.fineract.investor.service;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.accounting.journalentry.JournalEntryMapper;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.investor.data.ExternalOwnerJournalEntryData;
import org.apache.fineract.investor.data.ExternalOwnerTransferJournalEntryData;
import org.apache.fineract.investor.data.ExternalTransferData;
import org.apache.fineract.investor.domain.ExternalAssetOwnerJournalEntryMapping;
import org.apache.fineract.investor.domain.ExternalAssetOwnerJournalEntryMappingRepository;
import org.apache.fineract.investor.domain.ExternalAssetOwnerTransfer;
import org.apache.fineract.investor.domain.ExternalAssetOwnerTransferJournalEntryMapping;
import org.apache.fineract.investor.domain.ExternalAssetOwnerTransferJournalEntryMappingRepository;
import org.apache.fineract.investor.domain.ExternalAssetOwnerTransferLoanMapping;
import org.apache.fineract.investor.domain.ExternalAssetOwnerTransferLoanMappingRepository;
import org.apache.fineract.investor.domain.ExternalAssetOwnerTransferRepository;
import org.apache.fineract.investor.exception.ExternalAssetOwnerTransferNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExternalAssetOwnersReadServiceImpl implements ExternalAssetOwnersReadService {

    private final ExternalAssetOwnerTransferRepository externalAssetOwnerTransferRepository;
    private final ExternalAssetOwnerTransferLoanMappingRepository externalAssetOwnerTransferLoanMappingRepository;
    private final ExternalAssetOwnerTransferJournalEntryMappingRepository externalAssetOwnerTransferJournalEntryMappingRepository;
    private final ExternalAssetOwnerJournalEntryMappingRepository externalAssetOwnerJournalEntryMappingRepository;
    private final ExternalAssetOwnersTransferMapper mapper;
    private final JournalEntryMapper journalEntryMapper;

    @Override
    public Page<ExternalTransferData> retrieveTransferData(Long loanId, String externalLoanId, String externalTransferId, Integer offset,
            Integer limit) {
        Page<ExternalAssetOwnerTransfer> result;
        PageRequest pageRequest = getPageRequest(offset, limit);
        if (loanId != null) {
            result = externalAssetOwnerTransferRepository.findAllByLoanId(loanId, pageRequest);
        } else if (externalLoanId != null) {
            result = externalAssetOwnerTransferRepository.findAllByExternalLoanId(ExternalIdFactory.produce(externalLoanId), pageRequest);
        } else if (externalTransferId != null) {
            result = externalAssetOwnerTransferRepository.findAllByExternalId(ExternalIdFactory.produce(externalTransferId), pageRequest);
        } else {
            throw new IllegalArgumentException(
                    "At least one of the following parameters must be provided: loanId, externalLoanId, transferExternalId");
        }
        return result.map(mapper::mapTransfer);
    }

    @Override
    public ExternalTransferData retrieveActiveTransferData(Long loanId, String externalLoanId, String transferExternalId) {
        Optional<ExternalAssetOwnerTransferLoanMapping> result;
        if (loanId != null) {
            result = externalAssetOwnerTransferLoanMappingRepository.findByLoanId(loanId);
        } else if (externalLoanId != null) {
            result = externalAssetOwnerTransferLoanMappingRepository.findByLoanExternalId(ExternalIdFactory.produce(externalLoanId));
        } else if (transferExternalId != null) {
            result = externalAssetOwnerTransferLoanMappingRepository
                    .findByTransferExternalId(ExternalIdFactory.produce(transferExternalId));
        } else {
            throw new IllegalArgumentException(
                    "At least one of the following parameters must be provided: loanId, externalLoanId, transferExternalId");
        }
        return result.map(transferLoanMapping -> mapper.mapTransfer(transferLoanMapping.getOwnerTransfer())).orElse(null);
    }

    @Override
    public ExternalOwnerTransferJournalEntryData retrieveJournalEntriesOfTransfer(Long transferId, Integer offset, Integer limit) {
        PageRequest pageRequest = getPageRequest(offset, limit);
        Page<ExternalAssetOwnerTransferJournalEntryMapping> result = externalAssetOwnerTransferJournalEntryMappingRepository
                .findByTransferId(transferId, pageRequest);
        ExternalOwnerTransferJournalEntryData mappedResult = new ExternalOwnerTransferJournalEntryData();
        if (result.hasContent()) {
            mappedResult.setJournalEntryData(result.map(entry -> journalEntryMapper.map(entry.getJournalEntry())));
            mappedResult.setTransferData(mapper.mapTransfer(result.stream().findFirst().get().getOwnerTransfer()));
        }
        return mappedResult;
    }

    @Override
    public ExternalOwnerJournalEntryData retrieveJournalEntriesOfOwner(String ownerExternalId, Integer offset, Integer limit) {
        PageRequest pageRequest = getPageRequest(offset, limit);
        Page<ExternalAssetOwnerJournalEntryMapping> result = externalAssetOwnerJournalEntryMappingRepository
                .findByExternalOwnerId(ExternalIdFactory.produce(ownerExternalId), pageRequest);
        ExternalOwnerJournalEntryData mappedResult = new ExternalOwnerJournalEntryData();
        if (result.hasContent()) {
            mappedResult.setJournalEntryData(result.map(entry -> journalEntryMapper.map(entry.getJournalEntry())));
            mappedResult.setOwnerData(mapper.mapOwner(result.stream().findFirst().get().getOwner()));
        }
        return mappedResult;
    }

    @Override
    public ExternalTransferData retrieveFirstTransferByExternalId(ExternalId externalTransferId) {
        return externalAssetOwnerTransferRepository.findFirstByExternalIdOrderByIdAsc(externalTransferId).map(mapper::mapTransfer)
                .orElseThrow(() -> new ExternalAssetOwnerTransferNotFoundException(externalTransferId));
    }

    @Override
    public Long retrieveLastTransferIdByExternalId(ExternalId externalTransferId) {
        return externalAssetOwnerTransferRepository.findLastByExternalIdOrderByIdDesc(externalTransferId)
                .orElseThrow(() -> new ExternalAssetOwnerTransferNotFoundException(externalTransferId));
    }

    @Override
    public ExternalTransferData retrieveTransferData(Long transferId) {
        return externalAssetOwnerTransferRepository.findById(transferId).map(mapper::mapTransfer)
                .orElseThrow(() -> new ExternalAssetOwnerTransferNotFoundException(transferId));
    }

    private PageRequest getPageRequest(Integer offset, Integer limit) {
        if (offset == null) {
            offset = 0;
        }
        if (limit == null) {
            limit = 100;
        }
        return PageRequest.of(offset, limit, Sort.by("id"));
    }

}
