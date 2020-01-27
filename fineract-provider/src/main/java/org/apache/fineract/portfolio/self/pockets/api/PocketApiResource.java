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

package org.apache.fineract.portfolio.self.pockets.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import org.apache.commons.lang.StringUtils;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.portfolio.self.pockets.service.PocketAccountMappingReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/self/pockets")
@Component
@Scope("singleton")
@Api(tags = {"Pocket"})
@SwaggerDefinition(tags = {
  @Tag(name = "Pocket", description = "")
})
public class PocketApiResource {
 private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
 @SuppressWarnings("rawtypes")
 private final DefaultToApiJsonSerializer toApiJsonSerializer;
 private final PocketAccountMappingReadPlatformService pocketAccountMappingReadPlatformService;

 @SuppressWarnings("rawtypes")
 @Autowired
 public PocketApiResource(PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
   final DefaultToApiJsonSerializer toApiJsonSerializer,
   final PocketAccountMappingReadPlatformService pocketAccountMappingReadPlatformService) {
  this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
  this.toApiJsonSerializer = toApiJsonSerializer;
  this.pocketAccountMappingReadPlatformService = pocketAccountMappingReadPlatformService;
 }

 @POST
 @Consumes({ MediaType.APPLICATION_JSON })
 @Produces({ MediaType.APPLICATION_JSON })
 @ApiOperation(value = "Link/delink accounts to/from pocket", httpMethod = "POST", notes = "Pockets behave as favourites. An user can link his/her Loan, Savings and Share accounts to pocket for faster access. In a similar way linked accounts can be delinked from the pocket.\n\n" + "Example Requests:\n" + "\n" + "self/pockets?command=linkAccounts\n" + "\n" + "self/pockets?command=delinkAccounts")
 @ApiResponses({@ApiResponse(code = 200, message = "OK", response =  PocketApiResourceSwagger.PostLinkDelinkAccountsToFromPocketResponse.class)})
 public String handleCommands(@QueryParam("command") @ApiParam(value = "command") final String commandParam, @Context final UriInfo uriInfo,
   final String apiRequestBodyAsJson) {

  final CommandWrapperBuilder builder = new CommandWrapperBuilder().withJson(apiRequestBodyAsJson);

  CommandProcessingResult result = null;

  if (is(commandParam, PocketApiConstants.linkAccountsToPocketCommandParam)) {
   final CommandWrapper commandRequest = builder.linkAccountsToPocket().build();
   result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
  } else if (is(commandParam, PocketApiConstants.delinkAccountsFromPocketCommandParam)) {
   final CommandWrapper commandRequest = builder.delinkAccountsFromPocket().build();
   result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
  }

  if (result == null) {
   throw new UnrecognizedQueryParamException("command", commandParam);
  }

  return this.toApiJsonSerializer.serialize(result);
 }

 @GET
 @Consumes({ MediaType.APPLICATION_JSON })
 @Produces({ MediaType.APPLICATION_JSON })
 @ApiOperation(value = "Retrieve accounts linked to pocket", httpMethod = "GET", notes = "All linked loan\n\n" + "Example Requests:\n" + "\n"  + "\n" + "self/pockets")
 @ApiResponses({@ApiResponse(code = 200, message = "OK", response =  PocketApiResourceSwagger.GetAccountsLinkedToPocketResponse.class)})
 public String retrieveAll() {
  return this.toApiJsonSerializer.serialize(this.pocketAccountMappingReadPlatformService.retrieveAll());
 }

 private boolean is(final String commandParam, final String commandValue) {
  return StringUtils.isNotBlank(commandParam) && commandParam.trim().equalsIgnoreCase(commandValue);
 }

}
