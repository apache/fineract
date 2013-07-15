/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.service;

import java.util.Map;

import org.mifosplatform.accounting.producttoaccountmapping.service.ProductToGLAccountMappingWritePlatformService;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.savings.data.SavingsProductDataValidator;
import org.mifosplatform.portfolio.savings.domain.SavingsProduct;
import org.mifosplatform.portfolio.savings.domain.SavingsProductAssembler;
import org.mifosplatform.portfolio.savings.domain.SavingsProductRepository;
import org.mifosplatform.portfolio.savings.exception.SavingsProductNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.mifosplatform.portfolio.savings.SavingsApiConstants.accountingRuleParamName;

@Service
public class SavingsProductWritePlatformServiceJpaRepositoryImpl implements SavingsProductWritePlatformService {

    private final Logger logger;
    private final PlatformSecurityContext context;
    private final SavingsProductRepository savingProductRepository;
    private final SavingsProductDataValidator fromApiJsonDataValidator;
    private final SavingsProductAssembler savingsProductAssembler;
    private final ProductToGLAccountMappingWritePlatformService accountMappingWritePlatformService;

    @Autowired
    public SavingsProductWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context,
            final SavingsProductRepository savingProductRepository, final SavingsProductDataValidator fromApiJsonDataValidator,
            final SavingsProductAssembler savingsProductAssembler,
            final ProductToGLAccountMappingWritePlatformService accountMappingWritePlatformService) {
        this.context = context;
        this.savingProductRepository = savingProductRepository;
        this.fromApiJsonDataValidator = fromApiJsonDataValidator;
        this.savingsProductAssembler = savingsProductAssembler;
        this.logger = LoggerFactory.getLogger(SavingsProductWritePlatformServiceJpaRepositoryImpl.class);
        this.accountMappingWritePlatformService = accountMappingWritePlatformService;
    }

    /*
     * Guaranteed to throw an exception no matter what the data integrity issue
     * is.
     */
    private void handleDataIntegrityIssues(final JsonCommand command, final DataAccessException dae) {

        final Throwable realCause = dae.getMostSpecificCause();
        if (realCause.getMessage().contains("sp_unq_name")) {

            final String name = command.stringValueOfParameterNamed("name");
            throw new PlatformDataIntegrityException("error.msg.product.savings.duplicate.name", "Savings product with name `" + name
                    + "` already exists", "name", name);
        }

        logAsErrorUnexpectedDataIntegrityException(dae);
        throw new PlatformDataIntegrityException("error.msg.savingsproduct.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource.");
    }

    private void logAsErrorUnexpectedDataIntegrityException(final DataAccessException dae) {
        logger.error(dae.getMessage(), dae);
    }

    @Transactional
    @Override
    public CommandProcessingResult create(final JsonCommand command) {

        try {
            this.fromApiJsonDataValidator.validateForCreate(command.json());

            final SavingsProduct product = this.savingsProductAssembler.assemble(command);

            this.savingProductRepository.save(product);

            // save accounting mappings
            accountMappingWritePlatformService.createSavingProductToGLAccountMapping(product.getId(), command);

            return new CommandProcessingResultBuilder() //
                    .withEntityId(product.getId()) //
                    .build();
        } catch (DataAccessException e) {
            handleDataIntegrityIssues(command, e);
            return CommandProcessingResult.empty();
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult update(final Long productId, final JsonCommand command) {

        try {
            this.context.authenticatedUser();
            this.fromApiJsonDataValidator.validateForUpdate(command.json());

            final SavingsProduct product = this.savingProductRepository.findOne(productId);
            if (product == null) { throw new SavingsProductNotFoundException(productId); }

            final Map<String, Object> changes = product.update(command);

            // accounting related changes
            boolean accountingTypeChanged = changes.containsKey(accountingRuleParamName);
            final Map<String, Object> accountingMappingChanges = accountMappingWritePlatformService.updateSavingsProductToGLAccountMapping(
                    product.getId(), command, accountingTypeChanged, product.getAccountingType());
            changes.putAll(accountingMappingChanges);

            if (!changes.isEmpty()) {
                this.savingProductRepository.saveAndFlush(product);
            }

            return new CommandProcessingResultBuilder() //
                    .withEntityId(product.getId()) //
                    .with(changes).build();
        } catch (DataAccessException e) {
            handleDataIntegrityIssues(command, e);
            return CommandProcessingResult.empty();
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult delete(final Long productId) {

        this.context.authenticatedUser();
        final SavingsProduct product = this.savingProductRepository.findOne(productId);
        if (product == null) { throw new SavingsProductNotFoundException(productId); }

        this.savingProductRepository.delete(product);

        return new CommandProcessingResultBuilder() //
                .withEntityId(product.getId()) //
                .build();
    }
}