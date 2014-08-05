/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.survey.service;

import java.util.List;

import org.mifosplatform.infrastructure.dataqueries.data.GenericResultsetData;
import org.mifosplatform.infrastructure.survey.data.ClientScoresOverview;
import org.mifosplatform.infrastructure.survey.data.SurveyDataTableData;

/**
 * Created by Cieyou on 2/27/14.
 */
public interface ReadSurveyService {

    List<SurveyDataTableData> retrieveAllSurveys();

    SurveyDataTableData retrieveSurvey(String surveyName);

    List<ClientScoresOverview> retrieveClientSurveyScoreOverview(String surveyName, Long clientId);

    List<ClientScoresOverview> retrieveClientSurveyScoreOverview(Long clientId);

    GenericResultsetData retrieveSurveyEntry(String surveyName, Long clientId, Long entryId);

}
