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
package org.apache.fineract.portfolio.self.account.api;

import java.util.Collection;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.account.api.AccountTransfersApiResource;
import org.apache.fineract.portfolio.self.account.data.SelfAccountTemplateData;
import org.apache.fineract.portfolio.self.account.data.SelfAccountTransferData;
import org.apache.fineract.portfolio.self.account.data.SelfAccountTransferDataValidator;
import org.apache.fineract.portfolio.self.account.service.SelfAccountTransferReadService;
import org.apache.fineract.useradministration.domain.AppUser;
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
