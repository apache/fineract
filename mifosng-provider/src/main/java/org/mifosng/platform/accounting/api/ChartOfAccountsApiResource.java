package org.mifosng.platform.accounting.api;

import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.mifosng.platform.accounting.api.commands.ChartOfAccountCommand;
import org.mifosng.platform.accounting.api.data.ChartOfAccountsData;
import org.mifosng.platform.accounting.api.infrastructure.AccountingApiDataConversionService;
import org.mifosng.platform.accounting.service.ChartOfAccountsReadPlatformService;
import org.mifosng.platform.accounting.service.ChartOfAccountsWritePlatformService;
import org.mifosng.platform.api.data.EntityIdentifier;
import org.mifosng.platform.api.infrastructure.PortfolioApiJsonSerializerService;
import org.mifosng.platform.infrastructure.api.ApiParameterHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/coa")
@Component
@Scope("singleton")
public class ChartOfAccountsApiResource {

	@Autowired
	private ChartOfAccountsReadPlatformService coaReadPlatformService;
	
	@Autowired
	private ChartOfAccountsWritePlatformService coaWritePlatformService;
	
	@Autowired
	private AccountingApiDataConversionService apiDataConversionService;

	@Autowired
	private PortfolioApiJsonSerializerService apiJsonSerializerService;

//	private final static Logger logger = LoggerFactory.getLogger(ChartOfAccountsApiResource.class);

	@GET
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveExistingChartOfAccounts(@Context final UriInfo uriInfo) {

		final Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
		final boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());

		final ChartOfAccountsData chartOfAccounts = this.coaReadPlatformService.retrieveChartOfAccounts();
		
		return this.apiJsonSerializerService.serializeChartOfAccountDataToJson(prettyPrint, responseParameters, chartOfAccounts);
	}

	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String createChartOfAccounts(final String jsonRequestBody) {

		final ChartOfAccountCommand command = this.apiDataConversionService.convertJsonToChartOfAccountCommand(null, jsonRequestBody);

		final Long coaId = coaWritePlatformService.createAccount(command);

		return this.apiJsonSerializerService.serializeEntityIdentifier(new EntityIdentifier(coaId));
	}
}