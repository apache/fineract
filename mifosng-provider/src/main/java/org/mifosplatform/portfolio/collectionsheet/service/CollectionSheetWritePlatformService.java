package org.mifosplatform.portfolio.collectionsheet.service;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;

public interface CollectionSheetWritePlatformService {
    
    CommandProcessingResult updateCollectionSheet(JsonCommand command);
}
