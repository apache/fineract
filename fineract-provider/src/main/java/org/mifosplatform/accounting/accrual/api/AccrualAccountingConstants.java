/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.accrual.api;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class AccrualAccountingConstants {

    public static final String accrueTillParamName = "tillDate";
    public static final String localeParamName = "locale";
    public static final String dateFormatParamName = "dateFormat";
    
    public static final String PERIODIC_ACCRUAL_ACCOUNTING_RESOURCE_NAME = "periodicaccrual";
    public static final String PERIODIC_ACCRUAL_ACCOUNTING_EXECUTION_ERROR_CODE = "execution.failed";

    public static final Set<String> LOAN_PERIODIC_REQUEST_DATA_PARAMETERS = new HashSet<>(Arrays.asList(accrueTillParamName,
            localeParamName, dateFormatParamName));
}
