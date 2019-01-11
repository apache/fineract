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

package org.apache.fineract.portfolio.self.products.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.apache.fineract.portfolio.savings.SavingsApiConstants;
import org.apache.fineract.portfolio.savings.api.SavingsProductsApiResource;
import org.apache.fineract.portfolio.self.client.service.AppuserClientMapperReadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/self/savingsproducts")
@Component
@Scope("singleton")
public class SelfSavingsProductsApiResource {

	private final SavingsProductsApiResource savingsProductsApiResource;
	private final AppuserClientMapperReadService appUserClientMapperReadService;

	@Autowired
	public SelfSavingsProductsApiResource(final SavingsProductsApiResource savingsProductsApiResource,
			final AppuserClientMapperReadService appUserClientMapperReadService) {
		this.savingsProductsApiResource = savingsProductsApiResource;
		this.appUserClientMapperReadService = appUserClientMapperReadService;

	}

	@GET
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveAll(@QueryParam(SavingsApiConstants.clientIdParamName) final Long clientId,
			@Context final UriInfo uriInfo) {

		this.appUserClientMapperReadService.validateAppuserClientsMapping(clientId);
		return this.savingsProductsApiResource.retrieveAll(uriInfo);

	}

	@GET
	@Path("{productId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveOne(@PathParam(SavingsApiConstants.productIdParamName) final Long productId,
			@QueryParam(SavingsApiConstants.clientIdParamName) final Long clientId, @Context final UriInfo uriInfo) {

		this.appUserClientMapperReadService.validateAppuserClientsMapping(clientId);
		return this.savingsProductsApiResource.retrieveOne(productId, uriInfo);

	}

}
