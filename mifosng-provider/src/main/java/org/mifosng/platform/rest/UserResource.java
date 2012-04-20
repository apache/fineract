package org.mifosng.platform.rest;

import java.util.ArrayList;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.mifosng.data.AuthenticatedUserData;
import org.mifosng.oauth.CustomOAuthProviderTokenServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth.provider.token.OAuthProviderTokenImpl;
import org.springframework.stereotype.Component;

@Path("/protected/user")
@Component
@Scope("singleton")
public class UserResource {

    @Autowired
    private CustomOAuthProviderTokenServices oauthProviderTokenServices;
    
	@GET
	@Path("{accessToken}")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response retrieveClientAccount(
			@PathParam("accessToken") final String oauthToken) {

		OAuthProviderTokenImpl token = (OAuthProviderTokenImpl) this.oauthProviderTokenServices.getTokenByNonEncodedKey(oauthToken);
		if (token == null) {
			token = (OAuthProviderTokenImpl) this.oauthProviderTokenServices.getToken(oauthToken);
		}
		
		Authentication auth = null;
		if (token != null && token.isAccessToken()) {
			auth = token.getUserAuthentication();
		}
		
		Collection<String> permissions = new ArrayList<String>();
		if (auth != null) {
			for (GrantedAuthority authority : auth.getAuthorities()) {
				permissions.add(authority.getAuthority());
			}
		}
		AuthenticatedUserData authenticatedUserData = new AuthenticatedUserData(permissions);
		if (auth != null) {
			authenticatedUserData.setUsername(((UserDetails)auth.getPrincipal()).getUsername());
		}
		return Response.ok().entity(authenticatedUserData).build();
	}
	
	@GET
	@Path("{accessToken}/signout")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response userSignOut(@PathParam("accessToken") final String oauthToken, @Context HttpServletRequest request) {

		this.oauthProviderTokenServices.removeTokenByNonEncodedKey(oauthToken);
		
		HttpSession session = request.getSession(false);
		if (session != null) {
			session.invalidate();
		}
		
		SecurityContext context = SecurityContextHolder.getContext();
		context.setAuthentication(null);
		
		return Response.ok().build();
	}
}