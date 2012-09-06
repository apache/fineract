package org.mifosng.platform.api;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.mifosng.platform.InvalidSqlException;
import org.mifosng.platform.api.data.ApiParameterError;
import org.mifosng.platform.exceptions.PlatformApiDataValidationException;
import org.mifosng.platform.noncore.ReadWriteNonCoreDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/datatables")
@Component
@Scope("singleton")
public class DataTableApiResource {

	// private final static Logger logger =
	// LoggerFactory.getLogger(DataTableApiResource.class);

	@Autowired
	private ReadWriteNonCoreDataService readWriteNonCoreDataService;

	@GET
	@Path("{datatable}/{id}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String extraData(@PathParam("datatable") final String datatable,
			@PathParam("id") final Long id,
			@QueryParam("sqlFields") final String sqlFields,
			@QueryParam("sqlSearch") final String sqlSearch,
			@QueryParam("sqlOrder") final String sqlOrder) {

		return this.readWriteNonCoreDataService.retrieveDataTable(datatable, id,
				sqlFields, sqlSearch, sqlOrder);
		/*
		 * try { return this.readWriteNonCoreDataService.retrieveDataTable(
		 * datatable, sqlFields, sqlSearch, sqlOrder); } catch
		 * (InvalidSqlException e) { List<ApiParameterError>
		 * dataValidationErrors = new ArrayList<ApiParameterError>();
		 * ApiParameterError error = ApiParameterError.parameterError(
		 * "extradata.invalid.sql", "The sql is invalid.", "sql", e.getSql());
		 * dataValidationErrors.add(error); throw new
		 * PlatformApiDataValidationException(
		 * "validation.msg.validation.errors.exist", "Validation errors exist.",
		 * dataValidationErrors); }
		 */
	}

}