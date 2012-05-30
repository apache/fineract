package org.mifosng.platform.api;

import java.util.Collection;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.joda.time.LocalDate;
import org.mifosng.data.EntityIdentifier;
import org.mifosng.platform.api.commands.OfficeCommand;
import org.mifosng.platform.api.data.OfficeData;
import org.mifosng.platform.api.infrastructure.ApiDataConversionService;
import org.mifosng.platform.api.infrastructure.ApiJSONFormattingService;
import org.mifosng.platform.organisation.service.OfficeReadPlatformService;
import org.mifosng.platform.organisation.service.OfficeWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/offices")
@Component
@Scope("singleton")
public class OfficeApiResource {

	private String defaultFieldList = "openingDate";
	private String allowedFieldList = "allowedParents";
	private String filterName = "myFilter";

	@Autowired
	private OfficeReadPlatformService readPlatformService;

	@Autowired
	private OfficeWritePlatformService writePlatformService;

	@Autowired
	private ApiDataConversionService apiDataConversionService;

	@Autowired
	private ApiJSONFormattingService jsonFormattingService;

	@GET
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveOffices(@Context UriInfo uriInfo) {

		Collection<OfficeData> offices = this.readPlatformService
				.retrieveAllOffices();
		String selectedFields = "";
		return this.jsonFormattingService.convertRequest(offices, filterName,
				allowedFieldList, selectedFields, uriInfo.getQueryParameters());
	}

	@GET
	@Path("template")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveOfficeTemplate(@Context UriInfo uriInfo) {

		OfficeData officeData = this.readPlatformService
				.retrieveNewOfficeTemplate();

		String selectedFields = defaultFieldList + "," + allowedFieldList;
		return this.jsonFormattingService.convertRequest(officeData,
				filterName, allowedFieldList, selectedFields,
				uriInfo.getQueryParameters());
	}

	@POST
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response createOffice(final OfficeCommand command) {

		LocalDate openingLocalDate = apiDataConversionService.convertFrom(
				command.getOpeningDate(), "openingDate",
				command.getDateFormat());
		command.setOpeningLocalDate(openingLocalDate);

		Long officeId = this.writePlatformService.createOffice(command);

		return Response.ok().entity(new EntityIdentifier(officeId)).build();
	}

	@GET
	@Path("{officeId}")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retreiveOffice(@PathParam("officeId") final Long officeId,
			@QueryParam("template") String template, @Context UriInfo uriInfo) {

		OfficeData office = this.readPlatformService.retrieveOffice(officeId);
		if (template != null && template.equalsIgnoreCase("true")) {
			office.setAllowedParents(this.readPlatformService
					.retrieveAllowedParents(officeId));
		}

		String selectedFields = "";
		return this.jsonFormattingService.convertRequest(office, filterName,
				allowedFieldList, selectedFields, uriInfo.getQueryParameters());
	}

	@PUT
	@Path("{officeId}")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response updateOffice(@PathParam("officeId") final Long officeId,
			final OfficeCommand command) {

		LocalDate openingLocalDate = apiDataConversionService.convertFrom(
				command.getOpeningDate(), "openingDate",
				command.getDateFormat());
		command.setOpeningLocalDate(openingLocalDate);
		command.setId(officeId);

		Long entityId = this.writePlatformService.updateOffice(command);

		return Response.ok().entity(new EntityIdentifier(entityId)).build();
	}
}