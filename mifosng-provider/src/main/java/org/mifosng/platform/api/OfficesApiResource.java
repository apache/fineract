package org.mifosng.platform.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.mifosng.platform.api.commands.OfficeCommand;
import org.mifosng.platform.api.data.EntityIdentifier;
import org.mifosng.platform.api.data.OfficeData;
import org.mifosng.platform.api.data.OfficeLookup;
import org.mifosng.platform.api.infrastructure.ApiDataConversionService;
import org.mifosng.platform.api.infrastructure.ApiJsonSerializerService;
import org.mifosng.platform.api.infrastructure.ApiParameterHelper;
import org.mifosng.platform.organisation.service.OfficeReadPlatformService;
import org.mifosng.platform.organisation.service.OfficeWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/offices")
@Component
@Scope("singleton")
public class OfficesApiResource {

	@Autowired
	private OfficeReadPlatformService readPlatformService;

	@Autowired
	private OfficeWritePlatformService writePlatformService;

	@Autowired
	private ApiDataConversionService apiDataConversionService;
	
	@Autowired
	private ApiJsonSerializerService apiJsonSerializerService;

	@GET
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public String retrieveOffices(@Context final UriInfo uriInfo) {
		
		Set<String> typicalResponseParameters = new HashSet<String>(
				Arrays.asList("id", "name", "nameDecorated", "externalId", "openingDate", 
				"hierarchy", "parentId", "parentName"));
		
		Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
		if (responseParameters.isEmpty()) {
			responseParameters.addAll(typicalResponseParameters);
		}
		boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());

		Collection<OfficeData> offices = this.readPlatformService.retrieveAllOffices();
		
		return this.apiJsonSerializerService.serializeOfficeDataToJson(prettyPrint, responseParameters, offices);
	}

	@GET
	@Path("template")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public String retrieveOfficeTemplate(@Context final UriInfo uriInfo) {

		Set<String> typicalResponseParameters = new HashSet<String>(
				Arrays.asList("id", "name", "nameDecorated", "externalId", "openingDate", 
				"hierarchy", "parentId", "parentName", "allowedParents"));
		
		boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());

		OfficeData office = this.readPlatformService.retrieveNewOfficeTemplate();
		
		return this.apiJsonSerializerService.serializeOfficeDataToJson(prettyPrint, typicalResponseParameters, office);
	}

	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public Response createOffice(final String jsonRequestBody) {
		
		OfficeCommand command = this.apiDataConversionService.convertJsonToOfficeCommand(null, jsonRequestBody);
	    
		Long officeId = this.writePlatformService.createOffice(command);

		return Response.ok().entity(new EntityIdentifier(officeId)).build();
	}

	@GET
	@Path("{officeId}")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public String retreiveOffice(@PathParam("officeId") final Long officeId, @Context final UriInfo uriInfo) {
		
		Set<String> typicalResponseParameters = new HashSet<String>(
				Arrays.asList("id", "name", "nameDecorated", "externalId", "openingDate", 
				"hierarchy", "parentId", "parentName"));
		
		Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
		if (responseParameters.isEmpty()) {
			responseParameters.addAll(typicalResponseParameters);
		}
		boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());
		boolean template = ApiParameterHelper.template(uriInfo.getQueryParameters());

		OfficeData office = this.readPlatformService.retrieveOffice(officeId);
		if (template) {
			List<OfficeLookup> allowedParents = this.readPlatformService.retrieveAllowedParents(officeId);
			office = OfficeData.appendedTemplate(office, allowedParents);
			responseParameters.add("allowedParents");
		}

		return this.apiJsonSerializerService.serializeOfficeDataToJson(prettyPrint, responseParameters, office);
	}

	@PUT
	@Path("{officeId}")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public Response updateOffice(@PathParam("officeId") final Long officeId, final String jsonRequestBody) {
		
		OfficeCommand command = this.apiDataConversionService.convertJsonToOfficeCommand(officeId, jsonRequestBody);
		
		Long entityId = this.writePlatformService.updateOffice(command);

		return Response.ok().entity(new EntityIdentifier(entityId)).build();
	}
}