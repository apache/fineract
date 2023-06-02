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
package org.apache.fineract.portfolio.loanaccount.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.office.data.OfficeData;
import org.apache.fineract.organisation.office.service.OfficeReadPlatformService;
import org.apache.fineract.organisation.staff.data.BulkTransferLoanOfficerData;
import org.apache.fineract.organisation.staff.data.StaffAccountSummaryCollectionData;
import org.apache.fineract.organisation.staff.data.StaffData;
import org.apache.fineract.organisation.staff.service.StaffReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.service.BulkLoansReadPlatformService;
import org.springframework.stereotype.Component;

@Path("/v1/loans/loanreassignment")
@Component
@Tag(name = "Bulk Loans", description = "")
@RequiredArgsConstructor
public class BulkLoansApiResource {

    public static final String OFFICE_ID = "officeId";
    public static final String FROM_LOAN_OFFICER_ID = "fromLoanOfficerId";
    public static final String ASSIGNMENT_DATE = "assignmentDate";
    public static final String OFFICE_OPTIONS = "officeOptions";
    public static final String LOAN_OFFICER_OPTIONS = "loanOfficerOptions";
    public static final String ACCOUNT_SUMMARY_COLLECTION = "accountSummaryCollection";
    private static final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<>(Arrays.asList(OFFICE_ID, FROM_LOAN_OFFICER_ID,
            ASSIGNMENT_DATE, OFFICE_OPTIONS, LOAN_OFFICER_OPTIONS, ACCOUNT_SUMMARY_COLLECTION));

    private static final String RESOURCE_NAME_FOR_PERMISSIONS = "LOAN";

    private final PlatformSecurityContext context;
    private final StaffReadPlatformService staffReadPlatformService;
    private final OfficeReadPlatformService officeReadPlatformService;
    private final BulkLoansReadPlatformService bulkLoansReadPlatformService;
    private final DefaultToApiJsonSerializer<BulkTransferLoanOfficerData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String loanReassignmentTemplate(@QueryParam(OFFICE_ID) final Long officeId,
            @QueryParam(FROM_LOAN_OFFICER_ID) final Long loanOfficerId, @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);

        final Collection<OfficeData> offices = this.officeReadPlatformService.retrieveAllOfficesForDropdown();

        Collection<StaffData> loanOfficers = null;
        StaffAccountSummaryCollectionData staffAccountSummaryCollectionData = null;

        if (officeId != null) {
            loanOfficers = this.staffReadPlatformService.retrieveAllLoanOfficersInOfficeById(officeId);
        }

        if (loanOfficerId != null) {
            staffAccountSummaryCollectionData = this.bulkLoansReadPlatformService.retrieveLoanOfficerAccountSummary(loanOfficerId);
        }

        final BulkTransferLoanOfficerData loanReassignmentData = BulkTransferLoanOfficerData.templateForBulk(officeId, loanOfficerId,
                LocalDate.now(DateUtils.getDateTimeZoneOfTenant()), offices, loanOfficers, staffAccountSummaryCollectionData);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, loanReassignmentData, RESPONSE_DATA_PARAMETERS);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String loanReassignment(final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().assignLoanOfficersInBulk().withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }
}
