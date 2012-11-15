package org.mifosng.platform.api;

import java.util.Collection;
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

import org.mifosng.platform.api.commands.CodeCommand;
import org.mifosng.platform.api.data.CodeData;
import org.mifosng.platform.api.data.EntityIdentifier;
import org.mifosng.platform.api.infrastructure.PortfolioApiDataConversionService;
import org.mifosng.platform.api.infrastructure.PortfolioApiJsonSerializerService;
import org.mifosng.platform.infrastructure.api.ApiParameterHelper;
import org.mifosng.platform.organisation.service.CodeWritePlatformService;
import org.mifosng.platform.organisation.service.CodeReadPlatformService;
import org.mifosng.platform.security.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/codes")
@Component
@Scope("singleton")
public class CodesApiResource {

	@Autowired
	private CodeReadPlatformService readPlatformService;

	@Autowired
	private CodeWritePlatformService writePlatformService;

	@Autowired
	private PortfolioApiDataConversionService apiDataConversionService;
	
	@Autowired
	private PortfolioApiJsonSerializerService apiJsonSerializerService;

	private final String entityType = "CODE";
	private final PlatformSecurityContext context;

	@Autowired
	public CodesApiResource(final PlatformSecurityContext context) {
		this.context = context;
	}
	
	@GET
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public String retrieveCodes(@Context final UriInfo uriInfo) {

    	context.authenticatedUser().validateHasReadPermission(entityType);
    	
		Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
		boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());
		
		final Collection<CodeData> codes = this.readPlatformService.retrieveAllCodes();
		
		return this.apiJsonSerializerService.serializeCodeDataToJson(prettyPrint, responseParameters, codes);
	}

	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public Response createCode(final String jsonRequestBody) {
		
		final CodeCommand command = this.apiDataConversionService.convertJsonToCodeCommand(null, jsonRequestBody);
		
		final Long id = this.writePlatformService.createCode(command);

		return Response.ok().entity(new EntityIdentifier(id)).build();
	}


	@PUT
	@Path("{Id}")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public Response updateCode(@PathParam("Id") final Long Id, final String jsonRequestBody) {

		return null;
	}
}