/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.spm.util;

import org.mifosplatform.organisation.staff.domain.Staff;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.spm.data.ScorecardData;
import org.mifosplatform.spm.domain.Question;
import org.mifosplatform.spm.domain.Response;
import org.mifosplatform.spm.domain.Scorecard;
import org.mifosplatform.spm.domain.Survey;

import java.util.List;

public class ScorecardMapper {

    private ScorecardMapper() {
        super();
    }

    public static ScorecardData map(final Scorecard scorecard) {
        final ScorecardData scorecardData = new ScorecardData(
                scorecard.getQuestion().getId(), scorecard.getResponse().getId(), scorecard.getStaff().getId(),
                scorecard.getClient().getId(), scorecard.getCreatedOn(), scorecard.getValue()
        );
        return scorecardData;
    }

    public static Scorecard map(final ScorecardData scorecardData, final Survey survey,
                                final Staff staff, final Client client) {
        final Scorecard scorecard = new Scorecard();
        ScorecardMapper.setQuestionAndResponse(scorecardData, scorecard, survey);
        scorecard.setStaff(staff);
        scorecard.setClient(client);
        scorecard.setCreatedOn(scorecardData.getCreatedOn());
        scorecard.setValue(scorecardData.getValue());
        return scorecard;
    }

    private static void setQuestionAndResponse(final ScorecardData scorecardData, final Scorecard scorecard,
                                        final Survey survey) {
        final List<Question> questions = survey.getQuestions();
        for (final Question question : questions) {
            if (question.getId().equals(scorecardData.getQuestionId())) {
                scorecard.setQuestion(question);
                for (final Response response : question.getResponses()) {
                    if (response.getId().equals(scorecardData.getResponseId())) {
                        scorecard.setResponse(response);
                        break;
                    }
                }
                break;
            }
        }
    }
}
