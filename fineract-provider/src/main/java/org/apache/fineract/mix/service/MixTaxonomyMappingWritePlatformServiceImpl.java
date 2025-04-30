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
package org.apache.fineract.mix.service;

import lombok.RequiredArgsConstructor;
import org.apache.fineract.command.core.Command;
import org.apache.fineract.mix.data.MixTaxonomyMappingRequest;
import org.apache.fineract.mix.data.MixTaxonomyMappingResponse;
import org.apache.fineract.mix.domain.MixTaxonomyMapping;
import org.apache.fineract.mix.domain.MixTaxonomyMappingRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class MixTaxonomyMappingWritePlatformServiceImpl implements MixTaxonomyMappingWritePlatformService {

    private final MixTaxonomyMappingRepository mappingRepository;

    @Transactional
    @Override
    public MixTaxonomyMappingResponse updateMapping(Command<MixTaxonomyMappingRequest> command) {
        Long mappingId = command.getPayload().getMappingId();

        try {
            MixTaxonomyMapping mapping = this.mappingRepository.findById(mappingId).orElse(null);
            if (mapping == null) {
                mapping = new MixTaxonomyMapping();
            }
            mapping.setIdentifier(command.getPayload().getIdentifier());
            mapping.setConfig(command.getPayload().getConfig());
            mapping.setCurrency(command.getPayload().getCurrency());

            MixTaxonomyMapping result = this.mappingRepository.saveAndFlush(mapping);

            return new MixTaxonomyMappingResponse(result.getIdentifier(), result.getConfig());
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            return new MixTaxonomyMappingResponse();
        }
    }
}
