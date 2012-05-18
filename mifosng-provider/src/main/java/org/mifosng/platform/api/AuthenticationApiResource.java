package org.mifosng.platform.api;

import java.util.ArrayList;
import java.util.Collection;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.mifosng.data.AuthenticatedUserData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

@Path("/v1/authentication")
@Component
@Scope("singleton")
public class AuthenticationApiResource {

	@Qualifier("customAuthenticationProvider")
    @Autowired
	private DaoAuthenticationProvider customAuthenticationProvider;
    
    @POST
	@Consumes({MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_XML})
	public Response authenticate(@QueryParam("username") final String username, @QueryParam("password") final String password) {

    	Authentication authentication = new UsernamePasswordAuthenticationToken(username, password);
    	Authentication authenticationCheck = customAuthenticationProvider.authenticate(authentication);
    	
    	Collection<String> permissions = new ArrayList<String>();
		AuthenticatedUserData authenticatedUserData = new AuthenticatedUserData(permissions);
		authenticatedUserData.setUsername(username);
		
		if (authenticationCheck.isAuthenticated()) {
			Collection<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>(authenticationCheck.getAuthorities());
			for (GrantedAuthority grantedAuthority : authorities) {
				permissions.add(grantedAuthority.getAuthority());
			}
			authenticatedUserData.setPermissions(permissions);
			authenticatedUserData.setAuthenticated(true);
		}
    	
		return Response.ok().entity(authenticatedUserData).build();
	}
}