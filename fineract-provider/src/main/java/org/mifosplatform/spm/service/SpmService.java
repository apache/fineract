/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.spm.service;

import org.joda.time.DateTime;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.spm.domain.Survey;
import org.mifosplatform.spm.repository.SurveyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class SpmService {

    private final PlatformSecurityContext securityContext;
    private final SurveyRepository surveyRepository;

    @Autowired
    public SpmService(final PlatformSecurityContext securityContext,
                      final SurveyRepository surveyRepository) {
        super();
        this.securityContext = securityContext;
        this.surveyRepository = surveyRepository;
    }

    public List<Survey> fetchValidSurveys() {
        this.securityContext.authenticatedUser();

        return this.surveyRepository.fetchActiveSurveys(new Date());
    }

    public Survey findById(final Long id) {
        this.securityContext.authenticatedUser();

        return this.surveyRepository.findOne(id);
    }

    public Survey createSurvey(final Survey survey) {
        this.securityContext.authenticatedUser();

        final Survey previousSurvey = this.surveyRepository.findByKey(survey.getKey(), new Date());

        if (previousSurvey != null) {
            this.deactivateSurvey(previousSurvey.getId());
        }

        // set valid from to start of today
        final DateTime validFrom = DateTime
                .now()
                .withHourOfDay(0)
                .withMinuteOfHour(0)
                .withSecondOfMinute(0)
                .withMillisOfSecond(0);

        survey.setValidFrom(validFrom.toDate());

        // set valid from to end in 100 years
        final DateTime validTo = validFrom
                .withDayOfMonth(31)
                .withMonthOfYear(12)
                .withHourOfDay(23)
                .withMinuteOfHour(59)
                .withSecondOfMinute(59)
                .withMillisOfSecond(999)
                .plusYears(100);

        survey.setValidTo(validTo.toDate());

        return this.surveyRepository.save(survey);
    }

    public void deactivateSurvey(final Long id) {
        this.securityContext.authenticatedUser();

        final Survey survey = this.surveyRepository.findOne(id);

        if (survey != null) {
            // set valid to to yesterday night
            final DateTime dateTime = DateTime
                    .now()
                    .withHourOfDay(23)
                    .withMinuteOfHour(59)
                    .withSecondOfMinute(59)
                    .withMillisOfSecond(999)
                    .minusDays(1);
            survey.setValidTo(dateTime.toDate());

            this.surveyRepository.save(survey);
        }
    }
}
