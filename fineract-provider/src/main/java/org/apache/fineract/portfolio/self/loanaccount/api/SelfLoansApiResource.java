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
package org.apache.fineract.portfolio.self.loanaccount.api;

import java.util.HashMap;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.apache.fineract.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.client.exception.ClientNotFoundException;
import org.apache.fineract.portfolio.loanaccount.api.LoanChargesApiResource;
import org.apache.fineract.portfolio.loanaccount.api.LoanTransactionsApiResource;
import org.apache.fineract.portfolio.loanaccount.api.LoansApiResource;
import org.apache.fineract.portfolio.loanaccount.exception.LoanNotFoundException;
import org.apache.fineract.portfolio.loanaccount.exception.LoanTemplateTypeRequiredException;
import org.apache.fineract.portfolio.loanaccount.exception.NotSupportedLoanTemplateTypeException;
import org.apache.fineract.portfolio.self.client.service.AppuserClientMapperReadService;
import org.apache.fineract.portfolio.self.loanaccount.data.SelfLoansDataValidator;
import org.apache.fineract.portfolio.self.loanaccount.service.AppuserLoansMapperReadService;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/self/loans")
@Component
@Scope("singleton")
public class SelfLoansApiResource {

	private final PlatformSecurityContext context;
	private final LoansApiResource loansApiResource;
	private final LoanTransactionsApiResource loanTransactionsApiResource;
	private final LoanChargesApiResource loanChargesApiResource;
	private final AppuserLoansMapperReadService appuserLoansMapperReadService;
	private final AppuserClientMapperReadService appUserClientMapperReadService;
	private final SelfLoansDataValidator dataValidator;

	@Autowired
	public SelfLoansApiResource(final PlatformSecurityContext context,
			final LoansApiResource loansApiResource,
			final LoanTransactionsApiResource loanTransactionsApiResource,
			final LoanChargesApiResource loanChargesApiResource,
			final AppuserLoansMapperReadService appuserLoansMapperReadService,
			final AppuserClientMapperReadService appUserClientMapperReadService,
			final SelfLoansDataValidator dataValidator) {
		this.context = context;
		this.loansApiResource = loansApiResource;
		this.loanTransactionsApiResource = loanTransactionsApiResource;
		this.loanChargesApiResource = loanChargesApiResource;
		this.appuserLoansMapperReadService = appuserLoansMapperReadService;
		this.appUserClientMapperReadService = appUserClientMapperReadService;
		this.dataValidator = dataValidator;
	}

	@GET
	@Path("{loanId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveLoan(@PathParam("loanId") final Long loanId,
			@Context final UriInfo uriInfo) {

		this.dataValidator.validateRetrieveLoan(uriInfo);

		validateAppuserLoanMapping(loanId);

		final boolean staffInSelectedOfficeOnly = false;
		return this.loansApiResource.retrieveLoan(loanId,
				staffInSelectedOfficeOnly, uriInfo);
	}

	@GET
	@Path("{loanId}/transactions/{transactionId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveTransaction(@PathParam("loanId") final Long loanId,
			@PathParam("transactionId") final Long transactionId,
			@Context final UriInfo uriInfo) {

		this.dataValidator.validateRetrieveTransaction(uriInfo);

		validateAppuserLoanMapping(loanId);

		return this.loanTransactionsApiResource.retrieveTransaction(loanId,
				transactionId, uriInfo);
	}

	@GET
	@Path("{loanId}/charges")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveAllLoanCharges(
			@PathParam("loanId") final Long loanId,
			@Context final UriInfo uriInfo) {

		validateAppuserLoanMapping(loanId);

		return this.loanChargesApiResource.retrieveAllLoanCharges(loanId,
				uriInfo);
	}

	@GET
	@Path("{loanId}/charges/{chargeId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveLoanCharge(@PathParam("loanId") final Long loanId,
			@PathParam("chargeId") final Long loanChargeId,
			@Context final UriInfo uriInfo) {

		validateAppuserLoanMapping(loanId);

		return this.retrieveLoanCharge(loanId, loanChargeId, uriInfo);
	}

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String template(@QueryParam("clientId") final Long clientId, 
    		@QueryParam("productId") final Long productId,
    		@QueryParam("templateType") final String templateType,
    		@Context final UriInfo uriInfo) {
    	
    	if(clientId != null){
        	validateAppuserClientsMapping(clientId);
    	}
    	
        if (templateType == null) {
            final String errorMsg = "Loan template type must be provided";
            throw new LoanTemplateTypeRequiredException(errorMsg);
        } else if (!(templateType.equalsIgnoreCase("individual") 
        		|| templateType.equalsIgnoreCase("collateral"))){
            final String errorMsg = "Loan template type '" + templateType + "' is not supported";
            throw new NotSupportedLoanTemplateTypeException(errorMsg, templateType);
        }
    	final Long groupId = null;
    	final boolean staffInSelectedOfficeOnly = false;
    	final boolean onlyActive = true;
    	return this.loansApiResource.template(clientId, groupId, productId, 
    			templateType, staffInSelectedOfficeOnly, onlyActive, uriInfo);

    }
    
    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String calculateLoanScheduleOrSubmitLoanApplication(@QueryParam("command") final String commandParam,
            @Context final UriInfo uriInfo, final String apiRequestBodyAsJson) {
    	
    	HashMap<String, Object> attr = this.dataValidator.validateLoanApplication(apiRequestBodyAsJson);
        final Long clientId = (Long) attr.get("clientId");
        validateAppuserClientsMapping(clientId);

    	return this.loansApiResource.calculateLoanScheduleOrSubmitLoanApplication(commandParam, 
    			uriInfo, apiRequestBodyAsJson);
    }
    
    @PUT
    @Path("{loanId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String modifyLoanApplication(@PathParam("loanId") final Long loanId, final String apiRequestBodyAsJson) {

    	HashMap<String, Object> attr = this.dataValidator.validateModifyLoanApplication(apiRequestBodyAsJson);
        validateAppuserLoanMapping(loanId);
        final Long clientId = (Long) attr.get("clientId");
        if(clientId != null){
            validateAppuserClientsMapping(clientId);
        }

    	return this.loansApiResource.modifyLoanApplication(loanId, apiRequestBodyAsJson);
    }
    
    @POST
    @Path("{loanId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String stateTransitions(@PathParam("loanId") final Long loanId, @QueryParam("command") final String commandParam,
            final String apiRequestBodyAsJson) {
    	if (!is(commandParam, "withdrawnByApplicant")) {
    		throw new UnrecognizedQueryParamException("command", commandParam);
    	}
    	validateAppuserLoanMapping(loanId);
    	return this.loansApiResource.stateTransitions(loanId, commandParam, apiRequestBodyAsJson);
    }
    
    private void validateAppuserLoanMapping(final Long loanId) {
		AppUser user = this.context.authenticatedUser();
		final boolean isLoanMappedToUser = this.appuserLoansMapperReadService
				.isLoanMappedToUser(loanId, user.getId());
		if (!isLoanMappedToUser) {
			throw new LoanNotFoundException(loanId);
		}
	}
    
	private void validateAppuserClientsMapping(final Long clientId) {
		AppUser user = this.context.authenticatedUser();
		final boolean mappedClientId = this.appUserClientMapperReadService
				.isClientMappedToUser(clientId, user.getId());
		if (!mappedClientId) {
			throw new ClientNotFoundException(clientId);
		}
	}

    private boolean is(final String commandParam, final String commandValue) {
        return StringUtils.isNotBlank(commandParam) && commandParam.trim().equalsIgnoreCase(commandValue);
    }

}