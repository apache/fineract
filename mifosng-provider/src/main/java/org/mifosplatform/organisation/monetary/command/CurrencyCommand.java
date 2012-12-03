package org.mifosplatform.organisation.monetary.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Immutable command for updating allowed currencies.
 */
public class CurrencyCommand {

    private String[] currencies;
    private final transient boolean makerCheckerApproval;

    public CurrencyCommand(final boolean makerCheckerApproval, final String[] currencies) {
        this.makerCheckerApproval = makerCheckerApproval;
        final List<String> listOfCurrencyCodes = new ArrayList<String>(Arrays.asList(currencies));
        Collections.sort(listOfCurrencyCodes);
        this.currencies = listOfCurrencyCodes.toArray(new String[listOfCurrencyCodes.size()]);
    }

    public String[] getCurrencies() {
        return currencies;
    }
    
    public boolean isApprovedByChecker() {
        return this.makerCheckerApproval;
    }
}