/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.mifosplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.mifosplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.mifosplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.mifosplatform.infrastructure.core.service.Page;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.infrastructure.core.service.SearchParameters;
import org.mifosplatform.portfolio.savings.SavingsApiConstants;
import org.mifosplatform.portfolio.savings.data.DepositAccountOnHoldTransactionData;
import org.mifosplatform.portfolio.savings.service.DepositAccountOnHoldTransactionReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/savingsaccounts/{savingsId}/onholdtransactions")
@Component
@Scope("singleton")
public class DepositAccountOnHoldFundTransactionsApiResource {

    private final PlatformSecurityContext context;
    private final DefaultToApiJsonSerializer<DepositAccountOnHoldTransactionData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final DepositAccountOnHoldTransactionReadPlatformService depositAccountOnHoldTransactionReadPlatformService;

    @Autowired
    public DepositAccountOnHoldFundTransactionsApiResource(final PlatformSecurityContext context,
            final DefaultToApiJsonSerializer<DepositAccountOnHoldTransactionData> toApiJsonSerializer,
            final ApiRequestParameterHelper apiRequestParameterHelper,
            final DepositAccountOnHoldTransactionReadPlatformService depositAccountOnHoldTransactionReadPlatformService) {
        this.context = context;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.depositAccountOnHoldTransactionReadPlatformService = depositAccountOnHoldTransactionReadPlatformService;
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveAll(@PathParam("savingsId") final Long savingsId,
            @QueryParam("guarantorFundingId") final Long guarantorFundingId, @Context final UriInfo uriInfo,
            @QueryParam("offset") final Integer offset, @QueryParam("limit") final Integer limit,
            @QueryParam("orderBy") final String orderBy, @QueryParam("sortOrder") final String sortOrder) {

        this.context.authenticatedUser().validateHasReadPermission(SavingsApiConstants.SAVINGS_ACCOUNT_RESOURCE_NAME);

        final SearchParameters searchParameters = SearchParameters.forPagination(offset, limit, orderBy, sortOrder);

        final Page<DepositAccountOnHoldTransactionData> transfers = this.depositAccountOnHoldTransactionReadPlatformService.retriveAll(
                savingsId, guarantorFundingId, searchParameters);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer
                .serialize(settings, transfers, SavingsApiConstants.SAVINGS_ACCOUNT_ON_HOLD_RESPONSE_DATA_PARAMETERS);
    }

}