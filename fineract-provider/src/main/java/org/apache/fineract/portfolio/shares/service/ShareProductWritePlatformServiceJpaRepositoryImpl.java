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
package org.apache.fineract.portfolio.shares.service;

import java.util.Map;

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.portfolio.products.exception.ProductNotFoundException;
import org.apache.fineract.portfolio.shares.domain.ShareProduct;
import org.apache.fineract.portfolio.shares.domain.ShareProductRepository;
import org.apache.fineract.portfolio.shares.domain.ShareProductTempRepository;
import org.apache.fineract.portfolio.shares.serialization.ShareProductDataSerializer;
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
