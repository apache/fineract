/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.spm.service;

import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.spm.domain.Scorecard;
import org.mifosplatform.spm.domain.Survey;
import org.mifosplatform.spm.repository.ScorecardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ScorecardService {

    private final PlatformSecurityContext securityContext;
    private final ScorecardRepository scorecardRepository;

    @Autowired
    public ScorecardService(final PlatformSecurityContext securityContext,
                            final ScorecardRepository scorecardRepository) {
        super();
        this.securityContext = securityContext;
        this.scorecardRepository = scorecardRepository;
    }

    @Transactional
    public Scorecard createScorecard(final Scorecard scorecard) {
        this.securityContext.authenticatedUser();

        return this.scorecardRepository.save(scorecard);
    }

    public List<Scorecard> findBySurvey(final Survey survey) {
        this.securityContext.authenticatedUser();

        return this.scorecardRepository.findBySurvey(survey);
    }

    public List<Scorecard> findBySurveyAndClient(final Survey survey, final Client client) {
        this.securityContext.authenticatedUser();

        return this.scorecardRepository.findBySurveyAndClient(survey, client);
    }
}
