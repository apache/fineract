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
package org.apache.fineract.portfolio.workingcapitalloan.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.workingcapitalloan.WorkingCapitalLoanConstants;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanCommandTemplateData;
import org.apache.fineract.portfolio.workingcapitalloan.exception.WorkingCapitalLoanNotFoundException;
import org.apache.fineract.portfolio.workingcapitalloan.service.WorkingCapitalLoanApplicationReadPlatformService;
import org.apache.fineract.portfolio.workingcapitalloan.service.WorkingCapitalLoanTransactionReadPlatformService;
import org.springframework.stereotype.Component;

@Component
@Path("/v1/working-capital-loans")
@Tag(name = "Working Capital Loan Transactions", description = "Working Capital Loan Transactions")
@RequiredArgsConstructor
public class WorkingCapitalLoanTransactionsApiResource {

    private static final String RESOURCE_NAME_FOR_PERMISSIONS = WorkingCapitalLoanConstants.WCL_RESOURCE_NAME;

    private final PlatformSecurityContext context;
    private final WorkingCapitalLoanApplicationReadPlatformService readPlatformService;
    private final WorkingCapitalLoanTransactionReadPlatformService readTransactionPlatformService;

    @GET
    @Path("{loanId}/template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(operationId = "retrieveWorkingCapitalLoanTemplate", summary = "Retrieve Working Capital Loan action template", description = "Returns loan data for applying the proper loan action")
    public WorkingCapitalLoanCommandTemplateData retrieveWorkingCapitalLoanTemplate(
            @PathParam("loanId") @Parameter(description = "loanId", required = true) final Long loanId,
            @QueryParam("templateType") @Parameter(description = "templateType") final String templateType,
            @Context final UriInfo uriInfo) {
        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);

        return handleLoanTransactionTemplate(loanId, null, templateType);
    }

    private WorkingCapitalLoanCommandTemplateData handleLoanTransactionTemplate(final Long loanId, final String loanExternalIdStr,
            final String templateType) {
        final Long resolvedLoanId = loanId != null ? loanId
                : readPlatformService.getResolvedLoanId(ExternalIdFactory.produce(loanExternalIdStr));
        if (resolvedLoanId == null) {
            throw new WorkingCapitalLoanNotFoundException(ExternalIdFactory.produce(loanExternalIdStr));
        }

        final WorkingCapitalLoanCommandTemplateData loanTransactionTemplateData = readTransactionPlatformService
                .retrieveLoanTransactionTemplate(resolvedLoanId, templateType);
        if (loanTransactionTemplateData == null) {
            throw new UnrecognizedQueryParamException("command", templateType);
        }

        return loanTransactionTemplateData;
    }
}
