/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.self.account.api;

import java.util.Collection;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.mifosplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.mifosplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.mifosplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.account.api.AccountTransfersApiResource;
import org.mifosplatform.portfolio.self.account.data.SelfAccountTemplateData;
import org.mifosplatform.portfolio.self.account.data.SelfAccountTransferData;
import org.mifosplatform.portfolio.self.account.data.SelfAccountTransferDataValidator;
import org.mifosplatform.portfolio.self.account.service.SelfAccountTransferReadService;
import org.mifosplatform.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/self/accounttransfers")
@Component
@Scope("singleton")
public class SelfAccountTransferApiResource {

	private final PlatformSecurityContext context;
	private final DefaultToApiJsonSerializer<SelfAccountTransferData> toApiJsonSerializer;
	private final AccountTransfersApiResource accountTransfersApiResource;
	private final SelfAccountTransferReadService selfAccountTransferReadService;
	private final ApiRequestParameterHelper apiRequestParameterHelper;
	private final SelfAccountTransferDataValidator dataValidator;

	@Autowired
	public SelfAccountTransferApiResource(
			final PlatformSecurityContext context,
			final DefaultToApiJsonSerializer<SelfAccountTransferData> toApiJsonSerializer,
			final AccountTransfersApiResource accountTransfersApiResource,
			final SelfAccountTransferReadService selfAccountTransferReadService,
			final ApiRequestParameterHelper apiRequestParameterHelper,
			final SelfAccountTransferDataValidator dataValidator) {
		this.context = context;
		this.toApiJsonSerializer = toApiJsonSerializer;
		this.accountTransfersApiResource = accountTransfersApiResource;
		this.selfAccountTransferReadService = selfAccountTransferReadService;
		this.apiRequestParameterHelper = apiRequestParameterHelper;
		this.dataValidator = dataValidator;
	}

	@GET
	@Path("template")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String template(@Context final UriInfo uriInfo) {

		AppUser user = this.context.authenticatedUser();
		Collection<SelfAccountTemplateData> templateData = this.selfAccountTransferReadService
				.retrieveSelfAccountTemplateData(user);

		final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper
				.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings,
				new SelfAccountTransferData(templateData));
	}

	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String create(final String apiRequestBodyAsJson) {
		this.dataValidator.validateCreate(apiRequestBodyAsJson);
		return this.accountTransfersApiResource.create(apiRequestBodyAsJson);
	}

}
