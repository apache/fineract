package org.mifosng.ui.loan;

import java.math.BigDecimal;
import java.security.Principal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.mifosng.data.EntityIdentifier;
import org.mifosng.data.ErrorResponse;
import org.mifosng.data.LoanSchedule;
import org.mifosng.data.NewLoanWorkflowStepOneData;
import org.mifosng.data.command.CalculateLoanScheduleCommand;
import org.mifosng.data.command.SubmitLoanApplicationCommand;
import org.mifosng.oauth.ConsumerUserDetails;
import org.mifosng.ui.CommonRestOperations;
import org.mifosng.ui.loanproduct.ClientValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class LoanController {

	private final CommonRestOperations commonRestOperations;

	@Autowired
	public LoanController(final CommonRestOperations commonRestOperations) {
		this.commonRestOperations = commonRestOperations;
	}
	
    @ExceptionHandler(AccessDeniedException.class)
	public String accessDeniedException() {
		return "unAuthorizedAction";
	}
	
	@ExceptionHandler(ClientValidationException.class)
	public @ResponseBody Collection<ErrorResponse> validationException(ClientValidationException ex, HttpServletResponse response) {
		
		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		response.setContentType("application/json");
		
		return ex.getValidationErrors();
	}
	
	private LocalDate parseStringToLocalDate(String eventDate, String dateFieldIdentifier) {
		LocalDate eventLocalDate = null;
		if (StringUtils.isNotBlank(eventDate)) {
			try {
				Locale locale = LocaleContextHolder.getLocale();
				eventLocalDate = DateTimeFormat.forPattern("dd MMMM yyyy").withLocale(locale).parseLocalDate(eventDate.toLowerCase(locale));
			} catch (IllegalArgumentException e) {
				List<ErrorResponse> validationErrors = new ArrayList<ErrorResponse>();
				validationErrors.add(new ErrorResponse("validation.msg.invalid.date.format", dateFieldIdentifier, eventDate));
				throw new ClientValidationException(validationErrors);
			}
		}
		
		return eventLocalDate;
	}
	
	private BigDecimal parseStringToBigDecimal(String source) {
		try {
			BigDecimal number = null;

			if (StringUtils.isNotBlank(source)) {
				String sourceWithoutSpaces = source.replaceAll(" ", "");
				Locale locale = LocaleContextHolder.getLocale();
				NumberFormat format = NumberFormat.getNumberInstance(locale);
				Number parsedNumber = format.parse(sourceWithoutSpaces);
				number = BigDecimal.valueOf(Double.valueOf(parsedNumber
						.doubleValue()));
			}
			
			return number;
		} catch (ParseException e) {
			List<ErrorResponse> validationErrors = new ArrayList<ErrorResponse>();
			validationErrors.add(new ErrorResponse("validation.msg.invalid.number.format", "amount", source));
			throw new ClientValidationException(validationErrors);
		}
	}
	
	@RequestMapping(value = "/portfolio/client/{clientId}/loan/new", method = RequestMethod.GET)
	public String loadLoanCreationWorkflow(final Model model, @PathVariable("clientId") final Long clientId, final Principal principal) {
		
		UsernamePasswordAuthenticationToken authenticationToken = (UsernamePasswordAuthenticationToken) principal;
    	ConsumerUserDetails user =  (ConsumerUserDetails) authenticationToken.getPrincipal();
    	if (user.hasNoAuthorityToSumitLoanApplication()) {
    		throw new AccessDeniedException("");
    	}
		
		model.addAttribute("clientId", clientId);
		
		return "newloanapplication";
	}
	
	@RequestMapping(consumes="application/x-www-form-urlencoded", produces="application/json", value = "/portfolio/client/{clientId}/loan/new", method = RequestMethod.POST)
	public @ResponseBody EntityIdentifier submitNewLoanApplication(
					@RequestParam(value="clientId", required=false) Long applicantId,
					@RequestParam(value="productId", required=false) Long productId,
					@RequestParam(value="submittedOnDate", required=false) String submittedOnDateStr,
					@RequestParam(value="submittedOnNote", required=false) String submittedOnNote,
					@RequestParam(value="currencyCode", required=false) String currencyCode,
					@RequestParam(value="digitsAfterDecimal", required=false) Integer digitsAfterDecimal,
					@RequestParam(value="principalMoney", required=false) String principalMoney,
					@RequestParam(value="repaidEvery", required=false) Integer repaymentEvery,
					@RequestParam(value="selectedRepaymentFrequencyOption", required=false) Integer repaymentFrequency,
					@RequestParam(value="numberOfRepayments", required=false) Integer numberOfRepayments,
					@RequestParam(value="selectedAmortizationMethodOption", required=false) Integer amortizationMethod,
					@RequestParam(value="inArrearsTolerance", required=false) String inArrearsTolerance,
					@RequestParam(value="nominalInterestRate", required=false) String nominalInterestRate,
					@RequestParam(value="selectedInterestFrequencyOption", required=false) Integer interestRateFrequencyMethod,
					@RequestParam(value="selectedInterestMethodOption", required=false) Integer interestMethod,
					@RequestParam(value="interestRateCalculatedInPeriod", required=false) Integer interestRateCalculatedInPeriod,
					@RequestParam(value="expectedDisbursementDate", required=false) String expectedDisbursementDateStr,
					@RequestParam(value="repaymentsStartingFromDate", required=false) String repaymentsStartingFromDateStr,
					@RequestParam(value="interestCalculatedFromDate", required=false) String interestCalculatedFromDateStr
			) {
		
		final LocalDate submittedOnDate = parseStringToLocalDate(submittedOnDateStr, "submittedOnDate");
		final Number toleranceAmount = parseStringToBigDecimal(inArrearsTolerance);
		
		final Number principal = parseStringToBigDecimal(principalMoney);
		final Number interestRatePerPeriod = parseStringToBigDecimal(nominalInterestRate);
		final boolean flexibleRepaymentSchedule = false;
		final boolean interestRebateAllowed = false;
		
		final LocalDate expectedDisbursementDate = parseStringToLocalDate(expectedDisbursementDateStr, "expectedDisbursementDate");
		final LocalDate repaymentsStartingFromDate = parseStringToLocalDate(repaymentsStartingFromDateStr, "repaymentsStartingFromDate");
		final LocalDate interestCalculatedFromDate = parseStringToLocalDate(interestCalculatedFromDateStr, "interestCalculatedFromDate");
		
		CalculateLoanScheduleCommand calculateLoanScheduleCommand = new CalculateLoanScheduleCommand(currencyCode, digitsAfterDecimal, principal, interestRatePerPeriod, 
				interestRateFrequencyMethod, interestMethod, interestRateCalculatedInPeriod, repaymentEvery, repaymentFrequency, numberOfRepayments, amortizationMethod, 
				flexibleRepaymentSchedule, interestRebateAllowed, expectedDisbursementDate, repaymentsStartingFromDate, interestCalculatedFromDate);
		
		LoanSchedule loanSchedule = this.commonRestOperations.calculateLoanSchedule(calculateLoanScheduleCommand);
		
		SubmitLoanApplicationCommand command = new SubmitLoanApplicationCommand(applicantId, productId, 
						submittedOnDate, submittedOnNote, expectedDisbursementDate, repaymentsStartingFromDate, interestCalculatedFromDate, 
						loanSchedule, currencyCode, digitsAfterDecimal, principal, 
						interestRatePerPeriod, interestRateFrequencyMethod, interestMethod, interestRateCalculatedInPeriod, 
						repaymentEvery, repaymentFrequency, numberOfRepayments, amortizationMethod, 
						toleranceAmount, flexibleRepaymentSchedule, interestRebateAllowed);
		
		Long identifier = this.commonRestOperations.submitLoanApplication(command);
		
		return new EntityIdentifier(identifier);
	}
	
	@RequestMapping(consumes="application/x-www-form-urlencoded", produces="application/json", value = "/portfolio/loanschedule/calculate", method = RequestMethod.POST)
	public @ResponseBody LoanSchedule calculateLoanSchedule(
					@RequestParam(value="currencyCode", required=false) String currencyCode,
					@RequestParam(value="digitsAfterDecimal", required=false) Integer digitsAfterDecimal,
					@RequestParam(value="principalMoney", required=false) String principalMoney,
					@RequestParam(value="repaidEvery", required=false) Integer repaymentEvery,
					@RequestParam(value="selectedRepaymentFrequencyOption", required=false) Integer repaymentFrequency,
					@RequestParam(value="numberOfRepayments", required=false) Integer numberOfRepayments,
					@RequestParam(value="selectedAmortizationMethodOption", required=false) Integer amortizationMethod,
					@RequestParam(value="nominalInterestRate", required=false) String nominalInterestRate,
					@RequestParam(value="selectedInterestFrequencyOption", required=false) Integer interestRateFrequencyMethod,
					@RequestParam(value="selectedInterestMethodOption", required=false) Integer interestMethod,
					@RequestParam(value="interestRateCalculatedInPeriod", required=false) Integer interestRateCalculatedInPeriod,
					@RequestParam(value="expectedDisbursementDate", required=false) String expectedDisbursementDateStr,
					@RequestParam(value="repaymentsStartingFromDate", required=false) String repaymentsStartingFromDateStr,
					@RequestParam(value="interestCalculatedFromDate", required=false) String interestCalculatedFromDateStr
			) {
		
		final Number principal = parseStringToBigDecimal(principalMoney);
		final Number interestRatePerPeriod = parseStringToBigDecimal(nominalInterestRate);
		final boolean flexibleRepaymentSchedule = false;
		final boolean interestRebateAllowed = false;
		final LocalDate expectedDisbursementDate = parseStringToLocalDate(expectedDisbursementDateStr, "expectedDisbursementDate");
		final LocalDate repaymentsStartingFromDate = parseStringToLocalDate(repaymentsStartingFromDateStr, "repaymentsStartingFromDate");
		final LocalDate interestCalculatedFromDate = parseStringToLocalDate(interestCalculatedFromDateStr, "interestCalculatedFromDate");
		
		CalculateLoanScheduleCommand command = new CalculateLoanScheduleCommand(currencyCode, digitsAfterDecimal, principal, interestRatePerPeriod, 
				interestRateFrequencyMethod, interestMethod, interestRateCalculatedInPeriod, repaymentEvery, repaymentFrequency, numberOfRepayments, amortizationMethod, 
				flexibleRepaymentSchedule, interestRebateAllowed, expectedDisbursementDate, repaymentsStartingFromDate, interestCalculatedFromDate);
		
		return this.commonRestOperations.calculateLoanSchedule(command);
	}
	
	@RequestMapping(consumes="application/json", produces="application/json", value = "/portfolio/client/{clientId}/product/{productId}/new", method = RequestMethod.GET)
	public @ResponseBody NewLoanWorkflowStepOneData retrieveNewLoan(@PathVariable("clientId") Long clientId, @PathVariable("productId") Long productId) {

		return this.commonRestOperations.retrieveNewLoanApplicationDetails(clientId, productId);
	}
	
	@RequestMapping(consumes="application/json", produces="application/json", value = "/portfolio/client/{clientId}/loan/new/workflow/one", method = RequestMethod.GET)
	public @ResponseBody NewLoanWorkflowStepOneData retrieveNewLoan(@PathVariable("clientId") Long clientId) {

		return this.commonRestOperations.retrieveNewLoanApplicationStepOneDetails(clientId);
	}
}