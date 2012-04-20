package org.mifosng.platform.rest;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.mifosng.data.ExtraDatasets;
import org.mifosng.data.reports.GenericResultset;
import org.mifosng.platform.ReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.sun.jersey.api.json.JSONWithPadding;

/**
 * TODO - remove open reporting from application
 * @deprecated - remove from application
 */
@Deprecated
@Path("/open/reporting")
@Component
@Scope("singleton")
public class FlexibleReportingResource {

	@Autowired
	private ReadPlatformService readPlatformService;

	@GET
	@Path("flexireportxxx")
	@Consumes({ "application/x-javascript" })
	@Produces({ "application/x-javascript" })
	public Response retrieveReport(
			@QueryParam("callback") @DefaultValue("callback") final String callbackName, @Context UriInfo uriInfo) {
		
		MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
		String rptDB = queryParams.getFirst("MRP_rptDB");
		String name = queryParams.getFirst("MRP_Name");
		String type = queryParams.getFirst("MRP_Type");
		
		Map<String, String> extractedQueryParams = new HashMap<String, String>();
		
		Set<String> keys = queryParams.keySet();  
		String pKey;
		String pValue;
		for (String k : keys) {  
	
			if (k.startsWith("MRP_"))
			{
				pKey = "${" + k.substring(4) + "}";
				pValue = queryParams.get(k).get(0);
				
				extractedQueryParams.put(pKey, pValue);
			}
		}  

		GenericResultset result = this.readPlatformService.retrieveGenericResultset(rptDB, name, type, extractedQueryParams);

		JSONWithPadding paddedResult = new JSONWithPadding(result,
				callbackName);
		
		return Response.ok().entity(paddedResult).build();
	}
	

	@GET
	@Path("extradatanamesxxx")
	@Consumes({ "application/x-javascript" })
	@Produces({ "application/x-javascript" })
	public Response extraDataNames(
			@QueryParam("callback") @DefaultValue("callback") final String callbackName, @Context UriInfo uriInfo) {
		
		MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
		String tableName = queryParams.getFirst("tableName"); 

		ExtraDatasets result = this.readPlatformService.retrieveExtraDatasetNames(tableName);

		JSONWithPadding paddedResult = new JSONWithPadding(result,
				callbackName);
		
		return Response.ok().entity(paddedResult).build();
	}
	
	@GET
	@Path("extradataxxx")
	@Consumes({ "application/x-javascript" })
	@Produces({ "application/x-javascript" })
	public Response extraData(
			@QueryParam("callback") @DefaultValue("callback") final String callbackName, @Context UriInfo uriInfo) {
		
		MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
		String extraDataTableName = queryParams.getFirst("extraDataTableName"); 
		String extraDataTableId = queryParams.getFirst("extraDataTableId"); 

		GenericResultset result = this.readPlatformService.retrieveExtraData(extraDataTableName, extraDataTableName, extraDataTableId);

		JSONWithPadding paddedResult = new JSONWithPadding(result,
				callbackName);
		
		return Response.ok().entity(paddedResult).build();
	}
}