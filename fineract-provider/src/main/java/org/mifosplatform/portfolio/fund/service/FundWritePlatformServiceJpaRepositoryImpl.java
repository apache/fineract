/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.fund.service;

import java.util.Map;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.fund.domain.Fund;
import org.mifosplatform.portfolio.fund.domain.FundRepository;
import org.mifosplatform.portfolio.fund.exception.FundNotFoundException;
import org.mifosplatform.portfolio.fund.serialization.FundCommandFromApiJsonDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FundWritePlatformServiceJpaRepositoryImpl implements FundWritePlatformService {

    private final static Logger logger = LoggerFactory.getLogger(FundWritePlatformServiceJpaRepositoryImpl.class);

    private final PlatformSecurityContext context;
    private final FundCommandFromApiJsonDeserializer fromApiJsonDeserializer;
    private final FundRepository fundRepository;

    @Autowired
    public FundWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context,
            final FundCommandFromApiJsonDeserializer fromApiJsonDeserializer, final FundRepository fundRepository) {
        this.context = context;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.fundRepository = fundRepository;
    }

    @Transactional
    @Override
    @CacheEvict(value = "funds", key = "T(org.mifosplatform.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat('fn')")
    public CommandProcessingResult createFund(final JsonCommand command) {

        try {
            this.context.authenticatedUser();

            this.fromApiJsonDeserializer.validateForCreate(command.json());

            final Fund fund = Fund.fromJson(command);

            this.fundRepository.save(fund);

            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(fund.getId()).build();
        } catch (final DataIntegrityViolationException dve) {
            handleFundDataIntegrityIssues(command, dve);
            return CommandProcessingResult.empty();
        }
    }

    @Transactional
    @Override
    @CacheEvict(value = "funds", key = "T(org.mifosplatform.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat('fn')")
    public CommandProcessingResult updateFund(final Long fundId, final JsonCommand command) {

        try {
            this.context.authenticatedUser();

            this.fromApiJsonDeserializer.validateForUpdate(command.json());

            final Fund fund = this.fundRepository.findOne(fundId);
            if (fund == null) { throw new FundNotFoundException(fundId); }

            final Map<String, Object> changes = fund.update(command);
            if (!changes.isEmpty()) {
                this.fundRepository.saveAndFlush(fund);
            }

            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(fund.getId()).with(changes).build();
        } catch (final DataIntegrityViolationException dve) {
            handleFundDataIntegrityIssues(command, dve);
            return CommandProcessingResult.empty();
        }
    }

    /*
     * Guaranteed to throw an exception no matter what the data integrity issue
     * is.
     */
    private void handleFundDataIntegrityIssues(final JsonCommand command, final DataIntegrityViolationException dve) {

        final Throwable realCause = dve.getMostSpecificCause();
        if (realCause.getMessage().contains("fund_externalid_org")) {
            final String externalId = command.stringValueOfParameterNamed("externalId");
            throw new PlatformDataIntegrityException("error.msg.fund.duplicate.externalId", "A fund with external id '" + externalId
                    + "' already exists", "externalId", externalId);
        } else if (realCause.getMessage().contains("fund_name_org")) {
            final String name = command.stringValueOfParameterNamed("name");
            throw new PlatformDataIntegrityException("error.msg.fund.duplicate.name", "A fund with name '" + name + "' already exists",
                    "name", name);
        }

        logger.error(dve.getMessage(), dve);
        throw new PlatformDataIntegrityException("error.msg.fund.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource: " + realCause.getMessage());
    }
}