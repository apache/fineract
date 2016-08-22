package org.apache.fineract.CreditCheck.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.apache.fineract.CreditCheck.data.CreditBureauData;
import org.apache.fineract.CreditCheck.data.CreditBureauLpMappingData;
import org.apache.fineract.CreditCheck.data.OrganisationCreditbureauData;
import org.apache.fineract.CreditCheck.service.CreditBureauLpMappingReadPlatformService;
import org.apache.fineract.CreditCheck.service.CreditBureauReadPlatformService;
import org.apache.fineract.CreditCheck.service.OrganisationCreditBureauReadPlatformService;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;



@Path("/cbconfig")
@Component
@Scope("singleton")
public class CreditBureauConfigurationAPI 
{
    private final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<>(Arrays.asList("cb_id","alias",
            "country","cb_product_id","start_date","end_date","is_active"));
    private final String resourceNameForPermissions = "CreditBureau";
    private final PlatformSecurityContext context;
    private final CreditBureauReadPlatformService readPlatformService; 
    private final DefaultToApiJsonSerializer<CreditBureauData> toApiJsonSerializer;
    private final CreditBureauLpMappingReadPlatformService readPlatformServiceCbLp;
    private final OrganisationCreditBureauReadPlatformService readPlatformServiceOrgCb;
    private final DefaultToApiJsonSerializer<CreditBureauLpMappingData> toApiJsonSerializerCbLp; 
    private final DefaultToApiJsonSerializer<OrganisationCreditbureauData> toApiJsonSerializerOrgCb; 
  /*  private final CreditBureauMasterReadPlatformService readPlatformServiceMaster;
    private final DefaultToApiJsonSerializer<CreditBureauData> toApiJsonSerializer;  
    private final DefaultToApiJsonSerializer<CreditBureauLpMappingData> toApiJsonSerializerCbLp; 
    private final DefaultToApiJsonSerializer<CreditBureauProduct> toApiJsonSerializerCbp; 
    private final DefaultToApiJsonSerializer<CreditBureauMasterData> toApiJsonSerializerCbm; */
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
  
 /*   
    @Autowired
    public CreditBureauConfigurationAPI(final PlatformSecurityContext context, final CreditBureauReadPlatformService readPlatformService,
            final CreditBureauLpMappingReadPlatformService readPlatformServiceCbLp,final DefaultToApiJsonSerializer<CreditBureauProduct> toApiJsonSerializerCbp,
            final CreditBureauMasterReadPlatformService readPlatformServiceMaster,
            final DefaultToApiJsonSerializer<CreditBureauData> toApiJsonSerializer,final DefaultToApiJsonSerializer<CreditBureauLpMappingData> toApiJsonSerializerCbLp,
            final DefaultToApiJsonSerializer<CreditBureauMasterData> toApiJsonSerializerCbm,final ApiRequestParameterHelper apiRequestParameterHelper,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService)
    {
        
    this.context = context;
    this.readPlatformService = readPlatformService;
    this.readPlatformServiceCbLp=readPlatformServiceCbLp;
    this.toApiJsonSerializerCbp=toApiJsonSerializerCbp;
    this.readPlatformServiceMaster=readPlatformServiceMaster;
    this.toApiJsonSerializer = toApiJsonSerializer;
    this.toApiJsonSerializerCbLp=toApiJsonSerializerCbLp;
    this.toApiJsonSerializerCbm=toApiJsonSerializerCbm;
    this.apiRequestParameterHelper = apiRequestParameterHelper;
    this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
 
        
    }*/
    
