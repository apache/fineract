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
package org.apache.fineract.portfolio.fund.mvc.service;

import jakarta.persistence.PersistenceException;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.fineract.infrastructure.core.api.mvc.TypeCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.exception.ErrorHandler;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.fund.domain.Fund;
import org.apache.fineract.portfolio.fund.domain.FundRepository;
import org.apache.fineract.portfolio.fund.exception.FundNotFoundException;
import org.apache.fineract.portfolio.fund.mvc.data.PostFundsRequest;
import org.apache.fineract.portfolio.fund.mvc.data.PutFundsRequest;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
public class FundWritePlatformServiceJpaRepositoryImpl implements FundWritePlatformService {

    private final PlatformSecurityContext context;
    private final FundRepository fundRepository;

    @Transactional
    @Override
    @CacheEvict(value = "funds", key = "T(org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat('fn')")
    public CommandProcessingResult createFund(final TypeCommand<PostFundsRequest> command) {

        final PostFundsRequest request = command.getRequest();
        try {
            this.context.authenticatedUser();

            final Fund fund = new Fund();
            fund.setName(request.name());
            fund.setExternalId(request.externalId());

            this.fundRepository.saveAndFlush(fund);

            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(fund.getId()).build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handleFundDataIntegrityIssues(request.externalId(), request.name(), dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        } catch (final PersistenceException dve) {
            Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            handleFundDataIntegrityIssues(request.externalId(), request.name(), throwable, dve);
            return CommandProcessingResult.empty();
        }
    }

    @Transactional
    @Override
    @CacheEvict(value = "funds", key = "T(org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat('fn')")
    public CommandProcessingResult updateFund(final Long fundId, final TypeCommand<PutFundsRequest> command) {

        final PutFundsRequest request = command.getRequest();
        try {
            this.context.authenticatedUser();

            final Fund fund = this.fundRepository.findById(fundId).orElseThrow(() -> new FundNotFoundException(fundId));

            final Map<String, Object> changes = update(fund, command);
            if (!changes.isEmpty()) {
                this.fundRepository.saveAndFlush(fund);
            }

            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(fund.getId()).with(changes).build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handleFundDataIntegrityIssues(request.getExternalId().orElse(null), request.getName().orElse(null), dve.getMostSpecificCause(),
                    dve);
            return CommandProcessingResult.empty();
        } catch (final PersistenceException dve) {
            Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            handleFundDataIntegrityIssues(request.getExternalId().orElse(null), request.getName().orElse(null), throwable, dve);
            return CommandProcessingResult.empty();
        }
    }

    public Map<String, Object> update(final Fund fund, final TypeCommand<PutFundsRequest> command) {
        final PutFundsRequest request = command.getRequest();
        final Map<String, Object> actualChanges = new LinkedHashMap<>(7);

        if (request.getName() != null) {
            final String name = request.getName().orElse(null);
            if (command.differenceExists(fund.getName(), name)) {
                actualChanges.put(PutFundsRequest.Fields.name, name);
                fund.setName(name);
            }
        }

        if (request.getExternalId() != null) {
            final String externalId = request.getExternalId().orElse(null);
            if (command.differenceExists(fund.getExternalId(), externalId)) {
                actualChanges.put(PutFundsRequest.Fields.externalId, externalId);
                fund.setExternalId(externalId);
            }
        }
        return actualChanges;
    }

    /*
     * Guaranteed to throw an exception no matter what the data integrity issue is.
     */
    private void handleFundDataIntegrityIssues(final String externalId, final String name, final Throwable realCause, final Exception dve) {
        if (realCause.getMessage().contains("m_fund_external_id_key")) {
            throw new PlatformDataIntegrityException("error.msg.fund.duplicate.externalId",
                    "A fund with external id '" + externalId + "' already exists", "externalId", externalId);
        } else if (realCause.getMessage().contains("m_fund_external_id_name")) {
            throw new PlatformDataIntegrityException("error.msg.fund.duplicate.name", "A fund with name '" + name + "' already exists",
                    "name", name);
        }

        log.error("Error occurred.", dve);
        throw ErrorHandler.getMappable(dve, "error.msg.fund.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource: " + realCause.getMessage());
    }
}
