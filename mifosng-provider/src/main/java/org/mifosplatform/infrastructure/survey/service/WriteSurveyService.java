package org.mifosplatform.infrastructure.survey.service;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;

/**
 * Created by Cieyou on 3/13/14.
 */
public interface WriteSurveyService {

    CommandProcessingResult registerSurvey(JsonCommand command);

    CommandProcessingResult fullFillSurvey(String datatable, Long appTableId, JsonCommand command);
}
