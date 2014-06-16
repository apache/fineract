package org.mifosplatform.accounting.financialactivityaccount.api;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class FinancialActivityAccountsConstants {

    public static final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<String>(Arrays.asList("id", "financialActivityData",
            "glAccountData", "glAccountOptions", "financialActivityOptions"));

    public static final String resourceNameForPermission = "FINANCIALACTIVITYACCOUNT";
}
