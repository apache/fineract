package org.mifosng.platform.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.mifosng.data.LoanPayoffReadModel;
import org.mifosng.data.MoneyData;
import org.mifosng.platform.loan.service.CalculationPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.sun.jersey.api.json.JSONWithPadding;

/**
 * Payoff functionality was something CreoCore was looking for e.g. if paid in full on date x what should be paid? full minus any rebate or incentive provided.
 * 
 * NOTE: JSONP functionality not needed now due to use of CORS
 */
@Deprecated
@Path("/open/loan/{loanId}")
@Component
@Scope("singleton")
public class LoanOpenResource {

	@Autowired
	private CalculationPlatformService calculationPlatformService;

	@GET
	@Path("calculatepayoff/{payoffDate}")
	@Consumes({ "application/x-javascript" })
	@Produces({ "application/x-javascript" })
	public Response calculatePayoff(
			@QueryParam("callback") @DefaultValue("callback") final String callbackName,
			@PathParam("loanId") final String loanId,
			@PathParam("payoffDate") final String payoffDate) {

		try {

			DateTimeFormatter formatter = ISODateTimeFormat.dateParser();
			DateTime payoffDateTime = formatter.parseDateTime(payoffDate);

			LoanPayoffReadModel loanPayoffInformation = this.calculationPlatformService
					.calculatePayoffOn(Long.valueOf(loanId),
							payoffDateTime.toLocalDate());

			JSONObject payoffInfo = new JSONObject();
			payoffInfo.put("reference", loanPayoffInformation.getReference());
			payoffInfo.put("acutalDisbursementDate",
					loanPayoffInformation.getAcutalDisbursementDate());
			payoffInfo.put("expectedMaturityDate",
					loanPayoffInformation.getExpectedMaturityDate());
			payoffInfo.put("projectedMaturityDate",
					loanPayoffInformation.getProjectedMaturityDate());
			payoffInfo.put("expectedLoanTermInDays",
					loanPayoffInformation.getExpectedLoanTermInDays());
			payoffInfo.put("projectedLoanTermInDays",
					loanPayoffInformation.getProjectedLoanTermInDays());

			JSONObject totalPaidToDate = jsonifyMoneyReadModel(loanPayoffInformation
					.getTotalPaidToDate());
			payoffInfo.put("totalPaidToDate", totalPaidToDate);

			JSONObject totalOutstandingBasedOnExpectedMaturityDate = jsonifyMoneyReadModel(loanPayoffInformation
					.getTotalOutstandingBasedOnExpectedMaturityDate());
			payoffInfo.put("totalOutstandingBasedOnExpectedMaturityDate",
					totalOutstandingBasedOnExpectedMaturityDate);

			JSONObject totalOutstandingBasedOnPayoffDate = jsonifyMoneyReadModel(loanPayoffInformation
					.getTotalOutstandingBasedOnPayoffDate());
			payoffInfo.put("totalOutstandingBasedOnPayoffDate",
					totalOutstandingBasedOnPayoffDate);

			JSONObject interestRebateOwed = jsonifyMoneyReadModel(loanPayoffInformation
					.getInterestRebateOwed());
			payoffInfo.put("interestRebateOwed", interestRebateOwed);

			JSONWithPadding payoff = new JSONWithPadding(payoffInfo,
					callbackName);

			return Response.ok(payoff).build();
		} catch (JSONException e) {
			return Response.status(Status.BAD_REQUEST).build();
		}
	}

	private JSONObject jsonifyMoneyReadModel(final MoneyData money)
			throws JSONException {

		JSONObject object = new JSONObject();
		object.put("amount", money.getAmount());
		object.put("currencyCode", money.getCurrencyCode());
		object.put("currencyDigitsAfterDecimal", money.getDigitsAfterDecimal());

		return object;
	}
}