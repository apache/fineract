package org.mifosng.platform.api;

import java.util.Arrays;
import java.util.Collection;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.mifosng.data.EntityIdentifier;
import org.mifosng.data.ErrorResponse;
import org.mifosng.data.ErrorResponseList;
import org.mifosng.data.OfficeData;
import org.mifosng.data.OfficeList;
import org.mifosng.data.command.OfficeCommand;
import org.mifosng.platform.ReadPlatformService;
import org.mifosng.platform.WritePlatformService;
import org.mifosng.platform.exceptions.ApplicationDomainRuleException;
import org.mifosng.platform.exceptions.NewDataValidationException;
import org.mifosng.platform.user.domain.AppUser;
import org.mifosng.platform.user.domain.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Path("/v1/offices")
@Component
@Scope("singleton")
public class OfficeApiResource {

    @Autowired
	private ReadPlatformService readPlatformService;

	@Autowired
	private WritePlatformService writePlatformService;
	
	@Autowired
	private AppUserRepository appUserRepository;

	private void hardcodeUserIntoSecurityContext() {
		AppUser currentUser = this.appUserRepository.findOne(Long.valueOf(1));
    	
    	Authentication auth = new UsernamePasswordAuthenticationToken(currentUser, currentUser, currentUser.getAuthorities());
		SecurityContext context = SecurityContextHolder.getContext();
		context.setAuthentication(auth);
	}
	
    @GET
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Produces({MediaType.APPLICATION_JSON})
	public Response retrieveOffices() {

    	try {
    		hardcodeUserIntoSecurityContext();
    		
    		Collection<OfficeData> offices = this.readPlatformService.retrieveAllOffices();

    		return Response.ok().entity(new OfficeList(offices)).build();
		} catch (AccessDeniedException e) {
			ErrorResponse errorResponse = new ErrorResponse("error.msg.no.permission", "id");
			ErrorResponseList list = new ErrorResponseList(Arrays.asList(errorResponse));
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(list).build());
		} catch (ApplicationDomainRuleException e) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new ErrorResponseList(e.getErrors())).build());
		} catch (NewDataValidationException e) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new ErrorResponseList(e.getValidationErrors())).build());
		}
	}
    
	@POST
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON})
	public Response createOffice(final OfficeCommand command) {

		try {
			hardcodeUserIntoSecurityContext();
			
			Long officeId = this.writePlatformService.createOffice(command);

			return Response.ok().entity(new EntityIdentifier(officeId)).build();
		} catch (AccessDeniedException e) {
			ErrorResponse errorResponse = new ErrorResponse("error.msg.no.permission", "id");
			ErrorResponseList list = new ErrorResponseList(Arrays.asList(errorResponse));
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(list).build());
		} catch (ApplicationDomainRuleException e) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new ErrorResponseList(e.getErrors())).build());
		} catch (NewDataValidationException e) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new ErrorResponseList(e.getValidationErrors())).build());
		}
	}

	@GET
	@Path("{officeId}")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON})
	public Response retreiveOffice(@PathParam("officeId") final Long officeId) {

    	try {
    		hardcodeUserIntoSecurityContext();
    		
    		OfficeData office = this.readPlatformService.retrieveOffice(officeId);

    		return Response.ok().entity(office).build();
		} catch (AccessDeniedException e) {
			ErrorResponse errorResponse = new ErrorResponse("error.msg.no.permission", "id");
			ErrorResponseList list = new ErrorResponseList(Arrays.asList(errorResponse));
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(list).build());
		} catch (ApplicationDomainRuleException e) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new ErrorResponseList(e.getErrors())).build());
		} catch (NewDataValidationException e) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new ErrorResponseList(e.getValidationErrors())).build());
		}
    }

	@PUT
	@Path("{officeId}")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON})
	public Response updateOffice(@PathParam("officeId") final Long officeId, final OfficeCommand command) {

		try {
			hardcodeUserIntoSecurityContext();
			
			command.setId(officeId);
			Long entityId = this.writePlatformService.updateOffice(command);

			return Response.ok().entity(new EntityIdentifier(entityId)).build();
		} catch (AccessDeniedException e) {
			ErrorResponse errorResponse = new ErrorResponse("error.msg.no.permission", "id");
			ErrorResponseList list = new ErrorResponseList(Arrays.asList(errorResponse));
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(list).build());
		} catch (ApplicationDomainRuleException e) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new ErrorResponseList(e.getErrors())).build());
		} catch (NewDataValidationException e) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new ErrorResponseList(e.getValidationErrors())).build());
		}
	}
}