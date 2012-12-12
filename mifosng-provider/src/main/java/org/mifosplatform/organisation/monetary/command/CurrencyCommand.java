package org.mifosplatform.organisation.monetary.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;

/**
 * Immutable command for updating allowed currencies.
 */
public class CurrencyCommand {

    private String[] currencies;

    public CurrencyCommand(final String[] currencies) {
        final List<String> listOfCurrencyCodes = new ArrayList<String>(Arrays.asList(currencies));
        Collections.sort(listOfCurrencyCodes);
        this.currencies = listOfCurrencyCodes.toArray(new String[listOfCurrencyCodes.size()]);
    }

    public String[] getCurrencies() {
        return currencies;
    }

    public void validateForUpdate() {
        List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("currencies");

        baseDataValidator.reset().parameter("currencies").value(this.currencies).arrayNotEmpty();

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }
}