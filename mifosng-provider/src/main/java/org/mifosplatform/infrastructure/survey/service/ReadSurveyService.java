package org.mifosplatform.infrastructure.survey.service;

import org.mifosplatform.infrastructure.dataqueries.data.DatatableData;
import org.mifosplatform.infrastructure.dataqueries.data.GenericResultsetData;
import org.mifosplatform.infrastructure.survey.data.ClientScoresOverview;
import org.mifosplatform.infrastructure.survey.data.SurveyDataTableData;

import java.util.List;

/**
 * Created by Cieyou on 2/27/14.
 */
public interface ReadSurveyService {

    List<SurveyDataTableData> retrieveAllSurveys();

    SurveyDataTableData retrieveSurvey(String surveyName);

    List<ClientScoresOverview>retrieveClientSurveyScoreOverview(String surveyName, Long clientId);

    GenericResultsetData retrieveSurveyEntry(String surveyName, Long clientId, Long entryId);

}


