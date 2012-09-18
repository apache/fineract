package org.mifosng.platform.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.mifosng.platform.api.data.DatatableData;
import org.mifosng.platform.api.data.EntityIdentifier;
import org.mifosng.platform.api.data.GenericResultsetData;
import org.mifosng.platform.api.infrastructure.ApiJsonSerializerService;
import org.mifosng.platform.api.infrastructure.ApiParameterHelper;
import org.mifosng.platform.exceptions.NoAuthorizationException;
import org.mifosng.platform.noncore.ReadWriteNonCoreDataService;
import org.mifosng.platform.security.PlatformSecurityContext;
import org.mifosng.platform.user.domain.AppUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/datatables")
@Component
@Scope("singleton")
public class DataTableApiResource {

	private final static Logger logger = LoggerFactory
			.getLogger(DataTableApiResource.class);

	private final PlatformSecurityContext context;

	@Autowired
	public DataTableApiResource(final PlatformSecurityContext context) {
		this.context = context;
	}

	@Autowired
	private ReadWriteNonCoreDataService readWriteNonCoreDataService;

	@Autowired
	private ApiJsonSerializerService apiJsonSerializerService;

	@GET
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String datasets(@QueryParam("appTable") final String appTable,
			@Context final UriInfo uriInfo) {

		List<DatatableData> result = this.readWriteNonCoreDataService
				.retrieveDatatableNames(appTable);

		boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo
				.getQueryParameters());
		return this.apiJsonSerializerService.serializeDatatableDataToJson(
				prettyPrint, result);
	}

	@GET
	@Path("{datatable}/{appTableId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String getDatatable(@PathParam("datatable") final String datatable,
			@PathParam("appTableId") final Long appTableId,
			@QueryParam("sqlFields") final String sqlFields,
			@QueryParam("sqlOrder") final String sqlOrder,
			@Context final UriInfo uriInfo) {

		checkUserPermissionForDatatable(datatable, "READ");

		/*
		 * Dont use this for now... but its the code for returning data as json
		 * objects rather than a generic resultset String resultStr =
		 * this.readWriteNonCoreDataService
		 * .retrieveDataTableJSONObject(datatable, id, sqlFields, sqlOrder);
		 */

		GenericResultsetData results = this.readWriteNonCoreDataService
				.retrieveDataTableGenericResultSet(datatable, appTableId,
						sqlFields, sqlOrder);

		boolean prettyPrints = ApiParameterHelper.prettyPrint(uriInfo
				.getQueryParameters());
		return this.apiJsonSerializerService
				.serializeGenericResultsetDataToJson(prettyPrints, results);

	}

	@POST
	@Path("{datatable}/{appTableId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response newDatatableEntry(
			@PathParam("datatable") final String datatable,
			@PathParam("appTableId") final Long appTableId,
			final String jsonRequestBody) {

		checkUserPermissionForDatatable(datatable, "CREATE");
		Map<String, String> queryParams = getQueryParamsFromJsonRequestBody(jsonRequestBody);

		this.readWriteNonCoreDataService.newDatatableEntry(datatable,
				appTableId, queryParams);

		EntityIdentifier entityIdentifier = new EntityIdentifier(
				Long.valueOf(appTableId));

		return Response.ok().entity(entityIdentifier).build();

	}
	

	@PUT
	@Path("{datatable}/{appTableId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response updateDatatableEntryOnetoOne(
			@PathParam("datatable") final String datatable,
			@PathParam("appTableId") final Long appTableId,
			final String jsonRequestBody) {

		checkUserPermissionForDatatable(datatable, "UPDATE");
		Map<String, String> queryParams = getQueryParamsFromJsonRequestBody(jsonRequestBody);

		this.readWriteNonCoreDataService.updateDatatableEntryOnetoOne(datatable,
				appTableId, queryParams);

		EntityIdentifier entityIdentifier = new EntityIdentifier(
				Long.valueOf(appTableId));

		return Response.ok().entity(entityIdentifier).build();

	}

	@DELETE
	@Path("{datatable}/{appTableId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response deleteDatatableEntries(
			@PathParam("datatable") final String datatable,
			@PathParam("appTableId") final Long appTableId) {

		checkUserPermissionForDatatable(datatable, "DELETE");

		this.readWriteNonCoreDataService.deleteDatatableEntries(datatable,
				appTableId);

		EntityIdentifier entityIdentifier = new EntityIdentifier(
				Long.valueOf(appTableId));

		return Response.ok().entity(entityIdentifier).build();

	}

	@DELETE
	@Path("{datatable}/{appTableId}/{datatableId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response deleteDatatableEntries(
			@PathParam("datatable") final String datatable,
			@PathParam("appTableId") final Long appTableId,
			@PathParam("datatableId") final Long datatableId) {

		checkUserPermissionForDatatable(datatable, "DELETE");

		this.readWriteNonCoreDataService.deleteDatatableEntry(datatable,
				appTableId, datatableId);

		EntityIdentifier entityIdentifier = new EntityIdentifier(
				Long.valueOf(appTableId));

		return Response.ok().entity(entityIdentifier).build();

	}

	private Map<String, String> getQueryParamsFromJsonRequestBody(
			String jsonRequestBody) {

		Map<String, String> queryParams = new HashMap<String, String>();

		String pValue = "";
		String pName;
		try {
			JSONObject jsonObj = new JSONObject(jsonRequestBody);
			JSONArray jsonArr = jsonObj.names();
			if (jsonArr != null) {
				for (int i = 0; i < jsonArr.length(); i++) {
					pName = (String) jsonArr.get(i);
					pValue = jsonObj.getString(pName);
					logger.info("getQueryParamsFromJsonRequestBody: " + pName + " - " + pValue);
					queryParams.put(pName, pValue);
				}
				return queryParams;
			}
			throw new WebApplicationException(Response
					.status(Status.BAD_REQUEST).entity("JSON body empty")
					.build());

		} catch (JSONException e) {
			throw new WebApplicationException(Response
					.status(Status.BAD_REQUEST).entity("JSON body is wrong")
					.build());
		}

	}

	private void checkUserPermissionForDatatable(String datatable,
			String accessType) {
		AppUser currentUser = context.authenticatedUser();
		if (currentUser.hasNotPermissionForDatatable(datatable, accessType)) {
			throw new NoAuthorizationException("Not Authorised to "
					+ accessType + " Data Table: " + datatable);
		}
	}

}
