package org.mifosplatform.infrastructure.dataqueries.api;

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
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.infrastructure.core.api.ApiParameterHelper;
import org.mifosplatform.infrastructure.core.data.EntityIdentifier;
import org.mifosplatform.infrastructure.core.serialization.ToApiJsonSerializer;
import org.mifosplatform.infrastructure.dataqueries.data.DatatableData;
import org.mifosplatform.infrastructure.dataqueries.data.GenericResultsetData;
import org.mifosplatform.infrastructure.dataqueries.service.GenericDataService;
import org.mifosplatform.infrastructure.dataqueries.service.ReadWriteNonCoreDataService;
import org.mifosplatform.infrastructure.security.exception.NoAuthorizationException;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.useradministration.domain.AppUser;
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
	private final GenericDataService genericDataService;
	private final ReadWriteNonCoreDataService readWriteNonCoreDataService;
	private final ToApiJsonSerializer<GenericResultsetData> toApiJsonSerializer;
	private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

	@Autowired
	public DataTableApiResource(
			final PlatformSecurityContext context,
			final GenericDataService genericDataService,
			final ReadWriteNonCoreDataService readWriteNonCoreDataService,
			ToApiJsonSerializer<GenericResultsetData> toApiJsonSerializer,
			final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService) {
		this.context = context;
		this.genericDataService = genericDataService;
		this.readWriteNonCoreDataService = readWriteNonCoreDataService;
		this.toApiJsonSerializer = toApiJsonSerializer;
		this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
	}

	@GET
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String getDatatables(@QueryParam("apptable") final String apptable,
			@Context final UriInfo uriInfo) {

		final List<DatatableData> result = this.readWriteNonCoreDataService
				.retrieveDatatableNames(apptable);

		final boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo
				.getQueryParameters());
		return this.toApiJsonSerializer.serializePretty(prettyPrint, result);
	}

	@POST
	@Path("register/{datatable}/{apptable}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String registerDatatable(
			@PathParam("datatable") final String datatable,
			@PathParam("apptable") final String apptable) {

		this.readWriteNonCoreDataService.registerDatatable(datatable, apptable);

		return this.toApiJsonSerializer.serialize(EntityIdentifier.empty());
	}

	@POST
	@Path("deregister/{datatable}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String deregisterDatatable(
			@PathParam("datatable") final String datatable) {

		this.readWriteNonCoreDataService.deregisterDatatable(datatable);

		return this.toApiJsonSerializer.serialize(EntityIdentifier.empty());
	}

	@GET
	@Path("{datatable}/{apptableId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String getDatatable(@PathParam("datatable") final String datatable,
			@PathParam("apptableId") final Long apptableId,
			@QueryParam("order") final String order,
			@Context final UriInfo uriInfo) {

		checkUserPermissionForDatatable(datatable, "READ");

		GenericResultsetData results = this.readWriteNonCoreDataService
				.retrieveDataTableGenericResultSet(datatable, apptableId,
						order, null);

		String json = "";
		final boolean genericResultSet = ApiParameterHelper
				.genericResultSet(uriInfo.getQueryParameters());
		if (genericResultSet) {
			final boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo
					.getQueryParameters());
			json = this.toApiJsonSerializer.serializePretty(prettyPrint,
					results);
		} else {
			json = this.genericDataService
					.generateJsonFromGenericResultsetData(results);
		}

		return json;
	}

	@POST
	@Path("{datatable}/{apptableId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String newDatatableEntry(
			@PathParam("datatable") final String datatable,
			@PathParam("apptableId") final Long apptableId,
			final String apiRequestBodyAsJson) {

		final EntityIdentifier entityIdentifier = this.commandsSourceWritePlatformService
				.logCommandSource("CREATE", datatable, "CREATE", "datatables/"+datatable,
						apptableId, apiRequestBodyAsJson);

		return this.toApiJsonSerializer.serialize(entityIdentifier);
	}

	@PUT
	@Path("{datatable}/{apptableId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String updateDatatableEntryOnetoOne(
			@PathParam("datatable") final String datatable,
			@PathParam("apptableId") final Long apptableId,
			final String apiRequestBodyAsJson) {
		/*
		 * for updating one to one relationships (where foreign key is the
		 * primary key)
		 */
//		checkUserPermissionForDatatable(datatable, "UPDATE");
//		Map<String, String> queryParams = getQueryParamsFromJsonRequestBody(jsonRequestBody);
		
		final EntityIdentifier entityIdentifier = this.commandsSourceWritePlatformService
				.logCommandSource("UPDATE", datatable, "UPDATE", "datatables/"+datatable,
						apptableId, apiRequestBodyAsJson);

//		this.readWriteNonCoreDataService.updateDatatableEntryOnetoOne(
//				datatable, apptableId, queryParams);
//
//		EntityIdentifier entityIdentifier = EntityIdentifier.resourceResult(
//				Long.valueOf(apptableId), null);

		return this.toApiJsonSerializer.serialize(entityIdentifier);
	}

	@PUT
	@Path("{datatable}/{apptableId}/{datatableId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String updateDatatableEntryOnetoOne(
			@PathParam("datatable") final String datatable,
			@PathParam("apptableId") final Long apptableId,
			@PathParam("datatableId") final Long datatableId,
			final String apiRequestBodyAsJson) {
		/*
		 * for updating one to many relationships (where foreign key isn't the
		 * primary key)
		 */
		
		final EntityIdentifier entityIdentifier = this.commandsSourceWritePlatformService
				.logCommandSource("UPDATE", datatable, "UPDATE_MULTIPLE", "datatables/"+datatable, apptableId, "datatables/"+datatable, datatableId, apiRequestBodyAsJson);

		return this.toApiJsonSerializer.serialize(entityIdentifier);
		
//		checkUserPermissionForDatatable(datatable, "UPDATE");
//		Map<String, String> queryParams = getQueryParamsFromJsonRequestBody(jsonRequestBody);
//
//		this.readWriteNonCoreDataService.updateDatatableEntryOnetoMany(
//				datatable, apptableId, datatableId, queryParams);
//
//		EntityIdentifier entityIdentifier = EntityIdentifier.resourceResult(
//				Long.valueOf(apptableId), null);
//
//		return this.toApiJsonSerializer.serialize(entityIdentifier);
	}

	@DELETE
	@Path("{datatable}/{apptableId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String deleteDatatableEntries(
			@PathParam("datatable") final String datatable,
			@PathParam("apptableId") final Long apptableId) {

		final EntityIdentifier entityIdentifier = this.commandsSourceWritePlatformService
				.logCommandSource("DELETE", datatable, "DELETE", "datatables/"+datatable,
						apptableId, "{}");
		
		return this.toApiJsonSerializer.serialize(entityIdentifier);
	}

	@DELETE
	@Path("{datatable}/{apptableId}/{datatableId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String deleteDatatableEntries(
			@PathParam("datatable") final String datatable,
			@PathParam("apptableId") final Long apptableId,
			@PathParam("datatableId") final Long datatableId) {

		final EntityIdentifier entityIdentifier = this.commandsSourceWritePlatformService
				.logCommandSource("DELETE", datatable, "DELETE_MULTIPLE", "datatables/"+datatable, apptableId, "datatables/"+datatable, datatableId, "{}");

		return this.toApiJsonSerializer.serialize(entityIdentifier);
	}

	private Map<String, String> getQueryParamsFromJsonRequestBody(
			final String jsonRequestBody) {

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

	private void checkUserPermissionForDatatable(final String datatable,
			final String accessType) {
		AppUser currentUser = context.authenticatedUser();
		if (currentUser.hasNotPermissionForDatatable(datatable, accessType)) {
			throw new NoAuthorizationException("Not Authorised to "
					+ accessType + " Data Table: " + datatable);
		}
	}
}