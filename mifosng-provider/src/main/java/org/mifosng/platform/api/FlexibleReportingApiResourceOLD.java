package org.mifosng.platform.api;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.mifosng.data.ErrorResponse;
import org.mifosng.data.ErrorResponseList;
import org.mifosng.data.reports.GenericResultset;
import org.mifosng.platform.ReadPlatformService;
import org.mifosng.platform.exceptions.ApplicationDomainRuleException;
import org.mifosng.platform.exceptions.NewDataValidationException;
import org.mifosng.platform.exceptions.UnAuthenticatedUserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Path("/v1/flexiblereportingxxx")
@Component
@Scope("singleton")
public class FlexibleReportingApiResourceOLD {

	private final static Logger logger = LoggerFactory.getLogger(FlexibleReportingApiResourceOLD.class);
	
	@Autowired
	private ReadPlatformService readPlatformService;
	
	@GET
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({ MediaType.APPLICATION_JSON})
	public Response retrieveReport(@Context UriInfo uriInfo) {
		
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
		logger.info("BEGINNING REQUEST FOR: " + name);
		GenericResultset result = this.readPlatformService.retrieveGenericResultset(rptDB, name, type, extractedQueryParams);

//		JSONWithPadding paddedResult = new JSONWithPadding(result,
//				callbackName);
		
		return Response.ok().entity(result).build();
	}
	/*
	@GET
	@Path("/exportcsv/{reportDb}/{reportName}/{reportType}/office/{officeId}/currency/{currencyId}")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response exportCsvReport(
			@PathParam("reportDb") String rptDB, 
			@PathParam("reportName") String name, 
			@PathParam("reportType") String type,
			@PathParam("officeId") Long officeId, @PathParam("currencyId") Long currencyId) throws IOException {
		
		try {
			Map<String, String> extractedQueryParams = new HashMap<String, String>();
			
			if (officeId != null && officeId > 0) {
				extractedQueryParams.put("${officeId}", officeId.toString());
			}
			
			if (currencyId != null) {
				extractedQueryParams.put("${currencyId}", currencyId.toString());
			}
			
			GenericResultset result = this.readPlatformService.retrieveGenericResultset(rptDB, name, type, extractedQueryParams);

			return Response.ok().entity(result).build();
		} catch (UnAuthenticatedUserException e) {
			throw new WebApplicationException(Response.status(Status.UNAUTHORIZED).build());
		} catch (AccessDeniedException e) {*/
}