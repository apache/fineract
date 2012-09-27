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
import org.mifosng.platform.noncore.GenericDataService;
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
	private GenericDataService genericDataService;

	@Autowired
	private ReadWriteNonCoreDataService readWriteNonCoreDataService;

	@Autowired
	private ApiJsonSerializerService apiJsonSerializerService;

	@GET
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String getDatatables(@QueryParam("apptable") final String apptable,
			@Context final UriInfo uriInfo) {

		List<DatatableData> result = this.readWriteNonCoreDataService
				.retrieveDatatableNames(apptable);

		boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo
				.getQueryParameters());
		return this.apiJsonSerializerService.serializeDatatableDataToJson(
				prettyPrint, result);
	}

	@POST
	@Path("register/{datatable}/{apptable}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response registerDatatable(
			@PathParam("datatable") final String datatable,
			@PathParam("apptable") final String apptable) {

		this.readWriteNonCoreDataService.registerDatatable(datatable, apptable);

		EntityIdentifier entityIdentifier = new EntityIdentifier();
		return Response.ok().entity(entityIdentifier).build();
	}

	@POST
	@Path("deregister/{datatable}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response deregisterDatatable(
			@PathParam("datatable") final String datatable) {

		this.readWriteNonCoreDataService.deregisterDatatable(datatable);

		EntityIdentifier entityIdentifier = new EntityIdentifier();
		return Response.ok().entity(entityIdentifier).build();
	}

	@GET
	@Path("{datatable}/{apptableId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String getDatatable(@PathParam("datatable") final String datatable,
			@PathParam("apptableId") final Long apptableId,
			@QueryParam("sqlFields") final String sqlFields,
			@QueryParam("sqlOrder") final String sqlOrder,
			@Context final UriInfo uriInfo) {

		checkUserPermissionForDatatable(datatable, "READ");

		GenericResultsetData results = this.readWriteNonCoreDataService
				.retrieveDataTableGenericResultSet(datatable, apptableId,
						sqlFields, sqlOrder, null);

		boolean genericResultSet = ApiParameterHelper.genericResultSet(uriInfo
				.getQueryParameters());

		if (genericResultSet) {
			boolean prettyPrints = ApiParameterHelper.prettyPrint(uriInfo
					.getQueryParameters());
			return this.apiJsonSerializerService
					.serializeGenericResultsetDataToJson(prettyPrints, results);
		} else {

			return this.genericDataService
					.generateJsonFromGenericResultsetData(results);
		}

	}

	@POST
	@Path("{datatable}/{apptableId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response newDatatableEntry(
			@PathParam("datatable") final String datatable,
			@PathParam("apptableId") final Long apptableId,
			final String jsonRequestBody) {

		checkUserPermissionForDatatable(datatable, "CREATE");
		Map<String, String> queryParams = getQueryParamsFromJsonRequestBody(jsonRequestBody);

		this.readWriteNonCoreDataService.newDatatableEntry(datatable,
				apptableId, queryParams);

		EntityIdentifier entityIdentifier = new EntityIdentifier(
				Long.valueOf(apptableId));

		return Response.ok().entity(entityIdentifier).build();

	}

	@PUT
	@Path("{datatable}/{apptableId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response updateDatatableEntryOnetoOne(
			@PathParam("datatable") final String datatable,
			@PathParam("apptableId") final Long apptableId,
			final String jsonRequestBody) {
		/*
		 * for updating one to one relationships (where foreign key is the
		 * primary key)
		 */
		checkUserPermissionForDatatable(datatable, "UPDATE");
		Map<String, String> queryParams = getQueryParamsFromJsonRequestBody(jsonRequestBody);

		this.readWriteNonCoreDataService.updateDatatableEntryOnetoOne(
				datatable, apptableId, queryParams);

		EntityIdentifier entityIdentifier = new EntityIdentifier(
				Long.valueOf(apptableId));

		return Response.ok().entity(entityIdentifier).build();

	}

	@PUT
	@Path("{datatable}/{apptableId}/{datatableId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response updateDatatableEntryOnetoOne(
			@PathParam("datatable") final String datatable,
			@PathParam("apptableId") final Long apptableId,
			@PathParam("datatableId") final Long datatableId,
			final String jsonRequestBody) {
		/*
		 * for updating one to many relationships (where foreign key isn't the
		 * primary key)
		 */
		checkUserPermissionForDatatable(datatable, "UPDATE");
		Map<String, String> queryParams = getQueryParamsFromJsonRequestBody(jsonRequestBody);

		this.readWriteNonCoreDataService.updateDatatableEntryOnetoMany(
				datatable, apptableId, datatableId, queryParams);

		EntityIdentifier entityIdentifier = new EntityIdentifier(
				Long.valueOf(apptableId));

		return Response.ok().entity(entityIdentifier).build();

	}

	@DELETE
	@Path("{datatable}/{apptableId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response deleteDatatableEntries(
			@PathParam("datatable") final String datatable,
			@PathParam("apptableId") final Long apptableId) {

		checkUserPermissionForDatatable(datatable, "DELETE");

		this.readWriteNonCoreDataService.deleteDatatableEntries(datatable,
				apptableId);

		EntityIdentifier entityIdentifier = new EntityIdentifier(
				Long.valueOf(apptableId));

		return Response.ok().entity(entityIdentifier).build();

	}

	@DELETE
	@Path("{datatable}/{apptableId}/{datatableId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response deleteDatatableEntries(
			@PathParam("datatable") final String datatable,
			@PathParam("apptableId") final Long apptableId,
			@PathParam("datatableId") final Long datatableId) {

		checkUserPermissionForDatatable(datatable, "DELETE");

		this.readWriteNonCoreDataService.deleteDatatableEntry(datatable,
				apptableId, datatableId);

		EntityIdentifier entityIdentifier = new EntityIdentifier(
				Long.valueOf(apptableId));

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
					logger.info("getQueryParamsFromJsonRequestBody: " + pName
							+ " - " + pValue);
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
