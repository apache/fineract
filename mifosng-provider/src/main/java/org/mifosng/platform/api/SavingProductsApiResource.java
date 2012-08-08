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

import org.mifosng.platform.api.commands.SavingProductCommand;
import org.mifosng.platform.api.data.EntityIdentifier;
import org.mifosng.platform.api.data.SavingProductData;
import org.mifosng.platform.api.infrastructure.ApiDataConversionService;
import org.mifosng.platform.api.infrastructure.ApiParameterHelper;
import org.mifosng.platform.savingproduct.service.SavingProductReadPlatformService;
import org.mifosng.platform.savingproduct.service.SavingProductWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/savingproducts")
@Component
@Scope("singleton")
public class SavingProductsApiResource {
	
	@Autowired
	private SavingProductReadPlatformService savingProductReadPlatformService;
	
	@Autowired
	private SavingProductWritePlatformService savingProductWritePlatformService;

	@Autowired
	private ApiDataConversionService apiDataConversionService;
	
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public Response createSavingProduct(final String jsonRequestBody){
		
		final SavingProductCommand command=this.apiDataConversionService.convertJsonToSavingProductCommand(null, jsonRequestBody);
		
		EntityIdentifier entityIdentifier=this.savingProductWritePlatformService.createSavingProduct(command);
		
		return Response.ok().entity(entityIdentifier).build();
	}
	
	@PUT
	@Path("{productId}")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public Response updateSavingProduct(@PathParam("productId") final Long productId, final String jsonRequestBody){
		
		SavingProductCommand command=this.apiDataConversionService.convertJsonToSavingProductCommand(productId, jsonRequestBody);
		EntityIdentifier entityIdentifier=this.savingProductWritePlatformService.updateSavingProduct(command);
		return Response.ok().entity(entityIdentifier).build();
	}
	
	@GET
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public String retrieveAllSavingProducts(@Context final UriInfo uriInfo) {

		Set<String> typicalResponseParameters = new HashSet<String>(Arrays.asList("id", "name", "description", "createdOn", "lastModifedOn",
				"interestRate","currencyCode","digitsAfterDecimal","minimumBalance","maximumBalance"));
		
		Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
		if (responseParameters.isEmpty()) {
			responseParameters.addAll(typicalResponseParameters);
		}
		boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());

		Collection<SavingProductData> products = this.savingProductReadPlatformService.retrieveAllSavingProducts();
		
		return this.apiDataConversionService.convertSavingProductDataToJson(prettyPrint, responseParameters, products.toArray(new SavingProductData[products.size()]));
	}
	
	@GET
	@Path("{productId}")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public String retrieveSavingProductDetails(@PathParam("productId") final Long productId, @Context final UriInfo uriInfo) {

		Set<String> typicalResponseParameters = new HashSet<String>(Arrays.asList("id", "name", "description", "createdOn", "lastModifedOn",
				"interestRate","currencyCode","digitsAfterDecimal","minimumBalance","maximumBalance"));
		
		Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
		if (responseParameters.isEmpty()) {
			responseParameters.addAll(typicalResponseParameters);
		}
		boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());
		boolean template = ApiParameterHelper.template(uriInfo.getQueryParameters());
		if (template) {
			responseParameters.addAll(Arrays.asList("currencyOptions"));
		}
		
		SavingProductData savingProduct = this.savingProductReadPlatformService.retrieveSavingProduct(productId);
		
		return this.apiDataConversionService.convertSavingProductDataToJson(prettyPrint, responseParameters, savingProduct);
	}
	
	@GET
	@Path("template")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public String retrieveNewSavingProductDetails(@Context final UriInfo uriInfo) {
		
		Set<String> typicalResponseParameters = new HashSet<String>(
				Arrays.asList("id", "name", "description", "createdOn", "lastModifedOn","interestRate","currencyCode","digitsAfterDecimal", "currencyOptions","minimumBalance","maximumBalance"));
		
		Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
		if (responseParameters.isEmpty()) {
			responseParameters.addAll(typicalResponseParameters);
		}
		boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());

		SavingProductData savingProduct = this.savingProductReadPlatformService.retrieveNewSavingProductDetails();
		
		return this.apiDataConversionService.convertSavingProductDataToJson(prettyPrint, responseParameters, savingProduct);
	}
	
	@DELETE
	@Path("{productId}")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public Response deleteProduct(@PathParam("productId") final Long productId) {

		this.savingProductWritePlatformService.deleteSavingProduct(productId);

		return Response.ok(new EntityIdentifier(productId)).build();
	}
}