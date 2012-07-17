package org.mifosng.platform.api.infrastructure;

import java.math.BigDecimal;
import java.util.Locale;

import org.joda.time.LocalDate;
import org.mifosng.platform.api.commands.FundCommand;
import org.mifosng.platform.api.commands.LoanProductCommand;
import org.mifosng.platform.api.commands.OfficeCommand;
import org.mifosng.platform.api.commands.RoleCommand;

public interface ApiDataConversionService {

	LocalDate convertFrom(String dateAsString, String parameterName,
			String dateFormat);

	Integer convertToInteger(String digitsAfterDecimal, String string, Locale clientApplicationLocale);
	
	BigDecimal convertFrom(String principalFormatted, String parameterName,
			Locale clientApplicationLocale);

	Locale localeFromString(String locale);
	
	Locale localeFrom(String languageCode, String courntryCode, String variantCode);

	LoanProductCommand convertJsonToLoanProductCommand(Long resourceIdentifier, String json);
	
	FundCommand convertJsonToFundCommand(Long resourceIdentifier, String json);

	OfficeCommand convertJsonToOfficeCommand(Long resourceIdentifier, String json);

	RoleCommand convertJsonToRoleCommand(Long resourceIdentifier, String json);
}