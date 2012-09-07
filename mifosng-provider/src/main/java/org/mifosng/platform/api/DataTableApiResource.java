package org.mifosng.platform.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.mifosng.platform.api.data.GenericResultsetData;
import org.mifosng.platform.api.infrastructure.ApiJsonSerializerService;
import org.mifosng.platform.api.infrastructure.ApiParameterHelper;
import org.mifosng.platform.exceptions.NoAuthorizationException;
import org.mifosng.platform.noncore.ReadWriteNonCoreDataService;
import org.mifosng.platform.security.PlatformSecurityContext;
import org.mifosng.platform.user.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/datatables")
@Component
@Scope("singleton")
public class DataTableApiResource {

	// private final static Logger logger =
	// LoggerFactory.getLogger(DataTableApiResource.class);

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
	@Path("{datatable}/{id}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String getDatatable(@PathParam("datatable") final String datatable,
			@PathParam("id") final Long id,
			@QueryParam("sqlFields") final String sqlFields,
			@QueryParam("sqlSearch") final String sqlSearch,
			@QueryParam("sqlOrder") final String sqlOrder,
			@Context final UriInfo uriInfo) {

		checkUserPermissionForDatatable(datatable, "READ");

		/*
		 * Dont use this for now... but its the code for returning data as json
		 * objects rather than a generic resultset String resultStr =
		 * this.readWriteNonCoreDataService
		 * .retrieveDataTableJSONObject(datatable, id, sqlFields, sqlSearch,
		 * sqlOrder);
		 */

		GenericResultsetData results = this.readWriteNonCoreDataService
				.retrieveDataTableGenericResultSet(datatable, id, sqlFields,
						sqlSearch, sqlOrder);

		boolean prettyPrints = ApiParameterHelper.prettyPrint(uriInfo
				.getQueryParameters());
		return this.apiJsonSerializerService
				.serializeGenericResultsetDataToJson(prettyPrints, results);

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
