package org.apache.fineract.portfolio.clientaddress.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.clientaddress.data.AddressData;
import org.apache.fineract.portfolio.clientaddress.data.FieldConfigurationData;
import org.apache.fineract.portfolio.clientaddress.service.AddressReadPlatformServiceImpl;
import org.apache.fineract.portfolio.clientaddress.service.FieldConfigurationReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/clients/addresses")
@Component
@Scope("singleton")
public class AddressConfigurationApiResources 
{
    private final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<>(Arrays.asList("clientAddressId","client_id","address_id","address_type_id","is_active",
            "fieldConfigurationId","entity","table","field",
            "is_enabled","is_mandatory","validation_regex"));
    private final String resourceNameForPermissions = "Address";
    private final PlatformSecurityContext context;
    private final AddressReadPlatformServiceImpl readPlatformService; 
    private final DefaultToApiJsonSerializer<AddressData> toApiJsonSerializer;
    private final FieldConfigurationReadPlatformService readPlatformServicefld;
    private final DefaultToApiJsonSerializer<FieldConfigurationData> toApiJsonSerializerfld;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    
    @Autowired
    public AddressConfigurationApiResources(final PlatformSecurityContext context,final AddressReadPlatformServiceImpl readPlatformService,
            final DefaultToApiJsonSerializer<AddressData> toApiJsonSerializer,final FieldConfigurationReadPlatformService readPlatformServicefld,
            final DefaultToApiJsonSerializer<FieldConfigurationData> toApiJsonSerializerfld,final ApiRequestParameterHelper apiRequestParameterHelper,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService)
    {
        this.context=context;
        this.readPlatformService=readPlatformService;
        this.toApiJsonSerializer=toApiJsonSerializer;
        this.readPlatformServicefld=readPlatformServicefld;
        this.toApiJsonSerializerfld=toApiJsonSerializerfld;
        this.apiRequestParameterHelper=apiRequestParameterHelper;
        this.commandsSourceWritePlatformService=commandsSourceWritePlatformService;
    }
    
    @GET
    @Path("/fieldconfiguration/{entity}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String getAddresses(@PathParam("entity")final String entityname,@Context final UriInfo uriInfo)
    {
        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final Collection<FieldConfigurationData> fldconfig = this.readPlatformServicefld.retrieveFieldConfiguration(entityname);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializerfld.serialize(settings, fldconfig, this.RESPONSE_DATA_PARAMETERS);
       
    }
    
    @GET
    @Path("/template/")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String getAddressesTemplate(@Context final UriInfo uriInfo)
    {
        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final AddressData template = this.readPlatformService.retrieveTemplate();

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, template, this.RESPONSE_DATA_PARAMETERS);
       
    }
    
    
  
}
