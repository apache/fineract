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
package org.apache.fineract.portfolio.loanorigination.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.portfolio.loanorigination.data.LoanOriginatorData;
import org.apache.fineract.portfolio.loanorigination.domain.LoanOriginator;
import org.apache.fineract.portfolio.loanorigination.domain.LoanOriginatorRepository;
import org.apache.fineract.portfolio.loanorigination.exception.LoanOriginatorNotFoundException;
import org.apache.fineract.portfolio.loanorigination.mapper.LoanOriginatorMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@ConditionalOnProperty(value = "fineract.module.loan-origination.enabled", havingValue = "true")
public class LoanOriginatorReadPlatformServiceImpl implements LoanOriginatorReadPlatformService {

    private final LoanOriginatorRepository loanOriginatorRepository;
    private final LoanOriginatorMapper loanOriginatorMapper;

    @Override
    public List<LoanOriginatorData> retrieveAll() {
        final List<LoanOriginator> originators = this.loanOriginatorRepository.findAllWithCodeValues();
        return this.loanOriginatorMapper.toDataList(originators);
    }

    @Override
    public LoanOriginatorData retrieveById(final Long id) {
        final LoanOriginator originator = this.loanOriginatorRepository.findByIdWithCodeValues(id)
                .orElseThrow(() -> new LoanOriginatorNotFoundException(id));
        return this.loanOriginatorMapper.toData(originator);
    }

    @Override
    public LoanOriginatorData retrieveByExternalId(final String externalId) {
        final LoanOriginator originator = this.loanOriginatorRepository.findByExternalIdWithCodeValues(new ExternalId(externalId))
                .orElseThrow(() -> new LoanOriginatorNotFoundException(externalId));
        return this.loanOriginatorMapper.toData(originator);
    }

    @Override
    public Long resolveIdByExternalId(final String externalId) {
        final LoanOriginator originator = this.loanOriginatorRepository.findByExternalId(new ExternalId(externalId))
                .orElseThrow(() -> new LoanOriginatorNotFoundException(externalId));
        return originator.getId();
    }
}
