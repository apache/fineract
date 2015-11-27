/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.spm.api;

import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.spm.data.LookupTableData;
import org.mifosplatform.spm.domain.LookupTable;
import org.mifosplatform.spm.domain.Survey;
import org.mifosplatform.spm.exception.LookupTableNotFoundException;
import org.mifosplatform.spm.exception.SurveyNotFoundException;
import org.mifosplatform.spm.service.LookupTableService;
import org.mifosplatform.spm.service.SpmService;
import org.mifosplatform.spm.util.LookupTableMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

@Path("/surveys/{surveyId}/lookuptables")
@Component
@Scope("singleton")
public class LookupTableApiResource {

    private final PlatformSecurityContext securityContext;
    private final SpmService spmService;
    private final LookupTableService lookupTableService;

    @Autowired
    public LookupTableApiResource(final PlatformSecurityContext securityContext,
                                  final SpmService spmService,
                                  final LookupTableService lookupTableService) {
        super();
        this.securityContext = securityContext;
        this.spmService = spmService;
        this.lookupTableService = lookupTableService;
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public List<LookupTableData> fetchLookupTables(@PathParam("surveyId") final Long surveyId) {
        this.securityContext.authenticatedUser();

        final Survey survey = findSurvey(surveyId);

        final List<LookupTableData> result = new ArrayList<>();

        final List<LookupTable> lookupTables = this.lookupTableService.findBySurvey(survey);

        if (lookupTables != null) {
            for (final LookupTable lookupTable : lookupTables) {
                result.add(LookupTableMapper.map(lookupTable));
            }
        }

        return result;
    }

    @GET
    @Path("/{id}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public LookupTableData findLookupTable(@PathParam("surveyId") final Long surveyId,
                                           @PathParam("id") final Long id) {
        this.securityContext.authenticatedUser();

        findSurvey(surveyId);

        final LookupTable lookupTable = this.lookupTableService.findById(id);

        if (lookupTable == null) {
            throw new LookupTableNotFoundException(id);
        }

        return LookupTableMapper.map(lookupTable);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public void createLookupTable(@PathParam("surveyId") final Long surveyId,
                                  final LookupTableData lookupTableData) {
        this.securityContext.authenticatedUser();

        final Survey survey = findSurvey(surveyId);

        this.lookupTableService.createLookupTable(LookupTableMapper.map(lookupTableData, survey));
    }

    private Survey findSurvey(final Long surveyId) {
        final Survey survey = this.spmService.findById(surveyId);
        if (survey == null) {
            throw new SurveyNotFoundException(surveyId);
        }
        return survey;
    }
}
