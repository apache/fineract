package org.mifosplatform.infrastructure.survey.service;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;

/**
 * Created by Cieyou on 3/12/14.
 */
public interface WriteLikelihoodService {

    CommandProcessingResult update(Long likelihoodId, JsonCommand command);
}
