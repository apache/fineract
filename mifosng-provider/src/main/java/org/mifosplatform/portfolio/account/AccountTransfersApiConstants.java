/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.account;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.mifosplatform.portfolio.account.data.AccountTransferData;

public class AccountTransfersApiConstants {

    public static final String ACCOUNT_TRANSFER_RESOURCE_NAME = "accounttransfer";

    // general
    public static final String localeParamName = "locale";
    public static final String dateFormatParamName = "dateFormat";

    // savings product and account parameters
    public static final String idParamName = "id";
    public static final String fromOfficeIdParamName = "fromOfficeId";
    public static final String fromClientIdParamName = "fromClientId";
    public static final String fromAccountIdParamName = "fromAccountId";
    public static final String fromAccountTypeParamName = "fromAccountType";
    public static final String toOfficeIdParamName = "toOfficeId";
    public static final String toClientIdParamName = "toClientId";
    public static final String toAccountIdParamName = "toAccountId";
    public static final String toAccountTypeParamName = "toAccountType";

    // transaction parameters
    public static final String transferDateParamName = "transferDate";
    public static final String transferAmountParamName = "transferAmount";
    public static final String transferDescriptionParamName = "transferDescription";

    public static final Set<String> REQUEST_DATA_PARAMETERS = new HashSet<String>(Arrays.asList(localeParamName, dateFormatParamName,
            fromOfficeIdParamName, fromClientIdParamName, fromAccountTypeParamName, fromAccountIdParamName, toOfficeIdParamName,
            toClientIdParamName, toAccountTypeParamName, toAccountIdParamName, transferDateParamName, transferAmountParamName,
            transferDescriptionParamName));

    /**
     * These parameters will match the class level parameters of
     * {@link AccountTransferData}. Where possible, we try to get response
     * parameters to match those of request parameters.
     */
    public static final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<String>(Arrays.asList(idParamName, transferDescriptionParamName,
            "currency"));
}