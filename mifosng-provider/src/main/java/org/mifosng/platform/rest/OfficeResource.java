package org.mifosng.platform.rest;

import java.util.Arrays;
import java.util.Collection;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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
import org.mifosng.platform.exceptions.UnAuthenticatedUserException;
import org.mifosng.platform.exceptions.NewDataValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Path("/protected/office")
@Component
@Scope("singleton")
public class OfficeResource {

    @Autowired
	private ReadPlatformService readPlatformService;

	@Autowired
	private WritePlatformService writePlatformService;

    @GET
	@Path("all")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response retrieveOffices() {

    	try {
    		Collection<OfficeData> offices = this.readPlatformService.retrieveAllOffices();

    		return Response.ok().entity(new OfficeList(offices)).build();
		} catch (UnAuthenticatedUserException e) {
			throw new WebApplicationException(Response.status(Status.UNAUTHORIZED).build());
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
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response retreiveOffice(@PathParam("officeId") final Long officeId) {

    	try {
    		OfficeData office = this.readPlatformService.retrieveOffice(officeId);

    		return Response.ok().entity(office).build();
		} catch (UnAuthenticatedUserException e) {
			throw new WebApplicationException(Response.status(Status.UNAUTHORIZED).build());
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
	@Path("new")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response createOffice(final OfficeCommand command) {

		try {
			Long officeId = this.writePlatformService.createOffice(command);

			return Response.ok().entity(new EntityIdentifier(officeId)).build();
		} catch (UnAuthenticatedUserException e) {
			throw new WebApplicationException(Response.status(Status.UNAUTHORIZED).build());
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
	@Path("update")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response updateOffice(final OfficeCommand command) {

		try {
			Long officeId = this.writePlatformService.updateOffice(command);

			return Response.ok().entity(new EntityIdentifier(officeId)).build();
		} catch (UnAuthenticatedUserException e) {
			throw new WebApplicationException(Response.status(Status.UNAUTHORIZED).build());
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