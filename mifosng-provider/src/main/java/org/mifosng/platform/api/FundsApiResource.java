package org.mifosng.platform.api;

import java.util.Collection;

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

import org.mifosng.platform.api.commands.FundCommand;
import org.mifosng.platform.api.data.EntityIdentifier;
import org.mifosng.platform.api.data.FundData;
import org.mifosng.platform.api.infrastructure.ApiJSONFormattingService;
import org.mifosng.platform.fund.service.FundReadPlatformService;
import org.mifosng.platform.fund.service.FundWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/funds")
@Component
@Scope("singleton")
public class FundsApiResource {

	private String allowedFieldList = "allowedParents";
	private String filterName = "myFilter";

	@Autowired
	private FundReadPlatformService readPlatformService;

	@Autowired
	private FundWritePlatformService writePlatformService;

	@Autowired
	private ApiJSONFormattingService jsonFormattingService;

	@GET
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveFunds(@Context UriInfo uriInfo) {

		Collection<FundData> funds = this.readPlatformService.retrieveAllFunds();
		String selectedFields = "";
		return this.jsonFormattingService.convertRequest(funds, filterName,
				allowedFieldList, selectedFields, uriInfo.getQueryParameters());
	}

	@POST
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response createFund(final FundCommand command) {

		Long fundId = this.writePlatformService.createFund(command);

		return Response.ok().entity(new EntityIdentifier(fundId)).build();
	}

	@GET
	@Path("{fundId}")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retreiveOffice(@PathParam("fundId") final Long fundId, @Context UriInfo uriInfo) {

		FundData fund = this.readPlatformService.retrieveFund(fundId);

		String selectedFields = "";
		return this.jsonFormattingService.convertRequest(fund, filterName,
				allowedFieldList, selectedFields, uriInfo.getQueryParameters());
	}

	@PUT
	@Path("{fundId}")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response updateFund(@PathParam("fundId") final Long fundId, final FundCommand command) {

		command.setId(fundId);

		Long entityId = this.writePlatformService.updateFund(command);

		return Response.ok().entity(new EntityIdentifier(entityId)).build();
	}
}