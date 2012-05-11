package org.mifosng.platform.api.office;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.mifosng.data.ApiParameterError;
import org.mifosng.data.EntityIdentifier;
import org.mifosng.data.OfficeData;
import org.mifosng.data.OfficeList;
import org.mifosng.data.command.OfficeCommand;
import org.mifosng.platform.exceptions.PlatformApiDataValidationException;
import org.mifosng.platform.organisation.service.OfficeReadPlatformService;
import org.mifosng.platform.organisation.service.OfficeWritePlatformService;
import org.mifosng.platform.user.domain.AppUser;
import org.mifosng.platform.user.domain.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
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
	private OfficeReadPlatformService readPlatformService;

	@Autowired
	private OfficeWritePlatformService writePlatformService;
	
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
		hardcodeUserIntoSecurityContext();
		
		Collection<OfficeData> offices = this.readPlatformService.retrieveAllOffices();

		return Response.ok().entity(new OfficeList(offices)).build();
	}
    
    @GET
    @Path("template")
   	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Produces({MediaType.APPLICATION_JSON})
   	public Response retrieveOfficeTemplate() {
   		hardcodeUserIntoSecurityContext();
   		
   		OfficeData newOfficeTemplate = this.readPlatformService.retrieveNewOfficeTemplate();

   		return Response.ok().entity(newOfficeTemplate).build();
   	}
    
	@POST
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON})
	public Response createOffice(final OfficeCommand command) {
		hardcodeUserIntoSecurityContext();
		
		LocalDate openingDate = parseStringToLocalDate(command.getOpeningDateFormatted(), "openingDateFormatted", command.getDateFormat());
		command.setOpeningDate(openingDate);
		
		Long officeId = this.writePlatformService.createOffice(command);

		return Response.ok().entity(new EntityIdentifier(officeId)).build();
	}

	@GET
	@Path("{officeId}")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON})
	public Response retreiveOffice(@PathParam("officeId") final Long officeId) {
		hardcodeUserIntoSecurityContext();
		
		OfficeData office = this.readPlatformService.retrieveOffice(officeId);

		return Response.ok().entity(office).build();
    }

	@PUT
	@Path("{officeId}")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON})
	public Response updateOffice(@PathParam("officeId") final Long officeId, final OfficeCommand command) {
		
		LocalDate openingDate = parseStringToLocalDate(command.getOpeningDateFormatted(), "openingDateFormatted", command.getDateFormat());
		
		hardcodeUserIntoSecurityContext();
		
		command.setId(officeId);
		command.setOpeningDate(openingDate);
		Long entityId = this.writePlatformService.updateOffice(command);

		return Response.ok().entity(new EntityIdentifier(entityId)).build();
	}
	
	private LocalDate parseStringToLocalDate(String eventDate, String dateFieldIdentifier, String dateFormat) {
		LocalDate eventLocalDate = null;
		if (StringUtils.isNotBlank(eventDate)) {
			try {
				Locale locale = LocaleContextHolder.getLocale();
				eventLocalDate = DateTimeFormat.forPattern(dateFormat).withLocale(locale).parseLocalDate(eventDate.toLowerCase(locale));
			} catch (IllegalArgumentException e) {
				List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
				ApiParameterError error = ApiParameterError.parameterError("validation.msg.invalid.date.format", "The parameter " + dateFieldIdentifier + " is invalid.", dateFieldIdentifier, eventDate);
				dataValidationErrors.add(error);
				
				throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.", dataValidationErrors);
			}
		}
		
		return eventLocalDate;
	}
}