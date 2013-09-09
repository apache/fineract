package org.mifosplatform.xbrl.mapping.service;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.xbrl.mapping.domain.TaxonomyMapping;
import org.mifosplatform.xbrl.mapping.domain.TaxonomyMappingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WriteTaxonomyMappingServiceImpl implements WriteTaxonomyMappingService {

    private final PlatformSecurityContext context;
    private final TaxonomyMappingRepository mappingRepository;

    @Autowired
    public WriteTaxonomyMappingServiceImpl(final PlatformSecurityContext context, final TaxonomyMappingRepository mappingRepository) {
        this.context = context;
        this.mappingRepository = mappingRepository;
    }

    @Transactional
    @Override
    public CommandProcessingResult updateMapping(final Long mappingId, final JsonCommand command) {
        try {
            context.authenticatedUser();

            TaxonomyMapping mapping = this.mappingRepository.findOne(mappingId);
            if (mapping == null) {
                mapping = TaxonomyMapping.fromJson(command);
            } else {
                mapping.update(command);
            }

            this.mappingRepository.saveAndFlush(mapping);

            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(mapping.getId()).build();

        } catch (DataIntegrityViolationException dve) {
            return CommandProcessingResult.empty();
        }
    }

}
