package org.mifosplatform.infrastructure.security.api;

import java.util.ArrayList;
import java.util.Collection;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.mifosplatform.infrastructure.core.serialization.ToApiJsonSerializer;
import org.mifosplatform.infrastructure.security.data.AuthenticatedUserData;
import org.mifosplatform.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import com.sun.jersey.core.util.Base64;

@Path("/authentication")
@Component
@Scope("singleton")
public class AuthenticationApiResource {

    private final DaoAuthenticationProvider customAuthenticationProvider;
    private final ToApiJsonSerializer<AuthenticatedUserData> apiJsonSerializerService;

    @Autowired
    public AuthenticationApiResource(
            @Qualifier("customAuthenticationProvider") final DaoAuthenticationProvider customAuthenticationProvider,
            final ToApiJsonSerializer<AuthenticatedUserData> apiJsonSerializerService) {
        this.customAuthenticationProvider = customAuthenticationProvider;
        this.apiJsonSerializerService = apiJsonSerializerService;
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String authenticate(@QueryParam("username") final String username, @QueryParam("password") final String password) {

        Authentication authentication = new UsernamePasswordAuthenticationToken(username, password);
        Authentication authenticationCheck = customAuthenticationProvider.authenticate(authentication);

        Collection<String> permissions = new ArrayList<String>();
        AuthenticatedUserData authenticatedUserData = new AuthenticatedUserData(username, permissions);

        if (authenticationCheck.isAuthenticated()) {
            Collection<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>(authenticationCheck.getAuthorities());
            for (GrantedAuthority grantedAuthority : authorities) {
                permissions.add(grantedAuthority.getAuthority());
            }
            AppUser principal = (AppUser) authenticationCheck.getPrincipal();
            byte[] base64EncodedAuthenticationKey = Base64.encode(username + ":" + password);

            authenticatedUserData = new AuthenticatedUserData(username, permissions, principal.getId(), new String(
                    base64EncodedAuthenticationKey));
        }

        return this.apiJsonSerializerService.serialize(authenticatedUserData);
    }
}