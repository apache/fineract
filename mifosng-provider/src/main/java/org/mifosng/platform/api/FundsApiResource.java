package org.mifosng.platform.api;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

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
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public Response createFund(final String jsonRequestBody) {
		
		Gson gson = new Gson();
		Type typeOfMap = new TypeToken<Map<String, String>>(){}.getType();
	    Map<String, String> requestMap = gson.fromJson(jsonRequestBody, typeOfMap);
	    
	    String name = null;
	    if (requestMap.containsKey("name")) {
	    	name = requestMap.get("name");
	    }
		
	    String externalId = null;
	    if (requestMap.containsKey("externalId")) {
	    	externalId = requestMap.get("externalId");
	    }
	    
		FundCommand command = new FundCommand(name, externalId);

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
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public Response updateFund(@PathParam("fundId") final Long fundId, final String jsonRequestBody) {

		Gson gson = new Gson();
		Type typeOfMap = new TypeToken<Map<String, String>>(){}.getType();
	    Map<String, String> requestMap = gson.fromJson(jsonRequestBody, typeOfMap);
	    
	    String name = null;
	    if (requestMap.containsKey("name")) {
	    	name = requestMap.get("name");
	    }
		
	    String externalId = null;
	    if (requestMap.containsKey("externalId")) {
	    	externalId = requestMap.get("externalId");
	    }
	    
		FundCommand command = new FundCommand(name, externalId);
		command.setId(fundId);

		Long entityId = this.writePlatformService.updateFund(command);

		return Response.ok().entity(new EntityIdentifier(entityId)).build();
	}
}