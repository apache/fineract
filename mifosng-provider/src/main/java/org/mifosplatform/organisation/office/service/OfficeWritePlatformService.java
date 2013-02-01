package org.mifosplatform.organisation.office.service;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;

public interface OfficeWritePlatformService {

    CommandProcessingResult createOffice(JsonCommand command);

    CommandProcessingResult updateOffice(Long officeId, JsonCommand command);

    CommandProcessingResult officeTransaction(JsonCommand command);

    CommandProcessingResult deleteOfficeTransaction(Long id, JsonCommand command);
}