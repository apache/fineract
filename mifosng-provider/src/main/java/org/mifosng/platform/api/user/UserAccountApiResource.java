package org.mifosng.platform.api.user;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.mifosng.data.AppUserData;
import org.mifosng.data.EntityIdentifier;
import org.mifosng.data.command.ChangePasswordCommand;
import org.mifosng.data.command.UserCommand;
import org.mifosng.platform.ReadPlatformService;
import org.mifosng.platform.user.domain.AppUser;
import org.mifosng.platform.user.domain.AppUserRepository;
import org.mifosng.platform.user.service.AppUserWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Path("/v1/useraccounts")
@Component
@Scope("singleton")
public class UserAccountApiResource {
	
    @Autowired
	private ReadPlatformService readPlatformService;

	@Autowired
	private AppUserWritePlatformService appUserWritePlatformService;

	@Autowired
	private AppUserRepository appUserRepository;
	
	private void hardcodeUserIntoSecurityContext() {
		AppUser currentUser = this.appUserRepository.findOne(Long.valueOf(1));
    	
    	Authentication auth = new UsernamePasswordAuthenticationToken(currentUser, currentUser, currentUser.getAuthorities());
		SecurityContext context = SecurityContextHolder.getContext();
		context.setAuthentication(auth);
	}
	
	@GET
	@Path("current")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({ MediaType.APPLICATION_JSON})
	public Response retrieveCurrentUser() {

		hardcodeUserIntoSecurityContext();
		
		AppUserData user = this.readPlatformService.retrieveCurrentUser();
    	
		return Response.ok().entity(user).build();
	}
	
	@PUT
	@Path("current")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({ MediaType.APPLICATION_JSON})
	public Response updateCurrentUser(UserCommand command) {

		hardcodeUserIntoSecurityContext();
		
		Long userId = this.appUserWritePlatformService.updateCurrentUser(command);

		return Response.ok().entity(new EntityIdentifier(userId)).build();
	}

	@GET
	@Path("current/password/template")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({ MediaType.APPLICATION_JSON})
	public Response retrieveCurrentPasswordTemplate() {

		return Response.ok().entity(new ChangePasswordCommand()).build();
	}
	
	@PUT
	@Path("current/password")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({ MediaType.APPLICATION_JSON})
	public Response updateCurrentUserPassword(ChangePasswordCommand command) {

		hardcodeUserIntoSecurityContext();
		
		Long userId = this.appUserWritePlatformService.updateCurrentUserPassword(command);

		return Response.ok().entity(new EntityIdentifier(userId)).build();
	}
}