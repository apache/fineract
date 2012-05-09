package org.mifosng.platform.rest;

import java.util.Arrays;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.mifosng.data.CurrencyData;
import org.mifosng.data.CurrencyList;
import org.mifosng.data.EnumOptionList;
import org.mifosng.data.EnumOptionReadModel;
import org.mifosng.data.ErrorResponse;
import org.mifosng.data.ErrorResponseList;
import org.mifosng.data.command.UpdateOrganisationCurrencyCommand;
import org.mifosng.platform.ReadPlatformService;
import org.mifosng.platform.WritePlatformService;
import org.mifosng.platform.exceptions.ApplicationDomainRuleException;
import org.mifosng.platform.exceptions.UnAuthenticatedUserException;
import org.mifosng.platform.exceptions.NewDataValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Path("/protected/config")
@Component
@Scope("singleton")
public class ApplicationConfigurationResource {

    @Autowired
	private ReadPlatformService readPlatformService;

	@Autowired
	private WritePlatformService writePlatformService;

    @GET
	@Path("currency/allowed")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response retrieveAllowedCurrenciesForOrganisation() {

		List<CurrencyData> currencyOptions = this.readPlatformService
				.retrieveAllowedCurrencies();

		return Response.ok().entity(new CurrencyList(currencyOptions)).build();
	}

	@PUT
	@Path("currency/update")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response updateAllowedCurrenciesForOrganisation(final UpdateOrganisationCurrencyCommand command) {
		
		try {
			this.writePlatformService.updateOrganisationCurrencies(command);

			return Response.ok().entity(command).build();
		} catch (UnAuthenticatedUserException e) {
			throw new WebApplicationException(Response.status(Status.UNAUTHORIZED).build());
		} catch (AccessDeniedException e) {
			ErrorResponse errorResponse = new ErrorResponse("error.msg.no.permission", "id");
			ErrorResponseList list = new ErrorResponseList(Arrays.asList(errorResponse));
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(list).build());
		} catch (ApplicationDomainRuleException e) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new ErrorResponseList(e.getErrors())).build());
		} catch (NewDataValidationException e) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new ErrorResponseList(e.getValidationErrors())).build());
		}
	}

	@GET
	@Path("currency/all")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response retrieveAllPlatformCurrencies() {

		try {
			List<CurrencyData> currencyOptions = this.readPlatformService
					.retrieveAllPlatformCurrencies();

			return Response.ok().entity(new CurrencyList(currencyOptions)).build();
		} catch (UnAuthenticatedUserException e) {
			throw new WebApplicationException(Response.status(Status.UNAUTHORIZED).build());
		} catch (AccessDeniedException e) {
			ErrorResponse errorResponse = new ErrorResponse("error.msg.no.permission", "id");
			ErrorResponseList list = new ErrorResponseList(Arrays.asList(errorResponse));
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(list).build());
		} catch (ApplicationDomainRuleException e) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new ErrorResponseList(e.getErrors())).build());
		} catch (NewDataValidationException e) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new ErrorResponseList(e.getValidationErrors())).build());
		}
	}
	
	@GET
	@Path("loan/amortization/allowed")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response retrieveAllowedLoanAmortizationMethodOptionsForOrganisation() {

		try {
			List<EnumOptionReadModel> options = this.readPlatformService
					.retrieveLoanAmortizationMethodOptions();

			return Response.ok().entity(new EnumOptionList(options)).build();
		} catch (UnAuthenticatedUserException e) {
			throw new WebApplicationException(Response.status(Status.UNAUTHORIZED).build());
		} catch (AccessDeniedException e) {
			ErrorResponse errorResponse = new ErrorResponse("error.msg.no.permission", "id");
			ErrorResponseList list = new ErrorResponseList(Arrays.asList(errorResponse));
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(list).build());
		} catch (ApplicationDomainRuleException e) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new ErrorResponseList(e.getErrors())).build());
		} catch (NewDataValidationException e) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new ErrorResponseList(e.getValidationErrors())).build());
		}
    }

	@GET
	@Path("loan/interestcalculation/allowed")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response retrieveAllowedLoanInterestMethodOptionsForOrganisation() {

		try {
			List<EnumOptionReadModel> options = this.readPlatformService
					.retrieveLoanInterestMethodOptions();

			return Response.ok().entity(new EnumOptionList(options)).build();
		} catch (UnAuthenticatedUserException e) {
			throw new WebApplicationException(Response.status(Status.UNAUTHORIZED).build());
		} catch (AccessDeniedException e) {
			ErrorResponse errorResponse = new ErrorResponse("error.msg.no.permission", "id");
			ErrorResponseList list = new ErrorResponseList(Arrays.asList(errorResponse));
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(list).build());
		} catch (ApplicationDomainRuleException e) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new ErrorResponseList(e.getErrors())).build());
		} catch (NewDataValidationException e) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new ErrorResponseList(e.getValidationErrors())).build());
		}
    }

	@GET
	@Path("loan/repaymentfrequency/allowed")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response retrieveAllowedRepaymentFrequencyOptionsForOrganisation() {

		try {
			List<EnumOptionReadModel> options = this.readPlatformService
					.retrieveRepaymentFrequencyOptions();

			return Response.ok().entity(new EnumOptionList(options)).build();
		} catch (UnAuthenticatedUserException e) {
			throw new WebApplicationException(Response.status(Status.UNAUTHORIZED).build());
		} catch (AccessDeniedException e) {
			ErrorResponse errorResponse = new ErrorResponse("error.msg.no.permission", "id");
			ErrorResponseList list = new ErrorResponseList(Arrays.asList(errorResponse));
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(list).build());
		} catch (ApplicationDomainRuleException e) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new ErrorResponseList(e.getErrors())).build());
		} catch (NewDataValidationException e) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new ErrorResponseList(e.getValidationErrors())).build());
		}
	}
	
	@GET
	@Path("loan/interestfrequency/allowed")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response retrieveAllowedNominalInterestFrequencyOptionsForOrganisation() {

		try {
			List<EnumOptionReadModel> options = this.readPlatformService.retrieveInterestFrequencyOptions();

			return Response.ok().entity(new EnumOptionList(options)).build();
		} catch (UnAuthenticatedUserException e) {
			throw new WebApplicationException(Response.status(Status.UNAUTHORIZED).build());
		} catch (AccessDeniedException e) {
			ErrorResponse errorResponse = new ErrorResponse("error.msg.no.permission", "id");
			ErrorResponseList list = new ErrorResponseList(Arrays.asList(errorResponse));
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(list).build());
		} catch (ApplicationDomainRuleException e) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new ErrorResponseList(e.getErrors())).build());
		} catch (NewDataValidationException e) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new ErrorResponseList(e.getValidationErrors())).build());
		}
	}
}