package org.mifosng.ui.loanproduct;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.mifosng.data.EntityIdentifier;
import org.mifosng.data.ErrorResponse;
import org.mifosng.data.LoanProductData;
import org.mifosng.data.command.LoanProductCommand;
import org.mifosng.ui.CommonRestOperations;
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
public class LoanProductController {

	private final CommonRestOperations commonRestOperations;

	@Autowired
	public LoanProductController(final CommonRestOperations commonRestOperations) {
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
	
	@RequestMapping(consumes="application/json", produces="application/json", value = "/portfolio/product/loan/all", method = RequestMethod.GET)
	public @ResponseBody Collection<LoanProductData> viewAllLoanProducts() {

		return this.commonRestOperations.retrieveAllLoanProducts();
	}
	
	@RequestMapping(consumes="application/json", produces="application/json", value = "/portfolio/product/loan/new", method = RequestMethod.GET)
	public @ResponseBody LoanProductData viewNewLoanProductForm() {

		return this.commonRestOperations.retrieveNewLoanProductDetails();
	}
	
	@RequestMapping(consumes="application/x-www-form-urlencoded", produces="application/json", value = "/portfolio/product/loan/new", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody EntityIdentifier createLoanProduct(
			@RequestParam("name") String name, 
			@RequestParam("description") String description,
			@RequestParam("selectedCurrency") String currencyCode, @RequestParam("selectedDigitsAfterDecimal") Integer digitsAfterDecimal,
			@RequestParam("amount") String principalAsNumber,
			@RequestParam("repaidEvery") Integer repaymentEvery, 
			@RequestParam("selectedRepaymentFrequencyOption") Integer repaymentFrequency,
			@RequestParam("numberOfRepayments") Integer numberOfRepayments,
			@RequestParam("selectedAmortizationMethodOption") Integer amortizationMethod,
			@RequestParam("inArrearsTolerance") String inArrearsToleranceAsNumber,
			@RequestParam("nominalInterestRate") String nominalInterestRatePerPeriodAsNumber,
			@RequestParam("selectedInterestFrequencyOption") Integer nominalInterestRatePerPeriodFrequency,
			@RequestParam("selectedInterestMethodOption") Integer interestMethod,
			@RequestParam(value="interestRateCalculatedInPeriod", required=false) Integer interestCalculationPeriodMethod
			)  {
		
		LoanProductCommand command = new LoanProductCommand();
		command.setName(name);
		command.setDescription(description);
		command.setCurrencyCode(currencyCode);
		command.setDigitsAfterDecimal(digitsAfterDecimal);
		
		if (StringUtils.isNotBlank(principalAsNumber)) {
			BigDecimal principal = parseStringToBigDecimal(principalAsNumber);
			command.setPrincipal(principal);
		}
		
		command.setRepaymentEvery(repaymentEvery);
		command.setRepaymentFrequency(repaymentFrequency);
		command.setNumberOfRepayments(numberOfRepayments);
		command.setAmortizationMethod(amortizationMethod);
		
		if (StringUtils.isNotBlank(inArrearsToleranceAsNumber)) {
			BigDecimal inArrearsToleranceAmount =  parseStringToBigDecimal(inArrearsToleranceAsNumber);
			command.setInArrearsToleranceAmount(inArrearsToleranceAmount);
		}
		
		if (StringUtils.isNotBlank(nominalInterestRatePerPeriodAsNumber)) {
			BigDecimal interestRatePerPeriod = parseStringToBigDecimal(nominalInterestRatePerPeriodAsNumber);
			command.setInterestRatePerPeriod(interestRatePerPeriod);
		}
		command.setInterestRateFrequencyMethod(nominalInterestRatePerPeriodFrequency);
		command.setInterestMethod(interestMethod);
		command.setInterestCalculationPeriodMethod(interestCalculationPeriodMethod);

		return this.commonRestOperations.createLoanProduct(command);
	}
	
	@RequestMapping(consumes="application/json", produces="application/json", value = "/portfolio/product/loan/{productId}", method = RequestMethod.GET)
	public @ResponseBody LoanProductData viewLoanProduct(@PathVariable("productId") final Long productId) {

		return this.commonRestOperations.retrieveLoanProductDetails(productId);
	}
	
	@RequestMapping(consumes="application/x-www-form-urlencoded", produces="application/json", value = "/portfolio/product/loan/{productId}", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody EntityIdentifier updateLoanProduct(@PathVariable("productId") final Long productId,
			@RequestParam("name") String name, @RequestParam("description") String description,
			@RequestParam("selectedCurrency") String currencyCode, @RequestParam("selectedDigitsAfterDecimal") Integer digitsAfterDecimal,
			@RequestParam("amount") String principalAsNumber,
			@RequestParam("repaidEvery") Integer repaymentEvery, 
			@RequestParam("selectedRepaymentFrequencyOption") Integer repaymentFrequency,
			@RequestParam("numberOfRepayments") Integer numberOfRepayments,
			@RequestParam("selectedAmortizationMethodOption") Integer amortizationMethod,
			@RequestParam("inArrearsTolerance") String inArrearsToleranceAsNumber,
			@RequestParam("nominalInterestRate") String nominalInterestRatePerPeriodAsNumber,
			@RequestParam("selectedInterestFrequencyOption") Integer nominalInterestRatePerPeriodFrequency,
			@RequestParam("selectedInterestMethodOption") Integer interestMethod,
			@RequestParam(value="interestRateCalculatedInPeriod", required=false) Integer interestCalculationPeriodMethod
			)  {
		
		LoanProductCommand command = new LoanProductCommand();
		command.setId(productId);
		command.setName(name);
		command.setDescription(description);
		command.setExternalId(null);
		command.setCurrencyCode(currencyCode);
		command.setDigitsAfterDecimal(digitsAfterDecimal);
		
		if (StringUtils.isNotBlank(principalAsNumber)) {
			BigDecimal principal = parseStringToBigDecimal(principalAsNumber);
			command.setPrincipal(principal);
		}
		
		command.setRepaymentEvery(repaymentEvery);
		command.setRepaymentFrequency(repaymentFrequency);
		command.setNumberOfRepayments(numberOfRepayments);
		command.setAmortizationMethod(amortizationMethod);
		
		if (StringUtils.isNotBlank(inArrearsToleranceAsNumber)) {
			BigDecimal inArrearsToleranceAmount =  parseStringToBigDecimal(inArrearsToleranceAsNumber);
			command.setInArrearsToleranceAmount(inArrearsToleranceAmount);
		}
		
		if (StringUtils.isNotBlank(nominalInterestRatePerPeriodAsNumber)) {
			BigDecimal interestRatePerPeriod = parseStringToBigDecimal(nominalInterestRatePerPeriodAsNumber);
			command.setInterestRatePerPeriod(interestRatePerPeriod);
		}
		command.setInterestRateFrequencyMethod(nominalInterestRatePerPeriodFrequency);
		command.setInterestMethod(interestMethod);
		command.setInterestCalculationPeriodMethod(interestCalculationPeriodMethod);
		
		return this.commonRestOperations.updateLoanProduct(command);
	}
}