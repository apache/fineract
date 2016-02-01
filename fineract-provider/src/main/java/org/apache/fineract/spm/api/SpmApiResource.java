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
package org.apache.fineract.spm.api;

import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.spm.data.SurveyData;
import org.apache.fineract.spm.domain.Survey;
import org.apache.fineract.spm.exception.SurveyNotFoundException;
import org.apache.fineract.spm.service.SpmService;
import org.apache.fineract.spm.util.SurveyMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
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
    @Transactional
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
    @Transactional
    public void createSurvey(final SurveyData surveyData) {
        this.securityContext.authenticatedUser();

        final Survey survey = SurveyMapper.map(surveyData);

        this.spmService.createSurvey(survey);
    }

    @DELETE
    @Path("/{id}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Transactional
    public void deactivateSurvey(@PathParam("id") final Long id) {
        this.securityContext.authenticatedUser();

        this.spmService.deactivateSurvey(id);
    }
}
