/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savingsdepositproduct.service;

import java.util.Map;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.loanproduct.domain.PeriodFrequencyType;
import org.mifosplatform.portfolio.savingsdepositproduct.domain.DepositProduct;
import org.mifosplatform.portfolio.savingsdepositproduct.domain.DepositProductRepository;
import org.mifosplatform.portfolio.savingsdepositproduct.exception.DepositProductNotFoundException;
import org.mifosplatform.portfolio.savingsdepositproduct.serialization.DepositProductCommandFromApiJsonDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DepositProductWritePlatformServiceJpaRepositoryImpl implements DepositProductWritePlatformService {

    private final static Logger logger = LoggerFactory.getLogger(DepositProductWritePlatformServiceJpaRepositoryImpl.class);

    private final PlatformSecurityContext context;
    private final DepositProductRepository depositProductRepository;
    private final DepositProductCommandFromApiJsonDeserializer fromApiJsonDeserializer;

    @Autowired
    public DepositProductWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context,
            final DepositProductRepository depositProductRepository,
            final DepositProductCommandFromApiJsonDeserializer fromApiJsonDeserializer) {
        this.context = context;
        this.depositProductRepository = depositProductRepository;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
    }

    /*
     * Guaranteed to throw an exception no matter what the data integrity issue
     * is.
     */
    private void handleDataIntegrityIssues(final JsonCommand command, final DataIntegrityViolationException dve) {

        Throwable realCause = dve.getMostSpecificCause();
        if (realCause.getMessage().contains("name_deposit_product")) {
            final String name = command.stringValueOfParameterNamed("name");
            throw new PlatformDataIntegrityException("error.msg.desposit.product.duplicate.name", "Deposit product with name: " + name
                    + " already exists", "name", name);
        }
        if (realCause.getMessage().contains("externalid_deposit_product")) {
            final String externalId = command.stringValueOfParameterNamed("externalId");
            throw new PlatformDataIntegrityException("error.msg.desposit.product.duplicate.externalId", "Deposit product with externalId "
                    + externalId + " already exists", "externalId", externalId);
        }

        logger.error(dve.getMessage(), dve);
        throw new PlatformDataIntegrityException("error.msg.deposit.product.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource.");
    }

    @Transactional
    @Override
    public CommandProcessingResult createDepositProduct(final JsonCommand command) {
        try {
            this.context.authenticatedUser();
            this.fromApiJsonDeserializer.validateForCreate(command.json());

            final Integer interestCompoundingPeriodTypeIntValue = command.integerValueOfParameterNamed("interestCompoundedEveryPeriodType");
            PeriodFrequencyType interestCompoundingPeriodType = PeriodFrequencyType.fromInt(interestCompoundingPeriodTypeIntValue);

            final Integer lockinPeriodTypeIntValue = command.integerValueOfParameterNamed("lockinPeriodType");
            PeriodFrequencyType lockinPeriodType = PeriodFrequencyType.fromInt(lockinPeriodTypeIntValue);

            DepositProduct product = DepositProduct.assembleFromJson(command, interestCompoundingPeriodType, lockinPeriodType);

            this.depositProductRepository.save(product);

            return new CommandProcessingResultBuilder() //
                    .withEntityId(product.getId()) //
                    .build();
        } catch (DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve);
            return CommandProcessingResult.empty();
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult updateDepositProduct(final Long productId, final JsonCommand command) {
        try {

            this.context.authenticatedUser();

            this.fromApiJsonDeserializer.validateForUpdate(command.json());

            DepositProduct product = this.depositProductRepository.findOne(productId);
            if (product == null || product.isDeleted()) { throw new DepositProductNotFoundException(productId); }

            final Map<String, Object> changes = product.update(command);

            if (!changes.isEmpty()) {
                this.depositProductRepository.saveAndFlush(product);
            }

            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withEntityId(productId) //
                    .with(changes) //
                    .build();

        } catch (DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve);
            return CommandProcessingResult.empty();
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult deleteDepositProduct(final Long productId) {

        this.context.authenticatedUser();
        DepositProduct product = this.depositProductRepository.findOne(productId);
        if (product == null || product.isDeleted()) { throw new DepositProductNotFoundException(productId); }
        product.delete();

        this.depositProductRepository.save(product);

        return new CommandProcessingResultBuilder() //
                .withEntityId(product.getId()) //
                .build();
    }
}