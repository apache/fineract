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

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanAccountSnapshotBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.migration.domain.LoanMigration;
import org.apache.fineract.migration.domain.LoanMigrationRepository;
import org.apache.fineract.migration.domain.LoanMigrationStatus;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Transactional
public class LoanMigrationWritePlatformServiceImpl implements LoanMigrationWritePlatformService {

    private final LoanMigrationRepository loanMigrationRepository;
    private final LoanRepositoryWrapper loanRepositoryWrapper;
    private final BusinessEventNotifierService businessEventNotifierService;

    @Override
    public CommandProcessingResult createMigration(ExternalId loanExternalId, LoanMigrationStatus action) {
        Loan loan = loanRepositoryWrapper.findOneWithNotFoundDetection(loanExternalId);
        return processMigrationCreation(loan, action);
    }

    @Override
    public CommandProcessingResult createMigration(Long loanId, LoanMigrationStatus action) {
        Loan loan = loanRepositoryWrapper.findOneWithNotFoundDetection(loanId);
        return processMigrationCreation(loan, action);
    }

    private CommandProcessingResult processMigrationCreation(Loan loan, LoanMigrationStatus action) {
        Optional<LoanMigration> existingMigration = loanMigrationRepository.findByLoanId(loan.getId());

        validateActionTransition(existingMigration, action);

        LoanMigration result;

        if (action == LoanMigrationStatus.MIGRATION_IN_PROGRESS) {
            result = handleNewMigration(loan);
        } else {
            result = handleExistingMigration(existingMigration.get(), action);
            businessEventNotifierService.notifyPostBusinessEvent(new LoanAccountSnapshotBusinessEvent(result.getLoan()));
        }

        return buildResponse(result);
    }

    private LoanMigration handleNewMigration(Loan loan) {
        return loanMigrationRepository.saveAndFlush(LoanMigration.newInstance(loan, OffsetDateTime.now(ZoneId.systemDefault()),
                LoanMigrationStatus.MIGRATION_IN_PROGRESS.name()));
    }

    private LoanMigration handleExistingMigration(LoanMigration migration, LoanMigrationStatus newStatus) {
        migration.setStatus(newStatus.name());
        migration.setMigrationEndDateTime(OffsetDateTime.now(ZoneId.systemDefault()));
        return loanMigrationRepository.saveAndFlush(migration);
    }

    private void validateActionTransition(Optional<LoanMigration> existingMigration, LoanMigrationStatus action) {
        if (action == LoanMigrationStatus.MIGRATION_IN_PROGRESS) {
            existingMigration.ifPresent(m -> {
                throw new GeneralPlatformDomainRuleException("migration.already.in.progress",
                        "Migration for loan %s already in progress".formatted(m.getLoan().getId()));
            });
        } else {
            LoanMigration migration = existingMigration.orElseThrow(() -> new GeneralPlatformDomainRuleException("migration.not.started",
                    "Migration must be started with MIGRATION_IN_PROGRESS status first"));

            if (!migration.getStatus().equals(LoanMigrationStatus.MIGRATION_IN_PROGRESS.name())) {
                throw new GeneralPlatformDomainRuleException("invalid.status.transition",
                        "Cannot transition from %s to %s".formatted(migration.getStatus(), action));
            }
        }
    }

    private CommandProcessingResult buildResponse(LoanMigration migration) {
        return new CommandProcessingResultBuilder().withEntityId(migration.getId()).withSubEntityId(migration.getLoan().getId())
                .withSubEntityExternalId(migration.getLoan().getExternalId()).build();
    }
}
