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
package org.apache.fineract.portfolio.self.security.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.apache.fineract.infrastructure.security.api.UserDetailsApiResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/self/userdetails")
@Component
@Profile("oauth")
@Scope("singleton")
@Api(tags = {"Self User Details"})
@SwaggerDefinition(tags = {
  @Tag(name = "Self User Details", description = "")
})
public class SelfUserDetailsApiResource {

    private final UserDetailsApiResource userDetailsApiResource;

    @Autowired
    public SelfUserDetailsApiResource(
            final UserDetailsApiResource userDetailsApiResource) {
        this.userDetailsApiResource = userDetailsApiResource;
    }

    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Fetch authenticated user details", httpMethod = "GET", notes = "Checks the Authentication and returns the set roles and permissions allowed\n\n" + "For more info visit this link - https://demo.mifos.io/api-docs/apiLive.htm#selfoauth")
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = SelfUserDetailsApiResourceSwagger.GetSelfUserDetailsResponse.class)})
    public String fetchAuthenticatedUserData(
            @QueryParam("access_token") @ApiParam(value = "äccess_token") final String accessToken) {
        return this.userDetailsApiResource
                .fetchAuthenticatedUserData(accessToken);
    }
}
