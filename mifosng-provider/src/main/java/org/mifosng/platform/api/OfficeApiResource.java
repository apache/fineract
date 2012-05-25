package org.mifosng.platform.api;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.joda.time.LocalDate;
import org.mifosng.data.EntityIdentifier;
import org.mifosng.data.OfficeData;
import org.mifosng.data.command.OfficeCommand;
import org.mifosng.platform.api.infrastructure.ApiDataConversionService;
import org.mifosng.platform.api.infrastructure.ApiJSONFormattingService;
import org.mifosng.platform.organisation.service.OfficeReadPlatformService;
import org.mifosng.platform.organisation.service.OfficeWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
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
	private ApiDataConversionService apiDataConversionService;

	@Autowired
	private ApiJSONFormattingService jsonFormattingService;

	@GET
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveOffices(@QueryParam("fields") String fields,
			@QueryParam("pretty") String pretty) {

		Collection<OfficeData> offices = this.readPlatformService
				.retrieveAllOffices();

		Set<String> excludeFields = new HashSet<String>();
		excludeFields.add("allowedParents");

		return this.jsonFormattingService.convertDataObjectJSON(offices,
				fields, excludeFields,
				this.jsonFormattingService.isTrue(pretty));
	}

	@GET
	@Path("template")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveOfficeTemplate(@QueryParam("pretty") String pretty) {

		OfficeData officeData = this.readPlatformService
				.retrieveNewOfficeTemplate();

		Set<String> excludeFields = new HashSet<String>();
		String includeFields = "openingDate,allowedParents";

		return this.jsonFormattingService.convertDataObjectJSON(officeData,
				includeFields, excludeFields,
				this.jsonFormattingService.isTrue(pretty));
	}

	@POST
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response createOffice(final OfficeCommand command) {

		LocalDate openingDate = apiDataConversionService.convertFrom(
				command.getOpeningDateFormatted(), "openingDateFormatted",
				command.getDateFormat());
		command.setOpeningDate(openingDate);

		Long officeId = this.writePlatformService.createOffice(command);

		return Response.ok().entity(new EntityIdentifier(officeId)).build();
	}

	@GET
	@Path("{officeId}")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retreiveOffice(@PathParam("officeId") final Long officeId,
			@QueryParam("fields") String fields,
			@QueryParam("template") String template,
			@QueryParam("pretty") String pretty) {

		String includeFields = fields;
		Set<String> excludeFields = new HashSet<String>();
		OfficeData office = this.readPlatformService.retrieveOffice(officeId);

		if (this.jsonFormattingService.isTrue(template)) {
			office.setAllowedParents(this.readPlatformService
					.retrieveAllowedParents(officeId));

			if (this.jsonFormattingService.isPassed(includeFields)) {
				includeFields += ",allowedParents";
			}

		} else {
			if (!(this.jsonFormattingService.isPassed(includeFields))) {
				excludeFields.add("allowedParents");
			}
		}

		return this.jsonFormattingService.convertDataObjectJSON(office,
				includeFields, excludeFields,
				this.jsonFormattingService.isTrue(pretty));
	}

	@PUT
	@Path("{officeId}")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response updateOffice(@PathParam("officeId") final Long officeId,
			final OfficeCommand command) {

		LocalDate openingDate = apiDataConversionService.convertFrom(
				command.getOpeningDateFormatted(), "openingDateFormatted",
				command.getDateFormat());
		command.setOpeningDate(openingDate);
		command.setId(officeId);

		Long entityId = this.writePlatformService.updateOffice(command);

		return Response.ok().entity(new EntityIdentifier(entityId)).build();
	}
}