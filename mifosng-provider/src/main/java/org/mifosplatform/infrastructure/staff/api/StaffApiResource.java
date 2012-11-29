package org.mifosplatform.infrastructure.staff.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
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
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.mifosng.platform.api.data.EntityIdentifier;
import org.mifosng.platform.api.infrastructure.PortfolioApiJsonSerializerService;
import org.mifosng.platform.infrastructure.api.ApiParameterHelper;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.infrastructure.office.data.OfficeLookup;
import org.mifosplatform.infrastructure.office.service.OfficeReadPlatformService;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.infrastructure.staff.data.StaffData;
import org.mifosplatform.infrastructure.staff.service.StaffReadPlatformService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/staff")
@Component
@Scope("singleton")
public class StaffApiResource {

    private final static Logger logger = LoggerFactory.getLogger(StaffApiResource.class);

    private final PlatformSecurityContext context;
    private final StaffReadPlatformService readPlatformService;
    private final OfficeReadPlatformService officeReadPlatformService;
    private final PortfolioApiJsonSerializerService apiJsonSerializerService;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    private final String resourceNameForPermissions = "STAFF";

    @Autowired
    public StaffApiResource(final PlatformSecurityContext context, final StaffReadPlatformService readPlatformService,
            final OfficeReadPlatformService officeReadPlatformService, final PortfolioApiJsonSerializerService apiJsonSerializerService,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService) {
        this.context = context;
        this.readPlatformService = readPlatformService;
        this.officeReadPlatformService = officeReadPlatformService;
        this.apiJsonSerializerService = apiJsonSerializerService;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveStaff(@Context final UriInfo uriInfo, @QueryParam("sqlSearch") final String sqlSearch,
            @QueryParam("officeId") final Integer officeId) {

        context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);

        final Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
        final boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());

        final String extraCriteria = getStaffCriteria(sqlSearch, officeId);
        final Collection<StaffData> staff = this.readPlatformService.retrieveAllStaff(extraCriteria);

        return this.apiJsonSerializerService.serializeStaffDataToJson(prettyPrint, responseParameters, staff);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String createStaff(final String apiRequestBodyAsJson) {

        final List<String> allowedPermissions = Arrays.asList("ALL_FUNCTIONS", "ORGANISATION_ADMINISTRATION_SUPER_USER", "CREATE_STAFF");
        context.authenticatedUser().validateHasPermissionTo("CREATE_STAFF", allowedPermissions);

        final EntityIdentifier result = this.commandsSourceWritePlatformService.logCommandSource("CREATE", "staff", null,
                apiRequestBodyAsJson);

        return this.apiJsonSerializerService.serializeEntityIdentifier(result);
    }

    @GET
    @Path("{staffId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retreiveStaff(@PathParam("staffId") final Long staffId, @Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);

        final Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
        final boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());
        boolean template = ApiParameterHelper.template(uriInfo.getQueryParameters());

        StaffData staff = this.readPlatformService.retrieveStaff(staffId);
        if (template) {
            final Collection<OfficeLookup> allowedOffices = officeReadPlatformService.retrieveAllOfficesForLookup();
            staff = StaffData.templateData(staff, allowedOffices);
        }

        return this.apiJsonSerializerService.serializeStaffDataToJson(prettyPrint, responseParameters, staff);
    }

    @PUT
    @Path("{staffId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String updateStaff(@PathParam("staffId") final Long staffId, final String apiRequestBodyAsJson) {

        final List<String> allowedPermissions = Arrays.asList("ALL_FUNCTIONS", "ORGANISATION_ADMINISTRATION_SUPER_USER", "UPDATE_STAFF");
        context.authenticatedUser().validateHasPermissionTo("UPDATE_STAFF", allowedPermissions);

        final EntityIdentifier result = this.commandsSourceWritePlatformService.logCommandSource("UPDATE", "staff", staffId,
                apiRequestBodyAsJson);

        return this.apiJsonSerializerService.serializeEntityIdentifier(result);
    }

    private String getStaffCriteria(final String sqlSearch, final Integer officeId) {

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
        logger.debug("extraCriteria; " + extraCriteria);

        return extraCriteria;
    }
}