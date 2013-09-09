package org.mifosplatform.xbrl.mapping.service;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;

public interface WriteTaxonomyMappingService {

    CommandProcessingResult updateMapping(Long mappingId, JsonCommand command);
}
