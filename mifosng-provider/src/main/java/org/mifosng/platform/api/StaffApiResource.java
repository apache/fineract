package org.mifosng.platform.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosng.platform.api.commands.LoanReassignmentCommand;
import org.mifosng.platform.api.commands.StaffCommand;
import org.mifosng.platform.api.data.EntityIdentifier;
import org.mifosng.platform.api.data.LoanReassignmentData;
import org.mifosng.platform.api.data.OfficeLookup;
import org.mifosng.platform.api.data.StaffAccountSummaryCollectionData;
import org.mifosng.platform.api.data.StaffData;
import org.mifosng.platform.api.infrastructure.PortfolioApiDataConversionService;
import org.mifosng.platform.api.infrastructure.PortfolioApiJsonSerializerService;
import org.mifosng.platform.infrastructure.api.ApiParameterHelper;
import org.mifosng.platform.loan.service.LoanWritePlatformService;
import org.mifosng.platform.organisation.service.OfficeReadPlatformService;
import org.mifosng.platform.security.PlatformSecurityContext;
import org.mifosng.platform.staff.service.StaffReadPlatformService;
import org.mifosng.platform.staff.service.StaffWritePlatformService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/staff")
@Component
@Scope("singleton")
public class StaffApiResource {

	@Autowired
	private StaffReadPlatformService readPlatformService;

	@Autowired
	private StaffWritePlatformService writePlatformService;

	@Autowired
	private PortfolioApiDataConversionService apiDataConversionService;

	@Autowired
	private PortfolioApiJsonSerializerService apiJsonSerializerService;

	@Autowired
	private OfficeReadPlatformService officeReadPlatformService;

    @Autowired
    private LoanWritePlatformService loanWritePlatformService;

	private final String entityType = "STAFF";
	@Autowired
	private PlatformSecurityContext context;
	
	private final static Logger logger = LoggerFactory
			.getLogger(StaffApiResource.class);

	@GET
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveStaff(@Context final UriInfo uriInfo,
			@QueryParam("sqlSearch") final String sqlSearch,
			@QueryParam("officeId") final Integer officeId) {

    	context.authenticatedUser().validateHasReadPermission(entityType);
    	
		Set<String> responseParameters = ApiParameterHelper
				.extractFieldsForResponseIfProvided(uriInfo
						.getQueryParameters());
		boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo
				.getQueryParameters());

		final String extraCriteria = getStaffCriteria(sqlSearch, officeId);
		final Collection<StaffData> staff = this.readPlatformService
				.retrieveAllStaff(extraCriteria);

		return this.apiJsonSerializerService.serializeStaffDataToJson(
				prettyPrint, responseParameters, staff);
	}

	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response createStaff(final String jsonRequestBody) {

		final StaffCommand command = this.apiDataConversionService
				.convertJsonToStaffCommand(null, jsonRequestBody);

		final Long staffId = this.writePlatformService.createStaff(command);

		return Response.ok().entity(new EntityIdentifier(staffId)).build();
	}

	@GET
	@Path("{staffId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retreiveStaff(@PathParam("staffId") final Long staffId,
			@Context final UriInfo uriInfo) {

    	context.authenticatedUser().validateHasReadPermission(entityType);
    	
		final Set<String> responseParameters = ApiParameterHelper
				.extractFieldsForResponseIfProvided(uriInfo
						.getQueryParameters());
		final boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo
				.getQueryParameters());
		boolean template = ApiParameterHelper.template(uriInfo
				.getQueryParameters());

		final StaffData staff = this.readPlatformService.retrieveStaff(staffId);
		if (template) {
			staff.setAllowedOffices(new ArrayList<OfficeLookup>(
					officeReadPlatformService.retrieveAllOfficesForLookup()));
		}

		return this.apiJsonSerializerService.serializeStaffDataToJson(
				prettyPrint, responseParameters, staff);
	}

	@PUT
	@Path("{staffId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response updateStaff(@PathParam("staffId") final Long staffId,
			final String jsonRequestBody) {

		final StaffCommand command = this.apiDataConversionService
				.convertJsonToStaffCommand(staffId, jsonRequestBody);

		final Long entityId = this.writePlatformService.updateStaff(command);

		return Response.ok().entity(new EntityIdentifier(entityId)).build();
	}

    @POST
    @Path("loanreassignment")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public Response loanReassignment(final String jsonRequestBody){

        LoanReassignmentCommand command =
                this.apiDataConversionService.convertJsonToBulkLoanReassignmentCommand(jsonRequestBody);

        EntityIdentifier loanOfficerIdentifier = this.loanWritePlatformService.bulkLoanReassignment(command);

        return Response.ok().entity(loanOfficerIdentifier).build();
    }

    @GET
    @Path("loanreassignment/template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String loanReassignmentTemplate(@QueryParam("officeId") final Long officeId,
                                           @QueryParam("fromLoanOfficerId") final Long loanOfficerId,
                                           @Context final UriInfo uriInfo){

    	context.authenticatedUser().validateHasReadPermission("LOAN");
    	
        final Set<String> responseParameters = ApiParameterHelper
                .extractFieldsForResponseIfProvided(uriInfo
                        .getQueryParameters());

        final boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());

        final Collection<OfficeLookup> offices = this.officeReadPlatformService.retrieveAllOfficesForLookup();

        Collection<StaffData> loanOfficers = null;
        StaffAccountSummaryCollectionData staffAccountSummaryCollectionData = null;

        if (officeId != null) {
            loanOfficers = this.readPlatformService.retrieveAllLoanOfficersByOffice(officeId);
        }
        
        if (loanOfficerId != null) {
            staffAccountSummaryCollectionData = this.readPlatformService.retrieveLoanOfficerAccountSummary(loanOfficerId);
        }

        final LoanReassignmentData loanReassignmentData = LoanReassignmentData.templateForBulk(officeId, loanOfficerId,
                new LocalDate(), offices, loanOfficers, staffAccountSummaryCollectionData);

        return this.apiJsonSerializerService.serializeLoanReassignmentDataToJson(prettyPrint, responseParameters, loanReassignmentData);
    }

	private String getStaffCriteria(String sqlSearch, Integer officeId) {

		String extraCriteria = "";

		if (sqlSearch != null) {
			extraCriteria = " and (" + sqlSearch + ")";
		}
		if (officeId != null) {
			extraCriteria += " and office_id = " + officeId;
		}

		if (StringUtils.isNotBlank(extraCriteria)) {
			extraCriteria = extraCriteria.substring(4);
		}
		logger.info("extraCriteria; " + extraCriteria);

		return extraCriteria;
	}

}