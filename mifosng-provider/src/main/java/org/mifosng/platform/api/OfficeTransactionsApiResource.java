package org.mifosng.platform.api;

import java.math.BigDecimal;
import java.util.Locale;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosng.platform.api.commands.BranchMoneyTransferCommand;
import org.mifosng.platform.api.data.EntityIdentifier;
import org.mifosng.platform.api.infrastructure.ApiDataConversionService;
import org.mifosng.platform.api.infrastructure.ApiJSONFormattingService;
import org.mifosng.platform.exceptions.UnrecognizedQueryParamException;
import org.mifosng.platform.organisation.service.OfficeReadPlatformService;
import org.mifosng.platform.organisation.service.OfficeWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/officetransactions")
@Component
@Scope("singleton")
public class OfficeTransactionsApiResource {

	private String defaultFieldList = "openingDate";
	private String allowedFieldList = "allowedParents";
	private String filterName = "myFilter";

	@Autowired
	private OfficeReadPlatformService readPlatformService;

	@Autowired
	private OfficeWritePlatformService writePlatformService;

	@Autowired
	private ApiDataConversionService apiDataConversionService;

	@Autowired
	private ApiJSONFormattingService jsonFormattingService;
	
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public Response transferMoneyFrom(@QueryParam("command") final String commandParam, final BranchMoneyTransferCommand command) {

		LocalDate transactionLocalDate = apiDataConversionService.convertFrom(command.getTransactionDate(), "transactionDate", command.getDateFormat());
		command.setTransactionLocalDate(transactionLocalDate);
		
		Locale clientLocale = this.apiDataConversionService.localeFromString(command.getLocale());

		BigDecimal transactionAmountValue = apiDataConversionService.convertFrom(command.getTransactionAmount(), "transactionAmount", clientLocale);
		command.setTransactionAmountValue(transactionAmountValue);
		
		Response response = null;
		
		if (is(commandParam, "intra-transfer")) {
			Long id = this.writePlatformService.interBranchMoneyTransfer(command);
			response = Response.ok().entity(new EntityIdentifier(id)).build();
		} else if (is(commandParam, "external-transfer")) {
			Long id = this.writePlatformService.externalBranchMoneyTransfer(command);
			response = Response.ok().entity(new EntityIdentifier(id)).build();
		} 
		
		if (response == null) {
			throw new UnrecognizedQueryParamException("command", commandParam);
		}
		
		return response;
	}
	
	private boolean is(final String commandParam, final String commandValue) {
		return StringUtils.isNotBlank(commandParam)
				&& commandParam.trim().equalsIgnoreCase(commandValue);
	}
}