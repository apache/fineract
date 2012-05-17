package org.mifosng.platform.api;

import java.util.Collection;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.mifosng.data.EntityIdentifier;
import org.mifosng.data.LoanProductData;
import org.mifosng.data.LoanProductList;
import org.mifosng.data.command.LoanProductCommand;
import org.mifosng.platform.loanproduct.service.LoanProductReadPlatformService;
import org.mifosng.platform.loanproduct.service.LoanProductWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/*
 * @see http://viewfromthefringe.blogspot.com/2010/06/versioning-rest-services.html
 * @see http://jersey.576304.n2.nabble.com/Restful-Service-Versioning-td6332833.html
 * 
 * Note: Jersey looks for 'requests' starting with 'api' e.g. this resources url is https://[localhost:8080]/api/v1/loanproducts
 * Note: Named this class *ApiResource as if you leave it as same class name as older 'resource' class, jersey throws a wobbler.
 * Note: no authentication scheme is in place so calls that match api/v1/** are not managed by spring security for now.
 * 
 * 1. resource is versioned here but will move the 'version' part to web.xml when finished.
 * 2. using plural of resource e.g. loanproducts rather than loanproduct
 * 3. @GET results in list of all loan products being returned (no scoping relevant here)
 * 4. Only 'produce'ing JSON by limiting to @Produces({MediaType.APPLICATION_JSON})
 * 5. The java object returned is LoanProductData and LoanProductList when list of products returned. 
 *    These java objects are annotated with  @XmlRootElement so JAXB reader/writers will automatically translate to JSON format.
 * 6. @GET /loanproducts/{productId} results in loan product of that identifier or appropriate error response
 * 7. @POST /loanproducts with correct request body creates new loan product
 * 8. @PUT /loanproducts/{productId} updates loan product of that identifier
 * 9. Using /resource/template pattern for UI specific function of get 'data with defaults' for new resource.
 */
@Path("/v1/loanproducts")
@Component
@Scope("singleton")
public class LoanProductApiResource {

    @Autowired
	private LoanProductReadPlatformService loanProductReadPlatformService;

	@Autowired
	private LoanProductWritePlatformService loanProductWritePlatformService;
	
	@POST
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON})
	public Response createLoanProduct(LoanProductCommand command) {

		EntityIdentifier entityIdentifier = this.loanProductWritePlatformService.createLoanProduct(command);

		return Response.ok().entity(entityIdentifier).build();
	}

    @GET
	@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Produces({MediaType.APPLICATION_JSON})
	public Response retrieveAllLoanProducts() {
    	
		Collection<LoanProductData> products = this.loanProductReadPlatformService.retrieveAllLoanProducts();

		return Response.ok().entity(new LoanProductList(products)).build();
    }
    
    /*
	 * see http://stackoverflow.com/questions/5250074/what-uri-can-be-used-to-request-a-default-resource
	 */
	@GET
	@Path("template")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON})
	public Response retrieveNewLoanProductDetails() {

		LoanProductData loanProduct = this.loanProductReadPlatformService.retrieveNewLoanProductDetails();

		return Response.ok().entity(loanProduct).build();
	}
    
	@GET
	@Path("{productId}")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON})
	public Response retrieveLoanProductDetails(@PathParam("productId") final Long productId) {

		LoanProductData loanProduct = this.loanProductReadPlatformService.retrieveLoanProduct(productId);

		return Response.ok().entity(loanProduct).build();
	}

	@PUT
	@Path("{productId}")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response updateLoanProduct(@PathParam("productId") final Long productId, final LoanProductCommand command) {

		command.setId(productId);
		EntityIdentifier entityIdentifier = this.loanProductWritePlatformService.updateLoanProduct(command);
		
		return Response.ok().entity(entityIdentifier).build();
	}
}