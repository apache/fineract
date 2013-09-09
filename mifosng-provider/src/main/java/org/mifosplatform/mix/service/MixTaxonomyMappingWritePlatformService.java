package org.mifosplatform.mix.service;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;

public interface MixTaxonomyMappingWritePlatformService {

    CommandProcessingResult updateMapping(Long mappingId, JsonCommand command);
}
