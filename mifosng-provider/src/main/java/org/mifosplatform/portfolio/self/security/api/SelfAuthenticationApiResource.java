/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.self.security.api;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.mifosplatform.infrastructure.security.api.AuthenticationApiResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/self/authentication")
@Component
@Profile("basicauth")
@Scope("singleton")
public class SelfAuthenticationApiResource {

	private final AuthenticationApiResource authenticationApiResource;

	@Autowired
	public SelfAuthenticationApiResource(
			final AuthenticationApiResource authenticationApiResource) {
		this.authenticationApiResource = authenticationApiResource;
	}

	@POST
	@Produces({ MediaType.APPLICATION_JSON })
	public String authenticate(@QueryParam("username") final String username,
			@QueryParam("password") final String password) {
		return this.authenticationApiResource.authenticate(username, password);
	}

}
