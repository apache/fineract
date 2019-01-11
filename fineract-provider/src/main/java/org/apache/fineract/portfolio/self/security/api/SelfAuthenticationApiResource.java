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

import io.swagger.annotations.*;
import org.apache.fineract.infrastructure.security.api.AuthenticationApiResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path("/self/authentication")
@Component
@Profile("basicauth")
@Scope("singleton")
@Api(value = "Self Authentication", description = "")
public class SelfAuthenticationApiResource {

	private final AuthenticationApiResource authenticationApiResource;

	@Autowired
	public SelfAuthenticationApiResource(
			final AuthenticationApiResource authenticationApiResource) {
		this.authenticationApiResource = authenticationApiResource;
	}

	@POST
	@Produces({ MediaType.APPLICATION_JSON })
	@ApiOperation(value = "Verify authentication", httpMethod = "POST", notes = "Authenticates the credentials provided and returns the set roles and permissions allowed.\n\n" + "Please visit this link for more info - https://demo.openmf.org/api-docs/apiLive.htm#selfbasicauth")
	@ApiResponses({@ApiResponse(code = 200, message = "OK", response = SelfAuthenticationApiResourceSwagger.PostSelfAuthenticationResponse.class)})
	public String authenticate(@QueryParam("username") @ApiParam(value = "username") final String username,
			@QueryParam("password") @ApiParam(value = "password") final String password) {
		return this.authenticationApiResource.authenticate(username, password);
	}

}
