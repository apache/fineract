/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
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
