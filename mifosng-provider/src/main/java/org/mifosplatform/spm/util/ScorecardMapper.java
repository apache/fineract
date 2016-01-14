/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.spm.util;

import org.mifosplatform.organisation.staff.domain.Staff;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.spm.data.ScorecardData;
import org.mifosplatform.spm.data.ScorecardValue;
import org.mifosplatform.spm.domain.Question;
import org.mifosplatform.spm.domain.Response;
import org.mifosplatform.spm.domain.Scorecard;
import org.mifosplatform.spm.domain.Survey;
import org.mifosplatform.useradministration.domain.AppUser;

import java.util.*;

public class ScorecardMapper {

    private ScorecardMapper() {
        super();
    }

    public static List<ScorecardData> map(final List<Scorecard> scorecards) {
        final Map<Date, ScorecardData> scorecardDataMap = new HashMap<>();
        ScorecardData scorecardData = null;
        if (scorecards != null && scorecards.isEmpty()) {
            for (Scorecard scorecard : scorecards) {
                if ((scorecardData = scorecardDataMap.get(scorecard.getCreatedOn())) == null) {
                    scorecardData = new ScorecardData();
                    scorecardDataMap.put(scorecard.getCreatedOn(), scorecardData);
                    scorecardData.setUserId(scorecard.getAppUser().getId());
                    scorecardData.setClientId(scorecard.getClient().getId());
                    scorecardData.setCreatedOn(scorecard.getCreatedOn());
                    scorecardData.setScorecardValues(new ArrayList<ScorecardValue>());
                }

                scorecardData.getScorecardValues().add(new ScorecardValue(scorecard.getQuestion().getId(), scorecard.getResponse().getId(),
                        scorecard.getValue()));
            }

            return new ArrayList<>(scorecardDataMap.values());
        }

        return Collections.EMPTY_LIST;
    }

    public static List<Scorecard> map(final ScorecardData scorecardData, final Survey survey,
                                      final AppUser appUser, final Client client) {
        final List<Scorecard> scorecards = new ArrayList<>();

        final List<ScorecardValue> scorecardValues = scorecardData.getScorecardValues();

        if (scorecardValues != null) {
           for (ScorecardValue scorecardValue : scorecardValues) {
               final Scorecard scorecard = new Scorecard();
               scorecards.add(scorecard);
               scorecard.setSurvey(survey);
               ScorecardMapper.setQuestionAndResponse(scorecardValue, scorecard, survey);
               scorecard.setAppUser(appUser);
               scorecard.setClient(client);
               scorecard.setCreatedOn(scorecardData.getCreatedOn());
               scorecard.setValue(scorecardValue.getValue());
           }
        }
        return scorecards;
    }

    private static void setQuestionAndResponse(final ScorecardValue scorecardValue, final Scorecard scorecard,
                                        final Survey survey) {
        final List<Question> questions = survey.getQuestions();
        for (final Question question : questions) {
            if (question.getId().equals(scorecardValue.getQuestionId())) {
                scorecard.setQuestion(question);
                for (final Response response : question.getResponses()) {
                    if (response.getId().equals(scorecardValue.getResponseId())) {
                        scorecard.setResponse(response);
                        break;
                    }
                }
                break;
            }
        }
    }
}
