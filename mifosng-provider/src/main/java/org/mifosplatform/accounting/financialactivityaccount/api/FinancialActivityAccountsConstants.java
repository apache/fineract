/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.financialactivityaccount.api;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class FinancialActivityAccountsConstants {

    public static final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<>(Arrays.asList("id", "financialActivityData",
            "glAccountData", "glAccountOptions", "financialActivityOptions"));

    public static final String resourceNameForPermission = "FINANCIALACTIVITYACCOUNT";
}
