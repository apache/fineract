/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.spm.api;

import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.spm.data.SurveyData;
import org.mifosplatform.spm.domain.Survey;
import org.mifosplatform.spm.exception.SurveyNotFoundException;
import org.mifosplatform.spm.service.SpmService;
import org.mifosplatform.spm.util.SurveyMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

@Path("/surveys")
@Component
@Scope("singleton")
public class SpmApiResource {

    private final PlatformSecurityContext securityContext;
    private final SpmService spmService;

    @Autowired
    public SpmApiResource(final PlatformSecurityContext securityContext,
                          final SpmService spmService) {
        this.securityContext = securityContext;
        this.spmService = spmService;
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public List<SurveyData> fetchActiveSurveys() {
        this.securityContext.authenticatedUser();

        final List<SurveyData> result = new ArrayList<>();

        final List<Survey> surveys = this.spmService.fetchValidSurveys();

        if (surveys != null) {
            for (final Survey survey : surveys) {
                result.add(SurveyMapper.map(survey));
            }
        }

        return result;
    }

    @GET
    @Path("/{id}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public SurveyData findSurvey(@PathParam("id") final Long id) {
        this.securityContext.authenticatedUser();

        final Survey survey = this.spmService.findById(id);

        if (survey == null) {
            throw new SurveyNotFoundException(id);
        }

        return SurveyMapper.map(survey);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public void createSurvey(final SurveyData surveyData) {
        this.securityContext.authenticatedUser();

        final Survey survey = SurveyMapper.map(surveyData);

        this.spmService.createSurvey(survey);
    }

    @DELETE
    @Path("/{id}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public void deactivateSurvey(@PathParam("id") final Long id) {
        this.securityContext.authenticatedUser();

        this.spmService.deactivateSurvey(id);
    }
}
