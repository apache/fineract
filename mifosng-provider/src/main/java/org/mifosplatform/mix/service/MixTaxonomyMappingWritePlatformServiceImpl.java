/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.mix.service;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.mix.domain.MixTaxonomyMapping;
import org.mifosplatform.mix.domain.MixTaxonomyMappingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MixTaxonomyMappingWritePlatformServiceImpl implements MixTaxonomyMappingWritePlatformService {

    private final MixTaxonomyMappingRepository mappingRepository;

    @Autowired
    public MixTaxonomyMappingWritePlatformServiceImpl(final MixTaxonomyMappingRepository mappingRepository) {
        this.mappingRepository = mappingRepository;
    }

    @Transactional
    @Override
    public CommandProcessingResult updateMapping(final Long mappingId, final JsonCommand command) {
        try {
            MixTaxonomyMapping mapping = this.mappingRepository.findOne(mappingId);
            if (mapping == null) {
                mapping = MixTaxonomyMapping.fromJson(command);
            } else {
                mapping.update(command);
            }

            this.mappingRepository.saveAndFlush(mapping);

            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(mapping.getId()).build();

        } catch (final DataIntegrityViolationException dve) {
            return CommandProcessingResult.empty();
        }
    }
}