package org.mifosng.platform.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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

import org.mifosng.data.EntityIdentifier;
import org.mifosng.data.ErrorResponse;
import org.mifosng.data.ErrorResponseList;
import org.mifosng.data.ExtraDatasets;
import org.mifosng.data.reports.GenericResultset;
import org.mifosng.platform.InvalidSqlException;
import org.mifosng.platform.ReadPlatformService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
@Path("/v1/extradata")
@Component
@Scope("singleton")
public class ExtraDataApiResource {

	private final static Logger logger = LoggerFactory
			.getLogger(ExtraDataApiResource.class);
	
	@Autowired
	private ReadPlatformService readPlatformService;

//	@GET
//	@Path("{dataSetPrefix}")
//	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
//	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
//	public Response extraDataNames(@PathParam("dataSetPrefix") final String dataSetPrefix) {
//		
//		List<String> result = this.readPlatformService.retrieveExtraDataTableNames(dataSetPrefix);
//		
//		return Response.ok().entity(result).build();
//	}

	@GET
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON})
	public Response datasets(@Context UriInfo uriInfo) {
		
		MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
		String type = queryParams.getFirst("type"); 

		ExtraDatasets result = this.readPlatformService.retrieveExtraDatasetNames(type);
		return Response.ok().entity(result).build();
	}
	/*
	@GET
	@Path("{datasetType}/{datasetName}/{datasetPKValue}")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response extraData(@PathParam("datasetType") final String datasetType,@PathParam("datasetName") final String datasetName, @PathParam("datasetPKValue") final String datasetPKValue, @Context UriInfo uriInfo) {
		
		try {
			GenericResultset result = this.readPlatformService.retrieveExtraData(datasetType, datasetName, datasetPKValue);
		
			return Response.ok().entity(result).build();
		} catch (InvalidSqlException e) {
			List<ErrorResponse> allErrors = new ArrayList<ErrorResponse>();

			ErrorResponse err = new ErrorResponse("extradata.invalid.sql", "sql", e.getSql());
			allErrors.add(err);

			throw new WebApplicationException(Response
					.status(Status.BAD_REQUEST)
					.entity(new ErrorResponseList(allErrors)).build());
		}
	}
	
	@POST
	@Path("{datasetType}/{datasetName}/{datasetPKValue}")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response saveExtraData(@PathParam("datasetType") final String datasetType,@PathParam("datasetName") final String datasetName, @PathParam("datasetPKValue") final String datasetPKValue, @Context UriInfo uriInfo) {
		
		try {			
			
			MultivaluedMap<String, String> incomingParams = uriInfo.getQueryParameters();
			Map<String, String> queryParams = new HashMap<String, String>();
			
			Set<String> keys = incomingParams.keySet();
			for (String key : keys) {
				String pValue = incomingParams.get(key).get(0);
				queryParams.put(key, pValue);
				logger.info(key + " - " + pValue);
			}
			
			
			this.readPlatformService.tempSaveExtraData(datasetType, datasetName, datasetPKValue, queryParams);
			
			EntityIdentifier entityIdentifier = new EntityIdentifier(Long.valueOf(datasetPKValue));
		
			return Response.ok().entity(entityIdentifier).build();
		} catch (InvalidSqlException e) {
			List<ErrorResponse> allErrors = new ArrayList<ErrorResponse>();

			ErrorResponse err = new ErrorResponse("extradata.invalid.sql", "sql", e.getSql());
			allErrors.add(err);

			throw new WebApplicationException(Response
					.status(Status.BAD_REQUEST)
					.entity(new ErrorResponseList(allErrors)).build());
		}
	}*/
}