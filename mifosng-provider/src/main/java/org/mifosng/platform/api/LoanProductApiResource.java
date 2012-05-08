package org.mifosng.platform.api;

import java.util.Collection;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.mifosng.data.LoanProductData;
import org.mifosng.data.LoanProductList;
import org.mifosng.platform.ReadPlatformService;
import org.mifosng.platform.WritePlatformService;
import org.mifosng.platform.exceptions.ClientNotAuthenticatedException;
import org.mifosng.platform.security.PlatformUserDetailsService;
import org.mifosng.platform.user.domain.AppUser;
import org.mifosng.platform.user.domain.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/*
 * Note: Jersey looks for 'requests' starting with 'api' e.g. this resources url is https://[localhost:8080]/api/v1/loanproducts
 * Note: Called this class *ApiResource as if you leave it as same class name as older 'resource' class, jersey throws a wobbler.
 * Note: no authentication scheme is in place so calls that match api/v1/** are not managed by spring security for now.
 * 
 * 1. resource is versioned
 * 2. using plural of resource e.g. loanproducts rather than loanproduct
 * 3. @GET results in list of all loan products being returned (no scoping relevant here)
 * 4. Only 'produce'ing JSON by limiting to @Produces({MediaType.APPLICATION_JSON})
 */
@Path("/v1/loanproducts")
@Component
@Scope("singleton")
public class LoanProductApiResource {

    @Autowired
	private ReadPlatformService readPlatformService;

	@Autowired
	private WritePlatformService writePlatformService;
	
	@Autowired
	private AppUserRepository appUserRepository;
	
	private void hardcodeUserIntoSecurityContext() {
		AppUser currentUser = this.appUserRepository.findOne(Long.valueOf(1));
    	
    	Authentication auth = new UsernamePasswordAuthenticationToken(currentUser, currentUser, currentUser.getAuthorities());
		SecurityContext context = SecurityContextHolder.getContext();
		context.setAuthentication(auth);
	}

    @GET
	@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Produces({MediaType.APPLICATION_JSON})
	public Response retrieveAllLoanProducts() {
    	
    	hardcodeUserIntoSecurityContext();
		
		Collection<LoanProductData> products = this.readPlatformService.retrieveAllLoanProducts();

		return Response.ok().entity(new LoanProductList(products)).build();
    }

//	@POST
//	@Path("new")
//	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
//	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
//	public Response createLoanProduct(final CreateLoanProductCommand command) {
//
//		try {
//			EntityIdentifier entityIdentifier = this.writePlatformService.createLoanProduct(command);
//
//			return Response.ok().entity(entityIdentifier).build();
//		} catch (ClientNotAuthenticatedException e) {
//			throw new WebApplicationException(Response.status(Status.UNAUTHORIZED).build());
//		} catch (AccessDeniedException e) {
//			ErrorResponse errorResponse = new ErrorResponse("error.msg.no.permission", "id");
//			ErrorResponseList list = new ErrorResponseList(Arrays.asList(errorResponse));
//			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(list).build());
//		} catch (ApplicationDomainRuleException e) {
//			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new ErrorResponseList(e.getErrors())).build());
//		} catch (NewDataValidationException e) {
//			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new ErrorResponseList(e.getValidationErrors())).build());
//		}
//	}
//	
//	@POST
//	@Path("update")
//	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
//	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
//	public Response updateLoanProduct(final UpdateLoanProductCommand command) {
//
//		try {
//			EntityIdentifier entityIdentifier = this.writePlatformService.updateLoanProduct(command);
//			return Response.ok().entity(entityIdentifier).build();
//		} catch (ClientNotAuthenticatedException e) {
//			throw new WebApplicationException(Response.status(Status.UNAUTHORIZED).build());
//		} catch (AccessDeniedException e) {
//			ErrorResponse errorResponse = new ErrorResponse("error.msg.no.permission", "id");
//			ErrorResponseList list = new ErrorResponseList(Arrays.asList(errorResponse));
//			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(list).build());
//		} catch (ApplicationDomainRuleException e) {
//			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new ErrorResponseList(e.getErrors())).build());
//		} catch (NewDataValidationException e) {
//			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new ErrorResponseList(e.getValidationErrors())).build());
//		}
//	}
//
//	@GET
//	@Path("{productId}")
//	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
//	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
//	public Response retrieveLoanProductDetails(@PathParam("productId") final Long productId) {
//
//		try {
//			LoanProductData loanProduct = this.readPlatformService.retrieveLoanProduct(productId);
//
//			return Response.ok().entity(loanProduct).build();
//		} catch (ClientNotAuthenticatedException e) {
//			throw new WebApplicationException(Response.status(Status.UNAUTHORIZED).build());
//		} catch (AccessDeniedException e) {
//			ErrorResponse errorResponse = new ErrorResponse("error.msg.no.permission", "id");
//			ErrorResponseList list = new ErrorResponseList(Arrays.asList(errorResponse));
//			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(list).build());
//		} catch (ApplicationDomainRuleException e) {
//			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new ErrorResponseList(e.getErrors())).build());
//		} catch (NewDataValidationException e) {
//			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new ErrorResponseList(e.getValidationErrors())).build());
//		}
//	}
//	
//	@GET
//	@Path("empty")
//	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
//	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
//	public Response retrieveNewLoanProductDetails() {
//
//		try {
//			LoanProductData loanProduct = this.readPlatformService.retrieveNewLoanProductDetails();
//
//			return Response.ok().entity(loanProduct).build();
//		} catch (ClientNotAuthenticatedException e) {
//			throw new WebApplicationException(Response.status(Status.UNAUTHORIZED).build());
//		} catch (AccessDeniedException e) {
//			ErrorResponse errorResponse = new ErrorResponse("error.msg.no.permission", "id");
//			ErrorResponseList list = new ErrorResponseList(Arrays.asList(errorResponse));
//			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(list).build());
//		} catch (ApplicationDomainRuleException e) {
//			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new ErrorResponseList(e.getErrors())).build());
//		} catch (NewDataValidationException e) {
//			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new ErrorResponseList(e.getValidationErrors())).build());
//		}
//	}
}