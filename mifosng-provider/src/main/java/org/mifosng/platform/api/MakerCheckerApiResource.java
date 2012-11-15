package org.mifosng.platform.api;

import java.util.Collection;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.mifosng.platform.api.data.EntityIdentifier;
import org.mifosng.platform.api.data.MakerCheckerData;
import org.mifosng.platform.api.infrastructure.PortfolioApiJsonSerializerService;
import org.mifosng.platform.exceptions.UnrecognizedQueryParamException;
import org.mifosng.platform.infrastructure.api.ApiParameterHelper;
import org.mifosng.platform.makerchecker.service.PortfolioMakerCheckerReadPlatformService;
import org.mifosng.platform.makerchecker.service.PortfolioMakerCheckerWriteService;
import org.mifosng.platform.security.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/makerchecker")
@Component
@Scope("singleton")
public class MakerCheckerApiResource {

	@Autowired
	private PortfolioMakerCheckerReadPlatformService readPlatformService;
	
	@Autowired
	private PortfolioMakerCheckerWriteService writePlatformService;

	@Autowired
	private PortfolioApiJsonSerializerService apiJsonSerializerService;

	private final String entityType = "MAKERCHECKER";
    @Autowired
    private PlatformSecurityContext context;
    
	@GET
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public String retrieveCodes(@Context final UriInfo uriInfo) {

    	context.authenticatedUser().validateHasReadPermission(entityType);
    	
		final Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
		final boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());
		
		final Collection<MakerCheckerData> entries = this.readPlatformService.retrieveAllEntriesToBeChecked();
		
		return this.apiJsonSerializerService.serializeMakerCheckerDataToJson(prettyPrint, responseParameters, entries);
	}
	
	@POST
	@Path("{makerCheckerId}")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public String approveMakerCheckerEntry(
			@PathParam("makerCheckerId") final Long makerCheckerId,
			@QueryParam("command") final String commandParam) {

		Long id = null;
		if (is(commandParam, "approve")) {
			id = this.writePlatformService.approveEntry(makerCheckerId);
		} else {
			throw new UnrecognizedQueryParamException("command", commandParam);
		}

		return this.apiJsonSerializerService.serializeEntityIdentifier(EntityIdentifier.makerChecker(id));
	}

	private boolean is(final String commandParam, final String commandValue) {
		return StringUtils.isNotBlank(commandParam)
				&& commandParam.trim().equalsIgnoreCase(commandValue);
	}
}