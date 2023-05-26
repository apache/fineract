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

import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.investor.data.ExternalTransferData;
import org.apache.fineract.investor.domain.ExternalAssetOwnerTransfer;
import org.apache.fineract.investor.domain.ExternalAssetOwnerTransferRepository;
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
    private final ExternalAssetOwnersTransferMapper mapper;

    @Override
    public Page<ExternalTransferData> retrieveTransferData(Long loanId, String externalLoanId, String transferExternalId, Integer offset,
            Integer limit) {
        Page<ExternalAssetOwnerTransfer> result;
        if (offset == null) {
            offset = 0;
        }
        if (limit == null) {
            limit = 100;
        }
        PageRequest pageRequest = PageRequest.of(offset, limit, Sort.by("id"));
        if (loanId != null) {
            result = externalAssetOwnerTransferRepository.findAllByLoanId(loanId, pageRequest);
        } else if (externalLoanId != null) {
            result = externalAssetOwnerTransferRepository.findAllByExternalLoanId(ExternalIdFactory.produce(externalLoanId), pageRequest);
        } else if (transferExternalId != null) {
            result = externalAssetOwnerTransferRepository.findAllByExternalId(ExternalIdFactory.produce(transferExternalId), pageRequest);
        } else {
            throw new IllegalArgumentException(
                    "At least one of the following parameters must be provided: loanId, externalLoanId, transferExternalId");
        }
        return result.map(mapper::mapTransfer);
    }

}
