package org.mifosng.platform.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.mifosng.platform.api.commands.DepositProductCommand;
import org.mifosng.platform.api.data.DepositProductData;
import org.mifosng.platform.api.data.EntityIdentifier;
import org.mifosng.platform.api.infrastructure.ApiDataConversionService;
import org.mifosng.platform.api.infrastructure.ApiParameterHelper;
import org.mifosng.platform.depositproduct.service.DepositProductReadPlatformService;
import org.mifosng.platform.depositproduct.service.DepositProductWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/depositproducts")
@Component
@Scope("singleton")
public class DepositProductsApiResource {
	
	@Autowired
	private DepositProductReadPlatformService depositProductReadPlatformService;
	
	@Autowired 
	private DepositProductWritePlatformService depositProductWritePlatformService;
	
	@Autowired
	private ApiDataConversionService apiDataConversionService;
	
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public Response createDepositProduct(final String jsonRequestBody){
		
		final DepositProductCommand command=this.apiDataConversionService.convertJsonToDepositProductCommand(null, jsonRequestBody);
		
		EntityIdentifier entityIdentifier=this.depositProductWritePlatformService.createDepositProduct(command);
		
		return Response.ok().entity(entityIdentifier).build();
		
	}
	
	@PUT
	@Path("{productId}")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public Response updateDepositProduct(@PathParam("productId") final Long productId, final String jsonRequestBody){
		
		final DepositProductCommand command=this.apiDataConversionService.convertJsonToDepositProductCommand(productId, jsonRequestBody);
		EntityIdentifier entityIdentifier=this.depositProductWritePlatformService.updateDepositProduct(command);
		return Response.ok().entity(entityIdentifier).build();
	}
	
	@DELETE
	@Path("{productId}")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public Response deleteDepositProduct(@PathParam("productId") final Long productId) {

		this.depositProductWritePlatformService.deleteDepositProduct(productId);

		return Response.ok(new EntityIdentifier(productId)).build();
	}
	
	@GET
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public String retrieveAllDepositProducts(@Context final UriInfo uriInfo){
		Set<String> typicalResponseParameters = new HashSet<String>(Arrays.asList("id", "name", "description", "createdOn", "lastModifedOn",
				"currencyCode","digitsAfterDecimal","minimumBalance","maximumBalance"));
		
		Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
		if (responseParameters.isEmpty()) {
			responseParameters.addAll(typicalResponseParameters);
		}
		boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());
		
		Collection<DepositProductData> products=this.depositProductReadPlatformService.retrieveAllDepositProducts();
		return this.apiDataConversionService.convertDepositProductDataToJson(prettyPrint, responseParameters, products.toArray(new DepositProductData[products.size()]));
	}
	
	@GET
	@Path("{productId}")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public String retrieveDepositProductDetails(@PathParam("productId") final Long productId, @Context final UriInfo uriInfo){
		Set<String> typicalResponseParameters = new HashSet<String>(Arrays.asList("id", "name", "description", "createdOn", "lastModifedOn",
				"currencyCode","digitsAfterDecimal","minimumBalance","maximumBalance"));
		
		Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
		if (responseParameters.isEmpty()) {
			responseParameters.addAll(typicalResponseParameters);
		}
		boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());
		boolean template = ApiParameterHelper.template(uriInfo.getQueryParameters());
		if (template) {
			responseParameters.addAll(Arrays.asList("currencyOptions"));
		}
		DepositProductData productData = this.depositProductReadPlatformService.retrieveDepositProductData(productId);
		return this.apiDataConversionService.convertDepositProductDataToJson(prettyPrint, responseParameters, productData);
	}

	@GET
	@Path("template")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public String retrieveNewDepositProductDetails(@Context final UriInfo uriInfo) {
		
		Set<String> typicalResponseParameters = new HashSet<String>(
				Arrays.asList("id", "name", "description", "createdOn", "lastModifedOn","currencyCode","digitsAfterDecimal", "currencyOptions","minimumBalance","maximumBalance"));
		
		Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
		if (responseParameters.isEmpty()) {
			responseParameters.addAll(typicalResponseParameters);
		}
		boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());

		DepositProductData depositProduct = this.depositProductReadPlatformService.retrieveNewDepositProductDetails();
		
		return this.apiDataConversionService.convertDepositProductDataToJson(prettyPrint, responseParameters, depositProduct);
	}	
	
}
