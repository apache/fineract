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
package org.apache.fineract.migration.service;

import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.migration.data.LoanMigrationResponseDto;
import org.apache.fineract.migration.domain.LoanMigration;
import org.apache.fineract.migration.domain.LoanMigrationRepository;
import org.apache.fineract.migration.domain.LoanMigrationStatus;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoanMigrationReadPlatformServiceImpl implements LoanMigrationReadPlatformService {

    private final LoanMigrationRepository loanMigrationRepository;
    private final LoanRepositoryWrapper loanRepositoryWrapper;

    @Override
    public LoanMigrationResponseDto getMigrationStatus(Long loanId) {
        return mapToDto(getMigrationStatusByLoanId(loanId));
    }

    @Override
    public LoanMigrationResponseDto getMigrationStatus(String loanExternalId) {
        Long loanId = loanRepositoryWrapper.findIdByExternalId(new ExternalId(loanExternalId));
        return mapToDto(getMigrationStatusByLoanId(loanId));
    }

    private LoanMigration getMigrationStatusByLoanId(Long loanId) {
        return loanMigrationRepository.findByLoanId(loanId).orElseThrow(
                () -> new GeneralPlatformDomainRuleException("error.msg.loan.migration.not.found", "Migration for Loan not found"));
    }

    private LoanMigrationResponseDto mapToDto(LoanMigration migration) {
        return new LoanMigrationResponseDto(LoanMigrationStatus.valueOf(migration.getStatus()), migration.getMigrationStartDateTime(),
                migration.getMigrationEndDateTime(), DateUtils.DEFAULT_DATE_FORMAT, Locale.getDefault().toString());
    }
}
