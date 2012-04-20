package org.mifosng.ui.loan;

import java.math.BigDecimal;
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
import org.mifosng.data.LoanRepaymentData;
import org.mifosng.data.command.AdjustLoanTransactionCommand;
import org.mifosng.data.command.LoanStateTransitionCommand;
import org.mifosng.data.command.LoanTransactionCommand;
import org.mifosng.data.command.UndoLoanApprovalCommand;
import org.mifosng.data.command.UndoLoanDisbursalCommand;
import org.mifosng.ui.CommonRestOperations;
import org.mifosng.ui.loanproduct.ClientValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
public class LoanTransactionController {

	private final CommonRestOperations commonRestOperations;

	@Autowired
	public LoanTransactionController(final CommonRestOperations commonRestOperations) {
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
    
    @RequestMapping(value = "/portfolio/loan/{loanId}/delete", method = RequestMethod.POST)
    public @ResponseBody EntityIdentifier deleteLoan(@PathVariable("loanId") final Long loanId) {
    	
    	return this.commonRestOperations.deleteLoan(loanId);
    }
    
	@RequestMapping(consumes="application/x-www-form-urlencoded", produces="application/json", value = "/portfolio/loan/{loanId}/approve", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody EntityIdentifier approveLoan(
			@PathVariable("loanId") final Long loanId,
			@RequestParam(value="eventDate", required=false) String eventDate,
			@RequestParam(value="comment", required=false) String comment) {

		LocalDate approvalDate = parseStringToLocalDate(eventDate, "eventDate");

		LoanStateTransitionCommand command = new LoanStateTransitionCommand(loanId, approvalDate, comment);

		return this.commonRestOperations.approveLoan(command);
	}

	@RequestMapping(value = "/portfolio/loan/{loanId}/undoapproval", method = RequestMethod.POST)
	public String undoLoanApproval(@PathVariable("loanId") final Long loanId) {

		UndoLoanApprovalCommand command = new UndoLoanApprovalCommand(loanId);

		EntityIdentifier clientAccountId = this.commonRestOperations
				.undoLoanApproval(command);

		return "redirect:/portfolio/client/" + clientAccountId.getEntityId();
	}

	@RequestMapping(consumes="application/x-www-form-urlencoded", produces="application/json", value = "/portfolio/loan/{loanId}/reject", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody EntityIdentifier rejectLoan(
			@PathVariable("loanId") final Long loanId,
			@RequestParam(value="eventDate", required=false) String eventDate,
			@RequestParam(value="comment", required=false) String comment) {

		LocalDate rejectionDate = parseStringToLocalDate(eventDate, "eventDate");

		LoanStateTransitionCommand command = new LoanStateTransitionCommand(loanId, rejectionDate, comment);
		
		return this.commonRestOperations.rejectLoan(command);
	}

	@RequestMapping(consumes="application/x-www-form-urlencoded", produces="application/json", value = "/portfolio/loan/{loanId}/withdraw", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody EntityIdentifier loanWithdrawnByClient(
			@PathVariable("loanId") final Long loanId,
			@RequestParam(value="eventDate", required=false) String eventDate,
			@RequestParam(value="comment", required=false) String comment) {

		LocalDate withdrawnOnDate = parseStringToLocalDate(eventDate, "eventDate");

		LoanStateTransitionCommand command = new LoanStateTransitionCommand(loanId, withdrawnOnDate, comment);
		
		return this.commonRestOperations.withdrawLoan(command);
	}

	@RequestMapping(consumes="application/x-www-form-urlencoded", produces="application/json", value = "/portfolio/loan/{loanId}/disburse", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody EntityIdentifier disburseLoan(
			@PathVariable("loanId") final Long loanId,
			@RequestParam(value="eventDate", required=false) String eventDate,
			@RequestParam(value="comment", required=false) String comment) {

		LocalDate disbursalDate = parseStringToLocalDate(eventDate, "eventDate");

		LoanStateTransitionCommand command = new LoanStateTransitionCommand(loanId, disbursalDate, comment);

		return this.commonRestOperations.disburseLoan(command);
	}

	@RequestMapping(value = "/portfolio/loan/{loanId}/undodisbursal", method = RequestMethod.POST)
	public String undoLoanDisbursal(@PathVariable final Long loanId) {

		UndoLoanDisbursalCommand command = new UndoLoanDisbursalCommand(loanId);

		EntityIdentifier clientAccountId = this.commonRestOperations
				.undloLoanDisbursal(command);

		return "redirect:/portfolio/client/" + clientAccountId.getEntityId();
	}
	
	@RequestMapping(consumes="application/json", produces="application/json", value ="/portfolio/loan/{loanId}/repayment", method = RequestMethod.GET)
	public @ResponseBody LoanRepaymentData retrieveNewLoanRepaymentDetail(@PathVariable("loanId") final Long loanId) {
		
		return this.commonRestOperations.retrieveNewLoanRepaymentDetails(loanId);
	}

	@RequestMapping(consumes="application/x-www-form-urlencoded", produces="application/json", value = "/portfolio/loan/{loanId}/repayment", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody EntityIdentifier enterRepaymentOnLoan(
			@PathVariable("loanId") final Long loanId,
			@RequestParam(value="transactionDate", required=false) String transactionDate,
			@RequestParam(value="transactionComment", required=false) String transactionComment,
			@RequestParam(value="transactionAmount", required=false) String transactionAmount) {

		LocalDate repaymentDate = parseStringToLocalDate(transactionDate, "transactionDate");
		BigDecimal repaymentAmount = parseStringToBigDecimal(transactionAmount);

		LoanTransactionCommand command = new LoanTransactionCommand(loanId,
				repaymentDate, transactionComment, repaymentAmount);

		return this.commonRestOperations.makeLoanRepayment(command);
	}
	
	@RequestMapping(consumes="application/json", produces="application/json", value = "/portfolio/loan/{loanId}/repayment/{repaymentId}/adjust", method = RequestMethod.GET)
	public @ResponseBody LoanRepaymentData retrieveLoanRepayment(@PathVariable("loanId") final Long loanId, @PathVariable("repaymentId") final Long repaymentId) {
		return this.commonRestOperations.retrieveLoanRepaymentDetails(loanId, repaymentId);
	}
	

	@RequestMapping(consumes="application/x-www-form-urlencoded", produces="application/json", value = "/portfolio/loan/{loanId}/repayment/{repaymentId}/adjust", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody EntityIdentifier adjustPreviousRepaymentOnLoan(
			@PathVariable("loanId") final Long loanId, @PathVariable("repaymentId") final Long repaymentId,
			@RequestParam(value="transactionDate", required=false) String transactionDate,
			@RequestParam(value="transactionComment", required=false) String transactionComment,
			@RequestParam(value="transactionAmount", required=false) String transactionAmount) {

		LocalDate repaymentDate = parseStringToLocalDate(transactionDate, "transactionDate");
		BigDecimal repaymentAmount = parseStringToBigDecimal(transactionAmount);

		AdjustLoanTransactionCommand command = new AdjustLoanTransactionCommand(loanId, repaymentId, repaymentDate, transactionComment, repaymentAmount);

		return this.commonRestOperations.adjustLoanRepayment(command);
	}
	
	@RequestMapping(consumes="application/json", produces="application/json", value = "/portfolio/loan/{loanId}/waive", method = RequestMethod.GET)
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody LoanRepaymentData retrievePossibleWaiveDetails(@PathVariable("loanId") final Long loanId) {
		return this.commonRestOperations.retrieveNewLoanWaiverDetails(loanId);
	}
	
	@RequestMapping(consumes="application/x-www-form-urlencoded", produces="application/json", value = "/portfolio/loan/{loanId}/waive", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody EntityIdentifier waiveLoanAmount(
			@PathVariable("loanId") final Long loanId,
			@RequestParam(value="transactionDate", required=false) String transactionDate,
			@RequestParam(value="transactionComment", required=false) String transactionComment,
			@RequestParam(value="transactionAmount", required=false) String transactionAmount) {

		LocalDate waiveDate = parseStringToLocalDate(transactionDate, "transactionDate");
		BigDecimal waiveAmount = parseStringToBigDecimal(transactionAmount);

		LoanTransactionCommand command = new LoanTransactionCommand(loanId,
				waiveDate, transactionComment, waiveAmount);

		return this.commonRestOperations.waiveLoanAmount(command);
	}
}