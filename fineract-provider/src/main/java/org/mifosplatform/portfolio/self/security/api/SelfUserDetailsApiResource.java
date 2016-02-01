/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.self.security.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.mifosplatform.infrastructure.security.api.UserDetailsApiResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/self/userdetails")
@Component
@Profile("oauth")
@Scope("singleton")
public class SelfUserDetailsApiResource {

	private final UserDetailsApiResource userDetailsApiResource;

	@Autowired
	public SelfUserDetailsApiResource(
			final UserDetailsApiResource userDetailsApiResource) {
		this.userDetailsApiResource = userDetailsApiResource;
	}

	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	public String fetchAuthenticatedUserData(
			@QueryParam("access_token") final String accessToken) {
		return this.userDetailsApiResource
				.fetchAuthenticatedUserData(accessToken);
	}
}
