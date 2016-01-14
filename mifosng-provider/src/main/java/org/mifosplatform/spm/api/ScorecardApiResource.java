/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.spm.api;

import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.client.domain.ClientRepository;
import org.mifosplatform.portfolio.client.exception.ClientNotFoundException;
import org.mifosplatform.spm.data.ScorecardData;
import org.mifosplatform.spm.domain.Scorecard;
import org.mifosplatform.spm.domain.Survey;
import org.mifosplatform.spm.exception.SurveyNotFoundException;
import org.mifosplatform.spm.service.ScorecardService;
import org.mifosplatform.spm.service.SpmService;
import org.mifosplatform.spm.util.ScorecardMapper;
import org.mifosplatform.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Collections;
import java.util.List;

@Path("/surveys/{surveyId}/scorecards")
@Component
@Scope("singleton")
public class ScorecardApiResource {

    private final PlatformSecurityContext securityContext;
    private final SpmService spmService;
    private final ScorecardService scorecardService;
    private final ClientRepository clientRepository;

    @Autowired
    public ScorecardApiResource(final PlatformSecurityContext securityContext, final SpmService spmService,
                                final ScorecardService scorecardService, final ClientRepository clientRepository) {
        super();
        this.securityContext = securityContext;
        this.spmService = spmService;
        this.scorecardService = scorecardService;
        this.clientRepository = clientRepository;
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Transactional
    public List<ScorecardData> findBySurvey(@PathParam("surveyId") final Long surveyId) {
        this.securityContext.authenticatedUser();

        final Survey survey = findSurvey(surveyId);

        final List<Scorecard> scorecards = this.scorecardService.findBySurvey(survey);

        if (scorecards == null) {
            return ScorecardMapper.map(scorecards);
        }

        return Collections.EMPTY_LIST;
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Transactional
    public void createScorecard(@PathParam("surveyId") final Long surveyId, final ScorecardData scorecardData) {
        final AppUser appUser = this.securityContext.authenticatedUser();

        final Survey survey = findSurvey(surveyId);

        final Client client = this.clientRepository.findOne(scorecardData.getClientId());

        if (client == null) {
            throw new ClientNotFoundException(scorecardData.getClientId());
        }

        this.scorecardService.createScorecard(ScorecardMapper.map(scorecardData, survey, appUser, client));
    }

    @Path("/clients/{clientId}")
    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Transactional
    public List<ScorecardData> findBySurveyClient(@PathParam("surveyId") final Long surveyId,
                                                  @PathParam("clientId") final Long clientId) {
        this.securityContext.authenticatedUser();

        final Survey survey = findSurvey(surveyId);

        final Client client = this.clientRepository.findOne(clientId);

        if (client == null) {
            throw new ClientNotFoundException(clientId);
        }

        final List<Scorecard> scorecards = this.scorecardService.findBySurveyAndClient(survey, client);

        if (scorecards == null) {
            return ScorecardMapper.map(scorecards);
        }

        return Collections.EMPTY_LIST;
    }

    private Survey findSurvey(final Long surveyId) {
        final Survey survey = this.spmService.findById(surveyId);
        if (survey == null) {
            throw new SurveyNotFoundException(surveyId);
        }
        return survey;
    }
}