    @Autowired
    public CreditBureauConfigurationAPI(final PlatformSecurityContext context, final CreditBureauReadPlatformService readPlatformService, final DefaultToApiJsonSerializer<CreditBureauData> toApiJsonSerializer,
            final CreditBureauLpMappingReadPlatformService readPlatformServiceCbLp,final DefaultToApiJsonSerializer<CreditBureauLpMappingData> toApiJsonSerializerCbLp,
            final OrganisationCreditBureauReadPlatformService readPlatformServiceOrgCb,final DefaultToApiJsonSerializer<OrganisationCreditbureauData> toApiJsonSerializerOrgCb,final ApiRequestParameterHelper apiRequestParameterHelper,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService)
    {
        this.context=context;
        this.readPlatformService=readPlatformService;
        this.apiRequestParameterHelper=apiRequestParameterHelper;
        this.readPlatformServiceCbLp=readPlatformServiceCbLp;
        this.toApiJsonSerializerCbLp=toApiJsonSerializerCbLp;
        this.readPlatformServiceOrgCb=readPlatformServiceOrgCb;
        this.toApiJsonSerializerOrgCb=toApiJsonSerializerOrgCb;
        this.toApiJsonSerializer=toApiJsonSerializer;
        this.commandsSourceWritePlatformService=commandsSourceWritePlatformService;
    }
    
    
    
    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String getCreditBureau(@Context final UriInfo uriInfo)
    {
        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final Collection<CreditBureauData> creditBureau = this.readPlatformService.retrieveCreditBureau();

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, creditBureau, this.RESPONSE_DATA_PARAMETERS);
       
    }
    
  @GET
    @Path("/mappings")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String getCreditBureauLPMapping(@Context final UriInfo uriInfo)
    {
        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final Collection<CreditBureauLpMappingData> creditBureauLpMapping = this.readPlatformServiceCbLp.readCreditBureauLpMapping();

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializerCbLp.serialize(settings, creditBureauLpMapping, this.RESPONSE_DATA_PARAMETERS);
       
   
           
    }
  
  @GET
  @Path("/orgcb")
  @Consumes({ MediaType.APPLICATION_JSON })
  @Produces({ MediaType.APPLICATION_JSON })
  public String getOrganisationCreditBureau(@Context final UriInfo uriInfo)
  {
      this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

      final Collection<OrganisationCreditbureauData> organisationCreditBureau = this.readPlatformServiceOrgCb.retrieveOrgCreditBureau();

      final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
      return this.toApiJsonSerializerOrgCb.serialize(settings, organisationCreditBureau, this.RESPONSE_DATA_PARAMETERS);
     
 
      
     
  }
  
  @PUT
  @Path("/orgcb")
  @Consumes({ MediaType.APPLICATION_JSON })
  @Produces({ MediaType.APPLICATION_JSON })
  public String updateCreditBureau(final String apiRequestBodyAsJson) {

      final CommandWrapper commandRequest = new CommandWrapperBuilder().updateCreditBureau().withJson(apiRequestBodyAsJson).build();

      final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

      return this.toApiJsonSerializer.serialize(result);
  }
  
  
  @PUT
  @Path("/mappings")
  @Consumes({ MediaType.APPLICATION_JSON })
  @Produces({ MediaType.APPLICATION_JSON })
  public String updateCbLpMapping(final String apiRequestBodyAsJson) {

      final CommandWrapper commandRequest = new CommandWrapperBuilder().updateCbLpMapping().withJson(apiRequestBodyAsJson).build();

      final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

      return this.toApiJsonSerializer.serialize(result);
  }
  
  
  @POST
  @Path("/orgcb/{ocbId}")
  @Consumes({ MediaType.APPLICATION_JSON })
  @Produces({ MediaType.APPLICATION_JSON })
  public String addOrgCreditBureau(@PathParam("ocbId") final Long ocbId,final String apiRequestBodyAsJson) {

      final CommandWrapper commandRequest = new CommandWrapperBuilder().addOrgCreditBureau(ocbId).withJson(apiRequestBodyAsJson).build();

      final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

      return this.toApiJsonSerializer.serialize(result);
  }
  
  @POST
  @Path("/mappings/{cbId}")
  @Consumes({ MediaType.APPLICATION_JSON })
  @Produces({ MediaType.APPLICATION_JSON })
  public String createCB_LP_Mapping(@PathParam("cbId") final Long cbId,final String apiRequestBodyAsJson) {

      final CommandWrapper commandRequest = new CommandWrapperBuilder().createCB_LP_Mapping(cbId).withJson(apiRequestBodyAsJson).build();

      final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

      return this.toApiJsonSerializer.serialize(result);
  }
  
  
  @GET
  @Path("/lp")
  @Consumes({ MediaType.APPLICATION_JSON })
  @Produces({ MediaType.APPLICATION_JSON })
  public String fetchLoanProducts(@Context final UriInfo uriInfo)
  {
      this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

      final Collection<CreditBureauLpMappingData> creditBureauLpMapping = this.readPlatformServiceCbLp.fetchLoanProducts();

      final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
      return this.toApiJsonSerializerCbLp.serialize(settings, creditBureauLpMapping, this.RESPONSE_DATA_PARAMETERS);
     }
    
/* 
    
    @GET
    @Path("/mappings/{cbId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String getCreditBureauProductByCreditBureau(@PathParam("cbId") final long cbId,@Context final UriInfo uriInfo)
    {
        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final Collection<CreditBureauProduct> creditBureauLpMapping = this.readPlatformServiceCbLp.findCreditBureauProductByCreditBureau(cbId);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializerCbp.serialize(settings, creditBureauLpMapping, this.RESPONSE_DATA_PARAMETERS);
       
    }
    
    
    @GET
    @Path("/dropdown")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String getDataForDropdown(@Context final UriInfo uriInfo,@QueryParam("country") final String country)
    {
        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        Collection<CreditBureauMasterData> creditBureauByCountry;
        if(country==null)
        {
            creditBureauByCountry = this.readPlatformServiceMaster.retrieveCreditBureauByCountry(); 
           
        }
        else
        {
          creditBureauByCountry = this.readPlatformServiceMaster.retrieveCreditBureauByCountry(country);   
            
        }
        
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializerCbm.serialize(settings, creditBureauByCountry, this.RESPONSE_DATA_PARAMETERS);
       
       
    }
    
    @GET
    @Path("/dropdown/{country}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String getCbBycountry(@Context final UriInfo uriInfo,@PathParam("country") final String country)
    {
        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        Collection<CreditBureauMasterData> creditBureauByCountry;
        if(country==null)
        {
            creditBureauByCountry = this.readPlatformServiceMaster.retrieveCreditBureauByCountry(); 
           
        }
        else
        {
          creditBureauByCountry = this.readPlatformServiceMaster.retrieveCreditBureauByCountry(country);   
            
        }
        
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializerCbm.serialize(settings, creditBureauByCountry, this.RESPONSE_DATA_PARAMETERS);
       
       
    }
    
     @GET
    @Path("/lp")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String fetchLoanProducts(@Context final UriInfo uriInfo)
    {
        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final Collection<CreditBureauLpMappingData> creditBureauLpMapping = this.readPlatformServiceCbLp.fetchLoanProducts();

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializerCbm.serialize(settings, creditBureauLpMapping, this.RESPONSE_DATA_PARAMETERS);
       }
    
    
    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String createCreditBureau(final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createCreditBureau().withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }
    
    
    
    @POST
    @Path("{cbId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String createCB_LP_Mapping(@PathParam("cbId") final Long cbId,final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createCB_LP_Mapping(cbId).withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }
    
    @PUT
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String updateCreditBureau(final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateCreditBureau().withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }*/

    
    
    
}
