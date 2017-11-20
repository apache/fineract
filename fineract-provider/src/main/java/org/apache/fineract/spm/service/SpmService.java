/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.spm.service;

import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.spm.domain.Survey;
import org.apache.fineract.spm.domain.SurveyValidator;
import org.apache.fineract.spm.exception.SurveyNotFoundException;
import org.apache.fineract.spm.repository.SurveyRepository;
import org.apache.openjpa.persistence.EntityExistsException;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.PersistenceException;

@Service
public class SpmService {

    private final PlatformSecurityContext securityContext;
    private final SurveyRepository surveyRepository;
    private final SurveyValidator surveyValidator;

    @Autowired
    public SpmService(final PlatformSecurityContext securityContext,
                      final SurveyRepository surveyRepository,
                      final SurveyValidator surveyValidator) {
        super();
        this.securityContext = securityContext;
        this.surveyRepository = surveyRepository;
        this.surveyValidator = surveyValidator;
    }

    public List<Survey> fetchValidSurveys() {
        this.securityContext.authenticatedUser();

        return this.surveyRepository.fetchActiveSurveys(new Date());
    }
    
    public List<Survey> fetchAllSurveys() {
        this.securityContext.authenticatedUser();

        return this.surveyRepository.fetchAllSurveys();
    }

    public Survey findById(final Long id) {
        this.securityContext.authenticatedUser();
        Survey survey = this.surveyRepository.findOne(id);
        if (survey == null) {
            throw new SurveyNotFoundException(id);
        }
        return survey;
    }

    public Survey createSurvey(final Survey survey) {
        this.securityContext.authenticatedUser();
        this.surveyValidator.validate(survey);
        final Survey previousSurvey = this.surveyRepository.findByKey(survey.getKey(), new Date());

        if (previousSurvey != null) {
            this.deactivateSurvey(previousSurvey.getId());
        }
        // set valid from to start of today
        LocalDate validFrom = DateUtils.getLocalDateOfTenant() ;
        // set valid to for 100 years
        Calendar cal = Calendar.getInstance() ;
        cal.setTime(validFrom.toDate());
        cal.add(Calendar.YEAR, 100); 
        survey.setValidFrom(validFrom.toDate());
        survey.setValidTo(cal.getTime());
        try {
            this.surveyRepository.saveAndFlush(survey);
        } catch (final EntityExistsException dve) {
            handleDataIntegrityIssues(dve, dve, survey.getKey());
        } catch (final DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(dve.getMostSpecificCause(), dve, survey.getKey());
        } catch (final JpaSystemException dve) {
            handleDataIntegrityIssues(dve.getMostSpecificCause(), dve, survey.getKey());
        } catch (final PersistenceException dve) {
            handleDataIntegrityIssues(dve, dve, survey.getKey());
        }
        return survey ;
    }
    
    public Survey updateSurvey(final Survey survey) {
        try {
            this.surveyValidator.validate(survey);
            this.surveyRepository.saveAndFlush(survey);
        } catch (final EntityExistsException dve) {
            handleDataIntegrityIssues(dve, dve, survey.getKey());
        } catch (final DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(dve.getMostSpecificCause(), dve, survey.getKey());
        } catch (final JpaSystemException dve) {
            handleDataIntegrityIssues(dve.getMostSpecificCause(), dve, survey.getKey());
        } catch (final PersistenceException dve) {
            handleDataIntegrityIssues(dve, dve, survey.getKey());
        }
        return survey;
    }

    public void deactivateSurvey(final Long id) {
        this.securityContext.authenticatedUser();

        final Survey survey = findById(id);
        final DateTime dateTime = getStartOfToday().minusMillis(1);
        survey.setValidTo(dateTime.toDate());

        this.surveyRepository.save(survey);
    }
    
    public void activateSurvey(final Long id) {
        this.securityContext.authenticatedUser();

        final Survey survey = findById(id);
        LocalDate validFrom = DateUtils.getLocalDateOfTenant() ;
        Calendar cal = Calendar.getInstance() ;
        cal.setTime(validFrom.toDate());
        cal.add(Calendar.YEAR, 100);
        survey.setValidFrom(validFrom.toDate());
        survey.setValidTo(cal.getTime());

        this.surveyRepository.save(survey);
    }
    
    
    public static DateTime getStartOfToday() {
        return DateTime.now().withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
    }
    
    private void handleDataIntegrityIssues(final Throwable realCause, final Exception dve, String key) {

        if (realCause.getMessage().contains("m_survey_scorecards")) { throw new PlatformDataIntegrityException(
                "error.msg.survey.cannot.be.modified.as.used.in.client.survey",
                "Survey can not be edited as it is already used in client survey", "name", key); }

        if (realCause.getMessage().contains("key")) { throw new PlatformDataIntegrityException("error.msg.survey.duplicate.key",
                "Survey with key already exists", "name", key); }

        throw new PlatformDataIntegrityException("error.msg.survey.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource: " + realCause.getMessage());
    }
}
