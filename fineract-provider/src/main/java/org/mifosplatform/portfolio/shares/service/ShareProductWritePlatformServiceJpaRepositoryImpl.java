/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.shares.service;

import java.util.Map;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.portfolio.products.exception.ProductNotFoundException;
import org.mifosplatform.portfolio.shares.domain.ShareProduct;
import org.mifosplatform.portfolio.shares.domain.ShareProductRepository;
import org.mifosplatform.portfolio.shares.domain.ShareProductTempRepository;
import org.mifosplatform.portfolio.shares.serialization.ShareProductDataSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class ShareProductWritePlatformServiceJpaRepositoryImpl implements ShareProductWritePlatformService {

    private final ShareProductRepository repository;
    private final ShareProductDataSerializer serializer;
    
    @Autowired
    public ShareProductWritePlatformServiceJpaRepositoryImpl(final ShareProductRepository repository,
            final ShareProductDataSerializer serializer) {
        this.repository = repository;
        this.serializer = serializer;
    }

    @Override
    public CommandProcessingResult createShareProduct(JsonCommand jsonCommand) {
        try {
            ShareProduct product = this.serializer.validateAndCreate(jsonCommand);
            //this.repository.save(product);
            ShareProductTempRepository.getInstance().save(product) ;
            return new CommandProcessingResultBuilder() //
                    .withCommandId(jsonCommand.commandId()) //
                    .withEntityId(product.getId()) //
                    .build();
        } catch (final DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(jsonCommand, dve);
            return CommandProcessingResult.empty();
        }

    }

    @Override
    public CommandProcessingResult updateProduct(Long productId, JsonCommand jsonCommand) {
        try {
            //ShareProduct product = this.repository.findOne(productId);
            ShareProduct product = ShareProductTempRepository.getInstance().fineOne(productId) ;
            if (product == null) { throw new ProductNotFoundException(productId, "share"); }
            final Map<String, Object> changes = this.serializer.validateAndUpdate(jsonCommand, product);
            if(!changes.isEmpty()) {
                //this.repository.saveAndFlush(product) ;
            }
            return new CommandProcessingResultBuilder() //
                    .withCommandId(jsonCommand.commandId()) //
                    .withEntityId(productId) //
                    .with(changes) //
                    .build();
        } catch (DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(jsonCommand, dve);
            return CommandProcessingResult.empty();
        }
    }

    private void handleDataIntegrityIssues(final JsonCommand command, final DataIntegrityViolationException dve) {

    }

}
